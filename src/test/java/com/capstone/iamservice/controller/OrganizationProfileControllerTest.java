package com.capstone.iamservice.controller;

import com.capstone.iamservice.dto.BasePageResponse;
import com.capstone.iamservice.dto.BaseResponse;
import com.capstone.iamservice.dto.request.CreateOrganizationRequest;
import com.capstone.iamservice.dto.request.UpdateOrganizationRequest;
import com.capstone.iamservice.dto.request.VerifyOrganizationRequest;
import com.capstone.iamservice.dto.response.OrganizationCreationResponse;
import com.capstone.iamservice.dto.response.OrganizationProfileResponse;
import com.capstone.iamservice.enums.OrganizationStatus;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.security.JwtUtil;
import com.capstone.iamservice.security.TokenMetaData;
import com.capstone.iamservice.service.OrganizationProfileService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationProfileControllerTest {

    @Mock
    private OrganizationProfileService organizationService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private OrganizationProfileController controller;

    @Test
    void createOrganization_ShouldReturn201() {
        CreateOrganizationRequest request = mock(CreateOrganizationRequest.class);
        MultipartFile logoFile = mock(MultipartFile.class);
        MultipartFile licenseFile = mock(MultipartFile.class);
        MultipartFile coverFile = mock(MultipartFile.class);
        OrganizationCreationResponse response = mock(OrganizationCreationResponse.class);
        when(organizationService.createOrganization(request, logoFile, licenseFile, coverFile)).thenReturn(response);

        ResponseEntity<BaseResponse<OrganizationCreationResponse>> result = controller.createOrganization(request, logoFile, licenseFile, coverFile);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("tạo org profile thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void getOrganizationById_ShouldReturn200() {
        OrganizationProfileResponse response = mock(OrganizationProfileResponse.class);
        when(organizationService.getOrganizationById(1L)).thenReturn(response);

        ResponseEntity<BaseResponse<OrganizationProfileResponse>> result = controller.getOrganizationById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Lấy profile thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void getMyOrganization_ShouldReturn200() {
        TokenMetaData metadata = new TokenMetaData(1L, true, 2L);
        when(jwtUtil.getDataFromAuth()).thenReturn(metadata);
        OrganizationProfileResponse response = mock(OrganizationProfileResponse.class);
        when(organizationService.getOrganizationByUserId(1L)).thenReturn(response);

        ResponseEntity<BaseResponse<OrganizationProfileResponse>> result = controller.getMyOrganization();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Lấy profile thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void getOrganizationByUserId_ShouldReturn200() {
        OrganizationProfileResponse response = mock(OrganizationProfileResponse.class);
        when(organizationService.getOrganizationByUserId(1L)).thenReturn(response);

        ResponseEntity<BaseResponse<OrganizationProfileResponse>> result = controller.getOrganizationByUserId(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Lấy profile thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void updateOrganization_Success_ShouldReturn200() {
        UpdateOrganizationRequest request = mock(UpdateOrganizationRequest.class);
        TokenMetaData metadata = new TokenMetaData(1L, true, 2L);
        when(jwtUtil.getDataFromAuth()).thenReturn(metadata);
        OrganizationProfileResponse response = mock(OrganizationProfileResponse.class);
        when(organizationService.updateOrganization(2L, request)).thenReturn(response);

        ResponseEntity<BaseResponse<OrganizationProfileResponse>> result = controller.updateOrganization(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Update profile thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void updateOrganization_NotOrganization_ShouldThrowException() {
        UpdateOrganizationRequest request = mock(UpdateOrganizationRequest.class);
        TokenMetaData metadata = new TokenMetaData(1L, false, null);
        when(jwtUtil.getDataFromAuth()).thenReturn(metadata);

        assertThrows(AppException.class, () -> controller.updateOrganization(request));
    }

    @Test
    void uploadUserAvatar_Success_ShouldReturn200() {
        MultipartFile file = mock(MultipartFile.class);
        TokenMetaData metadata = new TokenMetaData(1L, true, 2L);
        when(jwtUtil.getDataFromAuth()).thenReturn(metadata);
        OrganizationProfileResponse response = mock(OrganizationProfileResponse.class);
        when(organizationService.uploadLogoUrl(file, 1L, 2L)).thenReturn(response);

        ResponseEntity<BaseResponse<OrganizationProfileResponse>> result = controller.uploadUserAvatar(file);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Lấy thông tin người dùng thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void deleteOrganization_ShouldReturn200() {
        doNothing().when(organizationService).deleteOrganization(1L);

        ResponseEntity<BaseResponse<Void>> result = controller.deleteOrganization(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Xóa profile thành công", result.getBody().getMessage());
        verify(organizationService, times(1)).deleteOrganization(1L);
    }

    @Test
    void verifyOrganization_ShouldReturn200() {
        VerifyOrganizationRequest request = mock(VerifyOrganizationRequest.class);
        OrganizationProfileResponse response = mock(OrganizationProfileResponse.class);
        when(organizationService.verifyOrganization(1L, request)).thenReturn(response);

        ResponseEntity<BaseResponse<OrganizationProfileResponse>> result = controller.verifyOrganization(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("verify hồ sơ thành công", result.getBody().getMessage());
        assertEquals(response, result.getBody().getData());
    }

    @Test
    void advancedSearch_ShouldReturn200() {
        Page<OrganizationProfileResponse> page = new PageImpl<>(Collections.emptyList());
        when(organizationService.advancedSearch(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<BasePageResponse<OrganizationProfileResponse>> result = 
            controller.advancedSearch(OrganizationStatus.PENDING, 1, "test", 1, 10, "createdAt", "DESC");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
