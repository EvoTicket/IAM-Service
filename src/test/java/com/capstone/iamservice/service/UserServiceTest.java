package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.UserResponse;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.util.UserUtil;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserUtil userUtil;

    @Mock
    private Cloudinary cloudinary;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserByEmail_WhenExists_ShouldReturnUserResponse() {
        User user = new User();
        user.setEmail("test@ex.com");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));

        when(userRepository.findByEmail("test@ex.com")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByEmail("test@ex.com");

        assertEquals("test@ex.com", result.getEmail());
        assertTrue(result.getRoles().contains("USER"));
    }

    @Test
    void getUserByEmail_WhenNotExists_ShouldThrowAppException() {
        when(userRepository.findByEmail("notfound@ex.com")).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> userService.getUserByEmail("notfound@ex.com"));
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUserResponse() {
        User user = new User();
        user.setEmail("test@ex.com");
        user.setRoles(Collections.emptySet());
        Page<User> page = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("test@ex.com", result.getContent().get(0).getEmail());
    }

    @Test
    void deleteUser_WhenExists_ShouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WhenNotExists_ShouldThrowAppException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(AppException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void uploadAvatar_Success_ShouldReturnUserResponse() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});

        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("url", "http://cloud.com/img.png"));

        User user = new User();
        user.setId(1L);
        user.setRoles(Collections.emptySet());
        when(userUtil.getUserOrThrow(1L)).thenReturn(user);

        UserResponse result = userService.uploadAvatar(file, 1L);

        assertEquals("http://cloud.com/img.png", result.getAvatarUrl());
        verify(userUtil, times(1)).getUserOrThrow(1L);
    }

    @Test
    void uploadAvatar_InvalidContentType_ShouldThrowException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(IllegalArgumentException.class, () -> userService.uploadAvatar(file, 1L));
    }
}
