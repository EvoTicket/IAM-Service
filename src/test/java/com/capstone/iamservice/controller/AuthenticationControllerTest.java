package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.AuthenticationRequest;
import com.capstone.iamservice.dto.request.GoogleLoginRequest;
import com.capstone.iamservice.dto.request.RefreshTokenRequest;
import com.capstone.iamservice.dto.request.RegisterRequest;
import com.capstone.iamservice.dto.response.AuthenticationResponse;
import com.capstone.iamservice.service.AuthenticationService;
import com.capstone.iamservice.service.GoogleAuthService;
import com.capstone.iamservice.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private GoogleAuthService googleAuthService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void register_ShouldReturn201() {
        RegisterRequest request = mock(RegisterRequest.class);
        AuthenticationResponse response = mock(AuthenticationResponse.class);
        when(authenticationService.register(request)).thenReturn(response);

        ResponseEntity<BaseResponse<AuthenticationResponse>> result = authenticationController.register(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Đăng ký thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void login_ShouldReturn200() {
        AuthenticationRequest request = mock(AuthenticationRequest.class);
        AuthenticationResponse response = mock(AuthenticationResponse.class);
        when(authenticationService.authenticate(request)).thenReturn(response);

        ResponseEntity<BaseResponse<AuthenticationResponse>> result = authenticationController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Đăng nhập thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void loginWithGoogle_ShouldReturn200() {
        GoogleLoginRequest request = mock(GoogleLoginRequest.class);
        when(request.getAccessToken()).thenReturn("google_token");
        AuthenticationResponse response = mock(AuthenticationResponse.class);
        when(googleAuthService.processGoogleLogin("google_token")).thenReturn(response);

        ResponseEntity<BaseResponse<AuthenticationResponse>> result = authenticationController.loginWithGoogle(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Đăng nhập Google thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void refreshToken_ShouldReturn200() {
        RefreshTokenRequest request = mock(RefreshTokenRequest.class);
        when(request.getRefreshToken()).thenReturn("refresh_token");
        AuthenticationResponse response = mock(AuthenticationResponse.class);
        when(refreshTokenService.refresh("refresh_token")).thenReturn(response);

        ResponseEntity<BaseResponse<AuthenticationResponse>> result = authenticationController.refreshToken(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Làm mới token thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void logout_ShouldReturn200() {
        RefreshTokenRequest request = mock(RefreshTokenRequest.class);
        when(request.getRefreshToken()).thenReturn("refresh_token");
        doNothing().when(refreshTokenService).revoke("refresh_token");

        ResponseEntity<BaseResponse<Void>> result = authenticationController.logout(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Đăng xuất thành công", result.getBody().getMessage());
        verify(refreshTokenService, times(1)).revoke("refresh_token");
    }
}
