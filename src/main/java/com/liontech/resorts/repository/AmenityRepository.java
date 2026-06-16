package com.liontech.resorts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liontech.resorts.domain.Amenity;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    Optional<Amenity> findByNameIgnoreCase(String name);
}
