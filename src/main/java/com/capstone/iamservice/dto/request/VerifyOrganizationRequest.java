package com.capstone.iamservice.dto.request;

import com.capstone.iamservice.enums.OrganizationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOrganizationRequest {

    @NotNull(message = "Status is required")
    private OrganizationStatus status;

    private String rejectionReason;
}