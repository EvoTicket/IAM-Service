package com.capstone.iamservice.client;

import com.capstone.iamservice.dto.response.AddressInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgInternalResponse {
    private Long id;
    private String organizationName;
    private String logoUrl;
    private AddressInfo addressInfo;
    private String businessPhone;
    private String businessEmail;
}
