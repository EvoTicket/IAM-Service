package com.capstone.iamservice.dto.request;

import com.capstone.iamservice.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    @Schema(description = "Tên", example = "An")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Schema(description = "Họ", example = "Nguyễn Văn")
    private String lastName;

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phoneNumber;

    @Schema(description = "Ngày sinh", example = "2000-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "Giới tính")
    private Gender gender;

    @Schema(description = "Địa chỉ chi tiết", example = "Số 1 Đại Cồ Việt")
    private String userAddress;

    @Schema(description = "Mã phường/xã", example = "00001")
    private Integer wardCode;

    @Schema(description = "Mã tỉnh/thành phố", example = "01")
    private Integer provinceCode;
}
