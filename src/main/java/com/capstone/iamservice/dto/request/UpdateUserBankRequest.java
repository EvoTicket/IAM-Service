package com.capstone.iamservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserBankRequest {

    @NotBlank(message = "Mã ngân hàng không được để trống")
    @Schema(description = "Mã ngân hàng (Bank Code)", example = "VCB")
    private String bankCode;

    @NotBlank(message = "Số tài khoản ngân hàng không được để trống")
    @Schema(description = "Số tài khoản ngân hàng", example = "123456789")
    private String bankAccountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    @Schema(description = "Tên chủ tài khoản", example = "NGUYEN VAN A")
    private String bankAccountName;
}
