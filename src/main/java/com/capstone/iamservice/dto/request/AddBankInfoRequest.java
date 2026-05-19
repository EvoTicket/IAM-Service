package com.capstone.iamservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddBankInfoRequest {

    @NotBlank(message = "Profile name is required")
    private String profileName;

    @NotBlank(message = "Bank code is required")
    private String bankCode;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank owner name is required")
    private String bankOwnerName;
}
