package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.response.AddressInfo;
import com.capstone.iamservice.dto.response.OrgClientResponse;
import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.util.LocationUtil;
import com.capstone.iamservice.util.OrganizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/organizations")
@RequiredArgsConstructor
public class OrganizationProfileClientController {

    private final OrganizationUtil organizationUtil;
    private final LocationUtil locationUtil;

    @GetMapping("/{id}")
    public ResponseEntity<OrgClientResponse> getOrganizationById(@PathVariable Long id) {
        OrganizationProfile organization = organizationUtil.getOrgProfileOrElseThrow(id);

        AddressInfo addressInfo = locationUtil.getAddressInfo(organization.getProvince(), organization.getWard(), organization.getFullAddress());

        OrgClientResponse orgClientResponse = OrgClientResponse.builder()
                .id(organization.getId())
                .organizationName(organization.getOrganizationName())
                .logoUrl(organization.getLogoUrl())
                .addressInfo(addressInfo)
                .businessPhone(organization.getBusinessPhone())
                .businessEmail(organization.getBusinessEmail())
                .build();

        return ResponseEntity.ok(orgClientResponse);
    }
}
