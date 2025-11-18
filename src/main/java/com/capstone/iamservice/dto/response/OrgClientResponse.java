package com.capstone.iamservice.dto.response;

import com.capstone.iamservice.enums.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgClientResponse {
    private Long id;
    private String organizationName;
    private String logoUrl;
    private AddressInfo addressInfo;
    private String businessPhone;
    private String businessEmail;
}
