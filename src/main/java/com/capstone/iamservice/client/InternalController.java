package com.capstone.iamservice.client;

import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.response.AddressInfo;
import com.capstone.iamservice.entity.OrganizationProfile;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.util.LocationUtil;
import com.capstone.iamservice.util.OrganizationUtil;
import com.capstone.iamservice.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
                .description(organization.getDescription())
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
    @GetMapping("/users/count-since")
    public ResponseEntity<Long> getNewUsersCount(
            @RequestParam("since")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since
    ) {
        return ResponseEntity.ok(userUtil.countNewUsersSince(since));
    }

    @GetMapping("/users/{userId}/bank-info")
    public ResponseEntity<BaseResponse<UserBankAccountResponse>> getMyBankInfo(@PathVariable Long userId) {
        User user = userUtil.getUserOrThrow(userId);
        UserBankAccountResponse response = UserBankAccountResponse.builder()
                .bankCode(user.getBankCode())
                .bankAccountNumber(user.getBankAccountNumber())
                .bankAccountName(user.getBankAccountName())
                .build();

        return ResponseEntity.ok(BaseResponse.ok("lấy thông tin ngân hàng thành công", response));
    }
}
