package com.capstone.iamservice.client;

import com.capstone.iamservice.dto.response.AddressInfo;
import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.util.LocationUtil;
import com.capstone.iamservice.util.OrganizationUtil;
import com.capstone.iamservice.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final OrganizationUtil organizationUtil;
    private final LocationUtil locationUtil;
    private final UserUtil userUtil;

    @GetMapping("/organizations/{id}")
    public ResponseEntity<OrgInternalResponse> getOrganizationById(@PathVariable Long id) {
        OrganizationProfile organization = organizationUtil.getOrgProfileOrElseThrow(id);

        AddressInfo addressInfo = locationUtil.getAddressInfo(organization.getProvince(), organization.getWard(), organization.getFullAddress());

        OrgInternalResponse orgInternalResponse = OrgInternalResponse.builder()
                .id(organization.getId())
                .organizationName(organization.getOrganizationName())
                .logoUrl(organization.getLogoUrl())
                .addressInfo(addressInfo)
                .businessPhone(organization.getBusinessPhone())
                .businessEmail(organization.getBusinessEmail())
                .build();

        return ResponseEntity.ok(orgInternalResponse);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserInternalResponse> getUserById(@PathVariable Long id) {
        User user = userUtil.getUserOrThrow(id);
        UserInternalResponse response = UserInternalResponse.builder()
                .userFullName(user.getFullName())
                .userAvatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(response);
    }
}
