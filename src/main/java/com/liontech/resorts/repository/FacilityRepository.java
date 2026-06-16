package com.liontech.resorts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liontech.resorts.domain.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    Optional<Facility> findByNameIgnoreCase(String name);

    List<Facility> findAllByOrderByCategoryAscNameAsc();
}
