package com.capstone.iamservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrganizerAccountProfileResponse {
    private Long id;
    private Long userId;
    // --- Organization Header & Status ---
    private String organizationName;
    private String organizationType;       // VD: "Doanh nghiệp tổ chức sự kiện"
    private String status;                 // VD: "ACTIVE", "PENDING"
    private String verificationLevel;      // VD: "BASIC_VERIFIED"
    private LocalDateTime joinedAt;        // Dùng để hiển thị "Thành viên từ 03/2026"
    private String primaryContactName;     // Liên hệ chính lấy từ tên user
    // --- Profile Images & Public Info ---
    private String logoUrl;                // Avatar
    private String coverUrl;               // Ảnh bìa (Cover)
    private String website;
    private String supportEmail;           // Email hỗ trợ người mua
    private String supportPhone;           // Hotline
    private String shortDescription;       // Mô tả ngắn gọn
    private String publicBio;              // Public organizer bio

    private String businessType;           // Loại hình kinh doanh
    private String taxCode;                // Mã số thuế
    private boolean taxVerified;           // Đã xác minh MST chưa
    private String billingAddress;         // Địa chỉ xuất hoá đơn
    // --- Account Owner (Chủ tài khoản & Bảo mật) ---
    private AccountOwnerInfo ownerInfo;
    // --- Legal & Payout (Thanh toán & Pháp lý) ---
    private List<BankInfoResponse> payoutInfo;

    @Data
    @Builder
    public static class AccountOwnerInfo {
        private String fullName;
        private String email;
        private String phone;
        private String employeeCode;            // hardcode
        private boolean twoFactorEnabled;       // hardcode
        private LocalDateTime lastPasswordChangeAt;  // hardcode
        private int activeSessions;            // hardcode
    }
    @Data
    @Builder
    public static class BankInfoResponse {
        private String accountName;            // Tên chủ tài khoản ngân hàng
        private String bankName;               // Tên NH (VD: "Vietcombank")
           // bỏ Chi nhánh (VD: "CN Sài Gòn"), chỉ hiện thị tên ngân hàng chính để đơn giản
        private String accountNumber;          // Số TK (VD: "•••• •••• 4892")
    }
}
