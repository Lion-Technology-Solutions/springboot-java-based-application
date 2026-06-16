package com.liontech.resorts.dto;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import com.liontech.resorts.domain.RoomStatus;
import com.liontech.resorts.domain.RoomType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RoomForm {

    @NotBlank
    @Size(max = 20)
    private String roomNumber;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 700)
    private String description;

    @NotNull
    private RoomType type = RoomType.DELUXE_KING;

    @NotNull
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Min(1)
    @Max(12)
    private int maxGuests = 2;

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal nightlyRate;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency = "USD";

    @NotBlank
    @Size(max = 500)
    private String imageUrl;

    private Set<Long> amenityIds = new LinkedHashSet<>();

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public BigDecimal getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(BigDecimal nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Set<Long> getAmenityIds() {
        return amenityIds;
    }

    public void setAmenityIds(Set<Long> amenityIds) {
        this.amenityIds = amenityIds;
    }
}
