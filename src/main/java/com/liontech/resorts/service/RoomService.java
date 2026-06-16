package com.liontech.resorts.service;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liontech.resorts.domain.Amenity;
import com.liontech.resorts.domain.Room;
import com.liontech.resorts.domain.RoomStatus;
import com.liontech.resorts.domain.RoomType;
import com.liontech.resorts.dto.RoomForm;
import com.liontech.resorts.repository.AmenityRepository;
import com.liontech.resorts.repository.RoomRepository;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;

    public RoomService(RoomRepository roomRepository, AmenityRepository amenityRepository) {
        this.roomRepository = roomRepository;
        this.amenityRepository = amenityRepository;
    }

    @Transactional(readOnly = true)
    public List<Room> listAvailableRooms() {
        return roomRepository.findAllByStatusOrderByNightlyRateAsc(RoomStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<Room> listAllRooms() {
        return roomRepository.findAllByOrderByRoomNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<Amenity> listAmenities() {
        return amenityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoom(Long id) {
        return roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found."));
    }

    @Transactional
    public Room addRoom(RoomForm form) {
        roomRepository.findByRoomNumberIgnoreCase(form.getRoomNumber())
            .ifPresent(existing -> {
                throw new BusinessException("Room number already exists.");
            });

        Room room = new Room();
        room.setRoomNumber(form.getRoomNumber().trim());
        room.setName(form.getName().trim());
        room.setDescription(form.getDescription().trim());
        room.setType(form.getType());
        room.setStatus(form.getStatus());
        room.setMaxGuests(form.getMaxGuests());
        room.setNightlyRate(form.getNightlyRate());
        room.setCurrency(form.getCurrency().trim().toUpperCase());
        room.setImageUrl(form.getImageUrl().trim());
        room.setAmenities(resolveAmenities(form.getAmenityIds()));
        return roomRepository.save(room);
    }

    @Transactional
    public int seedFullRoomCatalog() {
        int before = (int) roomRepository.count();
        createRoomIfMissing("101", "Azure King Retreat", RoomType.DELUXE_KING, 2, "329.00",
            "A calm king retreat with garden terrace, marble bath, curated minibar, and quiet workspace.",
            "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1200&q=80",
            "King Bed", "Garden View", "Fast Wi-Fi", "Rain Shower");
        createRoomIfMissing("122", "Caribbean Double Queen", RoomType.DOUBLE_QUEEN, 4, "379.00",
            "A bright double queen room designed for families and business teams visiting from anywhere in the world.",
            "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80",
            "Two Queen Beds", "Fast Wi-Fi", "Smart TV", "Breakfast Included");
        createRoomIfMissing("204", "Oceanfront Signature Suite", RoomType.OCEAN_SUITE, 3, "589.00",
            "A private oceanfront suite with balcony dining, lounge seating, and elevated concierge service.",
            "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80",
            "Ocean View", "Private Balcony", "Concierge", "Espresso Bar");
        createRoomIfMissing("305", "Executive Work Suite", RoomType.EXECUTIVE_SUITE, 2, "649.00",
            "A boardroom-ready executive suite with separate lounge, standing desk, secure Wi-Fi, and airport transfer.",
            "https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=1200&q=80",
            "Workspace", "Fast Wi-Fi", "Airport Transfer", "Concierge");
        createRoomIfMissing("501", "Presidential Sky Villa", RoomType.PRESIDENTIAL_VILLA, 6, "1499.00",
            "A full-floor villa with chef-ready dining, panoramic ocean views, private pool, and dedicated butler support.",
            "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
            "Private Pool", "Ocean View", "Butler Service", "Chef Dining");
        createRoomIfMissing("610", "Family Residence", RoomType.FAMILY_RESIDENCE, 8, "899.00",
            "A spacious multi-room residence with kitchenette, laundry, kids zone, and flexible bedding for long stays.",
            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1200&q=80",
            "Kitchenette", "Laundry", "Kids Zone", "Two Queen Beds");
        return (int) roomRepository.count() - before;
    }

    private void createRoomIfMissing(
        String roomNumber,
        String name,
        RoomType type,
        int maxGuests,
        String nightlyRate,
        String description,
        String imageUrl,
        String... amenityNames
    ) {
        if (roomRepository.findByRoomNumberIgnoreCase(roomNumber).isPresent()) {
            return;
        }

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setName(name);
        room.setType(type);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setMaxGuests(maxGuests);
        room.setNightlyRate(new BigDecimal(nightlyRate));
        room.setCurrency("USD");
        room.setDescription(description);
        room.setImageUrl(imageUrl);

        Set<Amenity> amenities = new LinkedHashSet<>();
        for (String amenityName : amenityNames) {
            amenityRepository.findByNameIgnoreCase(amenityName).ifPresent(amenities::add);
        }
        room.setAmenities(amenities);
        roomRepository.save(room);
    }

    private Set<Amenity> resolveAmenities(Set<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(amenityRepository.findAllById(amenityIds));
    }
}
