package com.liontech.resorts.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liontech.resorts.domain.Facility;
import com.liontech.resorts.repository.FacilityRepository;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Transactional(readOnly = true)
    public List<Facility> listFacilities() {
        return facilityRepository.findAllByOrderByCategoryAscNameAsc();
    }
}
