package com.liontech.resorts.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liontech.resorts.domain.Booking;
import com.liontech.resorts.domain.BookingStatus;
import com.liontech.resorts.domain.Payment;
import com.liontech.resorts.domain.PaymentStatus;
import com.liontech.resorts.domain.Room;
import com.liontech.resorts.domain.RoomStatus;
import com.liontech.resorts.domain.UserAccount;
import com.liontech.resorts.dto.BookingRequest;
import com.liontech.resorts.dto.PaymentRequest;
import com.liontech.resorts.repository.BookingRepository;
import com.liontech.resorts.repository.PaymentRepository;
import com.liontech.resorts.repository.RoomRepository;
import com.liontech.resorts.repository.UserAccountRepository;
import com.liontech.resorts.service.FakePaymentService.PaymentResult;

@Service
public class BookingService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final EnumSet<BookingStatus> ACTIVE_BOOKING_STATUSES =
        EnumSet.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED);
    private static final Map<String, BigDecimal> CURRENCY_RATES = Map.of(
        "USD", new BigDecimal("1.00"),
        "CAD", new BigDecimal("1.36"),
        "EUR", new BigDecimal("0.92"),
        "GBP", new BigDecimal("0.78"),
        "HTG", new BigDecimal("132.00")
    );

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final UserAccountRepository userAccountRepository;
    private final FakePaymentService fakePaymentService;
    private final SecureRandom secureRandom = new SecureRandom();

    public BookingService(
        BookingRepository bookingRepository,
        PaymentRepository paymentRepository,
        RoomRepository roomRepository,
        UserAccountRepository userAccountRepository,
        FakePaymentService fakePaymentService
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.roomRepository = roomRepository;
        this.userAccountRepository = userAccountRepository;
        this.fakePaymentService = fakePaymentService;
    }

    @Transactional
    public Booking createBooking(BookingRequest request, String guestEmail) {
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        UserAccount guest = userAccountRepository.findByEmailIgnoreCase(guestEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Guest account not found."));

        validateBookingRequest(request, room);

        boolean roomTaken = bookingRepository.existsByRoomAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            room,
            ACTIVE_BOOKING_STATUSES,
            request.getCheckOutDate(),
            request.getCheckInDate()
        );
        if (roomTaken) {
            throw new BusinessException("This room is no longer available for the selected dates.");
        }

        String currency = normalizeCurrency(request.getCurrency());
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal total = room.getNightlyRate()
            .multiply(BigDecimal.valueOf(nights))
            .multiply(CURRENCY_RATES.get(currency))
            .setScale(2, RoundingMode.HALF_UP);

        Booking booking = new Booking();
        booking.setReference(reference());
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuests(request.getGuests());
        booking.setCountryOfResidence(request.getCountryOfResidence().trim());
        booking.setPreferredLanguage(request.getPreferredLanguage().trim().toLowerCase(Locale.ROOT));
        booking.setCurrency(currency);
        booking.setTotalAmount(total);
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBookingForGuest(String reference, String guestEmail) {
        Booking booking = findByReference(reference);
        assertBookingOwner(booking, guestEmail);
        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> listGuestBookings(String guestEmail) {
        UserAccount guest = userAccountRepository.findByEmailIgnoreCase(guestEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Guest account not found."));
        return bookingRepository.findByGuestOrderByCreatedAtDesc(guest);
    }

    @Transactional(readOnly = true)
    public Payment latestPaymentForBooking(Booking booking) {
        return paymentRepository.findTopByBookingOrderByCreatedAtDesc(booking).orElse(null);
    }

    @Transactional
    public Payment processPayment(String reference, PaymentRequest request, String guestEmail) {
        Booking booking = findByReference(reference);
        assertBookingOwner(booking, guestEmail);

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new BusinessException("This booking cannot accept payments.");
        }

        PaymentResult result = fakePaymentService.authorize(request, booking.getTotalAmount(), booking.getCurrency());
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setTransactionId(result.transactionId());
        payment.setAmount(booking.getTotalAmount());
        payment.setCurrency(booking.getCurrency());
        payment.setMaskedCard(result.maskedCard());
        payment.setProcessorMessage(result.message());
        payment.setStatus(result.approved() ? PaymentStatus.APPROVED : PaymentStatus.DECLINED);
        paymentRepository.save(payment);

        if (result.approved()) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }
        return payment;
    }

    @Transactional
    public Booking completeStayCheckout(String reference, String guestEmail) {
        Booking booking = findByReference(reference);
        assertBookingOwner(booking, guestEmail);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed stays can be checked out.");
        }
        booking.setStatus(BookingStatus.CHECKED_OUT);
        return bookingRepository.save(booking);
    }

    private Booking findByReference(String reference) {
        return bookingRepository.findByReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
    }

    private void validateBookingRequest(BookingRequest request, Room room) {
        LocalDate today = LocalDate.now();
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BusinessException("This room is not currently available.");
        }
        if (request.getCheckInDate().isBefore(today)) {
            throw new BusinessException("Check-in date cannot be in the past.");
        }
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BusinessException("Check-out date must be after check-in date.");
        }
        if (request.getGuests() > room.getMaxGuests()) {
            throw new BusinessException("This room supports up to " + room.getMaxGuests() + " guests.");
        }
    }

    private void assertBookingOwner(Booking booking, String guestEmail) {
        if (!booking.getGuest().getEmail().equalsIgnoreCase(guestEmail)) {
            throw new BusinessException("This booking belongs to another account.");
        }
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency == null ? "USD" : currency.trim().toUpperCase(Locale.ROOT);
        return CURRENCY_RATES.containsKey(normalized) ? normalized : "USD";
    }

    private String reference() {
        StringBuilder builder = new StringBuilder("LTR-");
        for (int i = 0; i < 8; i++) {
            builder.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}
