# Password Management Features

## Overview

Tính năng quản lý mật khẩu bao gồm:
1.  **Quên mật khẩu (Forgot Password)**: Cho phép người dùng reset mật khẩu khi không nhớ mật khẩu hiện tại.
2.  **Đổi mật khẩu (Reset/Change Password)**: Cho phép người dùng đổi mật khẩu khi đã đăng nhập hoặc thông qua quy trình gửi OTP.

## Security Policies

-   **OTP Expiry**: 5 phút.
-   **Rate Limit**: Mỗi email chỉ được gửi OTP 1 lần mỗi 3 phút.
-   **Brute Force Protection (Gửi)**: Giới hạn 5 lần gửi OTP trong 1 giờ cho mỗi email.
-   **Brute Force Protection (Verify)**: Giới hạn 5 lần nhập sai OTP cho mỗi mã. Nếu vượt quá, mã OTP sẽ bị vô hiệu hóa.

## API Endpoints

Base URL: `/api/password`

### 1. Forgot Password Flow

#### Step 1: Gửi OTP
-   **Endpoint**: `POST /forgot-password/send-otp`
-   **Body**: `{ "email": "user@example.com" }`
-   **Response**: `200 OK` nếu thành công. `404` nếu email không tồn tại. `429` nếu gửi quá nhanh.

#### Step 2: Xác thực OTP (Optional check for UI)
-   **Endpoint**: `POST /forgot-password/verify-otp`
-   **Body**: `{ "email": "user@example.com", "otpCode": "123456" }`
-   **Response**: `200 OK` nếu OTP đúng. `400` nếu sai hoặc hết hạn.

#### Step 3: Đặt lại mật khẩu
-   **Endpoint**: `POST /forgot-password/reset`
-   **Body**: 
    ```json
    { 
      "email": "user@example.com", 
      "otpCode": "123456",
      "newPassword": "newPassword123",
      "confirmPassword": "newPassword123"
    }
    ```
-   **Response**: `200 OK` nếu thành công.

### 2. Reset Password Flow (Authenticated or Verify First)

Quy trình tương tự như Forgot Password nhưng dùng cho mục đích đổi mật khẩu chủ động.

-   **Gửi OTP**: `POST /reset-password/send-otp`
-   **Xác thực OTP**: `POST /reset-password/verify-otp`
-   **Đổi mật khẩu**: `POST /reset-password/reset`

## Database Changes

-   Table `otp_tokens`: Thêm cột `verification_attempts` (INT, default 0).
-   Table `otp_rate_limit`: Sử dụng để track số lần gửi.

## Integration Notes

-   Hệ thống sử dụng **Redis Stream** để gửi email bất đồng bộ.
-   Consumer cần lắng nghe stream `forgot-password-otp` và `reset-password-otp`.
-   Payload gửi qua Redis có dạng JSON:
    ```json
    {
      "userId": 1,
      "email": "user@example.com",
      "fullName": "Nguyen Van A",
      "otpCode": "123456",
      "otpType": "FORGOT_PASSWORD",
      "expiryMinutes": 5
    }
    ```
