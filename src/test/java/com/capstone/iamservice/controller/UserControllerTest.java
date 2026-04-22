package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BasePageResponse;
import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.response.UserResponse;
import com.capstone.iamservice.security.JwtUtil;
import com.capstone.iamservice.security.TokenMetaData;
import com.capstone.iamservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    @Test
    void getAllUsers_ShouldReturn200() {
        Page<UserResponse> page = new PageImpl<>(Collections.emptyList());
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        ResponseEntity<BaseResponse<BasePageResponse<UserResponse>>> result = userController.getAllUsers(1, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Lấy thông tin người dùng thành công", result.getBody().getMessage());
    }

    @Test
    void getUserByEmail_ShouldReturn200() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@ex.com");
        UserResponse response = mock(UserResponse.class);
        when(userService.getUserByEmail("test@ex.com")).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = userController.getUserByEmail(userDetails);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Lấy thông tin người dùng thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void uploadUserAvatar_ShouldReturn200() {
        MultipartFile file = mock(MultipartFile.class);
        TokenMetaData tokenMetaData = new TokenMetaData(1L, false, null);
        when(jwtUtil.getDataFromAuth()).thenReturn(tokenMetaData);
        UserResponse response = mock(UserResponse.class);
        when(userService.uploadAvatar(file, 1L)).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = userController.uploadUserAvatar(file);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Lấy thông tin người dùng thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void deleteUser_ShouldReturn204() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<BaseResponse<Void>> result = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Xóa người dùng thành công", result.getBody().getMessage());
        verify(userService, times(1)).deleteUser(1L);
    }
}
