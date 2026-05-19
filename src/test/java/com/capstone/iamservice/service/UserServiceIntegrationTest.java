package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.UserResponse;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.repository.UserRepository;
import com.capstone.iamservice.util.UserUtil;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@org.junit.jupiter.api.Disabled("Yêu cầu cấu hình Database H2 hoặc Testcontainers để nạp Test Context")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserUtil userUtil;

    @MockBean
    private Cloudinary cloudinary;

    @Test
    void contextLoads() {
        assertNotNull(userService, "Dependency Injection for UserService should succeed");
    }

    @Test
    void testIntegration_getUserByEmail() {
        User mockUser = new User();
        mockUser.setEmail("integration@test.com");
        Role role = new Role();
        role.setName("ADMIN");
        mockUser.setRoles(Set.of(role));

        when(userRepository.findByEmail("integration@test.com")).thenReturn(Optional.of(mockUser));

        UserResponse response = userService.getUserByEmail("integration@test.com");

        assertEquals("integration@test.com", response.getEmail());
        assertTrue(response.getRoles().contains("ADMIN"));
        verify(userRepository, times(1)).findByEmail("integration@test.com");
    }

    @Test
    void testIntegration_getAllUsers() {
        User mockUser = new User();
        mockUser.setEmail("list@test.com");
        mockUser.setRoles(Collections.emptySet());
        Page<User> pageMock = new PageImpl<>(Collections.singletonList(mockUser));

        when(userRepository.findAll(any(PageRequest.class))).thenReturn(pageMock);

        Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 5));

        assertEquals(1, result.getTotalElements());
        assertEquals("list@test.com", result.getContent().get(0).getEmail());
    }
}
