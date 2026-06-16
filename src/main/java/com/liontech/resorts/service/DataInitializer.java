package com.liontech.resorts.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.liontech.resorts.domain.AccountRole;
import com.liontech.resorts.domain.Amenity;
import com.liontech.resorts.domain.Facility;
import com.liontech.resorts.domain.UserAccount;
import com.liontech.resorts.repository.AmenityRepository;
import com.liontech.resorts.repository.FacilityRepository;
import com.liontech.resorts.repository.UserAccountRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    private final AmenityRepository amenityRepository;
    private final FacilityRepository facilityRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoomService roomService;
    private final PasswordEncoder passwordEncoder;

    @Value("${liontech.seed.demo-data:true}")
    private boolean seedDemoData;

    @Value("${liontech.seed.admin-email:admin@liontechresorts.com}")
    private String adminEmail;

    @Value("${liontech.seed.admin-password:Admin@12345!}")
    private String adminPassword;

    public DataInitializer(
        AmenityRepository amenityRepository,
        FacilityRepository facilityRepository,
        UserAccountRepository userAccountRepository,
        RoomService roomService,
        PasswordEncoder passwordEncoder
    ) {
        this.amenityRepository = amenityRepository;
        this.facilityRepository = facilityRepository;
        this.userAccountRepository = userAccountRepository;
        this.roomService = roomService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdminAccount();
        if (!seedDemoData) {
            return;
        }
        seedAmenities();
        seedFacilities();
        roomService.seedFullRoomCatalog();
    }

    private void seedAdminAccount() {
        if (userAccountRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setEmail(adminEmail.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setFirstName("LionTech");
        admin.setLastName("Administrator");
        admin.setCountry("United States");
        admin.setPreferredLanguage("en");
        admin.setRole(AccountRole.ADMIN);
        admin.setEnabled(true);
        userAccountRepository.save(admin);
    }

    private void seedAmenities() {
        amenity("King Bed", "bed", "Premium king bed with high-thread-count linens.");
        amenity("Two Queen Beds", "beds", "Two queen beds prepared for groups and families.");
        amenity("Ocean View", "waves", "Direct ocean or coastline view.");
        amenity("Garden View", "leaf", "Private garden or courtyard view.");
        amenity("Private Balcony", "balcony", "Outdoor balcony with lounge seating.");
        amenity("Fast Wi-Fi", "wifi", "High-speed secure wireless internet.");
        amenity("Smart TV", "tv", "Streaming-ready smart television.");
        amenity("Rain Shower", "shower", "Spa-style rainfall shower.");
        amenity("Breakfast Included", "coffee", "Daily breakfast included with the room.");
        amenity("Concierge", "concierge", "Dedicated guest assistance for travel and reservations.");
        amenity("Espresso Bar", "espresso", "In-room espresso and tea service.");
        amenity("Workspace", "desk", "Work desk with comfortable chair and charging access.");
        amenity("Airport Transfer", "car", "Airport transfer coordination available.");
        amenity("Private Pool", "pool", "Private pool or plunge pool access.");
        amenity("Butler Service", "service", "Personalized butler support.");
        amenity("Chef Dining", "chef", "Private dining and chef service coordination.");
        amenity("Kitchenette", "kitchen", "Kitchenette for extended stays.");
        amenity("Laundry", "laundry", "In-room or nearby laundry access.");
        amenity("Kids Zone", "family", "Family-friendly sleeping and activity space.");
    }

    private void seedFacilities() {
        facility("Azure Spa and Wellness", "Wellness",
            "Full-service spa with massage therapy, sauna, hydrotherapy, and wellness consultations.",
            "7:00 AM - 9:00 PM");
        facility("LionTech Signature Restaurant", "Dining",
            "Contemporary Caribbean and international cuisine with private dining options.",
            "6:30 AM - 11:00 PM");
        facility("Infinity Pool Club", "Leisure",
            "Ocean-facing pool deck with cabanas, mocktail service, and sunset seating.",
            "6:00 AM - 10:00 PM");
        facility("Executive Conference Center", "Business",
            "Boardrooms, event suites, hybrid meeting technology, and concierge business support.",
            "24 hours by reservation");
        facility("Global Guest Desk", "Travel",
            "Multilingual assistance for airport transfers, excursions, visas, and local experiences.",
            "24 hours");
        facility("Fitness and Recovery Studio", "Wellness",
            "Cardio, strength training, guided recovery, and personal training sessions.",
            "5:00 AM - 11:00 PM");
    }

    private void amenity(String name, String icon, String description) {
        amenityRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Amenity amenity = new Amenity();
            amenity.setName(name);
            amenity.setIcon(icon);
            amenity.setDescription(description);
            return amenityRepository.save(amenity);
        });
    }

    private void facility(String name, String category, String description, String hours) {
        facilityRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Facility facility = new Facility();
            facility.setName(name);
            facility.setCategory(category);
            facility.setDescription(description);
            facility.setHours(hours);
            return facilityRepository.save(facility);
        });
    }
}
