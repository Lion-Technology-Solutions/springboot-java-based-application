package com.liontech.resorts.web;

import java.security.Principal;
import java.time.LocalDate;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.liontech.resorts.domain.Booking;
import com.liontech.resorts.domain.Payment;
import com.liontech.resorts.domain.PaymentStatus;
import com.liontech.resorts.domain.UserAccount;
import com.liontech.resorts.dto.BookingRequest;
import com.liontech.resorts.dto.PaymentRequest;
import com.liontech.resorts.service.AccountService;
import com.liontech.resorts.service.BookingService;
import com.liontech.resorts.service.BusinessException;
import com.liontech.resorts.service.RoomService;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final AccountService accountService;

    public BookingController(BookingService bookingService, RoomService roomService, AccountService accountService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.accountService = accountService;
    }

    @GetMapping("/book/{roomId}")
    public String bookRoom(@PathVariable Long roomId, Model model, Principal principal) {
        BookingRequest request = new BookingRequest();
        request.setRoomId(roomId);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));
        request.setCurrency("USD");
        UserAccount account = accountService.findByEmail(principal.getName());
        request.setCountryOfResidence(account.getCountry());
        request.setPreferredLanguage(account.getPreferredLanguage());
        model.addAttribute("bookingRequest", request);
        model.addAttribute("room", roomService.getRoom(roomId));
        return "booking";
    }

    @PostMapping("/bookings")
    public String createBooking(
        @Valid @ModelAttribute BookingRequest bookingRequest,
        BindingResult bindingResult,
        Model model,
        Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("room", roomService.getRoom(bookingRequest.getRoomId()));
            return "booking";
        }

        try {
            Booking booking = bookingService.createBooking(bookingRequest, principal.getName());
            return "redirect:/checkout/" + booking.getReference();
        } catch (BusinessException exception) {
            bindingResult.reject("booking", exception.getMessage());
            model.addAttribute("room", roomService.getRoom(bookingRequest.getRoomId()));
            return "booking";
        }
    }

    @GetMapping("/checkout/{reference}")
    public String checkout(@PathVariable String reference, Model model, Principal principal) {
        Booking booking = bookingService.getBookingForGuest(reference, principal.getName());
        model.addAttribute("booking", booking);
        model.addAttribute("paymentRequest", new PaymentRequest());
        model.addAttribute("latestPayment", bookingService.latestPaymentForBooking(booking));
        return "checkout";
    }

    @PostMapping("/checkout/{reference}")
    public String processCheckout(
        @PathVariable String reference,
        @Valid @ModelAttribute PaymentRequest paymentRequest,
        BindingResult bindingResult,
        Model model,
        Principal principal
    ) {
        Booking booking = bookingService.getBookingForGuest(reference, principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("booking", booking);
            model.addAttribute("latestPayment", bookingService.latestPaymentForBooking(booking));
            return "checkout";
        }

        Payment payment = bookingService.processPayment(reference, paymentRequest, principal.getName());
        if (payment.getStatus() != PaymentStatus.APPROVED) {
            model.addAttribute("booking", booking);
            model.addAttribute("latestPayment", payment);
            model.addAttribute("paymentError", payment.getProcessorMessage());
            return "checkout";
        }

        return "redirect:/confirmation/" + reference;
    }

    @GetMapping("/confirmation/{reference}")
    public String confirmation(@PathVariable String reference, Model model, Principal principal) {
        Booking booking = bookingService.getBookingForGuest(reference, principal.getName());
        model.addAttribute("booking", booking);
        model.addAttribute("latestPayment", bookingService.latestPaymentForBooking(booking));
        return "confirmation";
    }

    @GetMapping("/account/bookings")
    public String myBookings(Model model, Principal principal) {
        model.addAttribute("bookings", bookingService.listGuestBookings(principal.getName()));
        return "my-bookings";
    }

    @PostMapping("/account/bookings/{reference}/checkout-stay")
    public String checkoutStay(
        @PathVariable String reference,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.completeStayCheckout(reference, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Stay checked out successfully.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/account/bookings";
    }
}
