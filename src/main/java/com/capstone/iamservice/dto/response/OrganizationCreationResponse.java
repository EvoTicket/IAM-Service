package com.capstone.iamservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreationResponse {
    String newToken;
    OrganizationProfileResponse organizationProfile;
}
