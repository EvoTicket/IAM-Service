package com.capstone.iamservice.util;

import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.OrganizationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationUtil {
    private final OrganizationProfileRepository organizationProfileRepository;

    public OrganizationProfile getOrgProfileOrElseThrow(Long id){
        return organizationProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Organization not found"));
    }
}
