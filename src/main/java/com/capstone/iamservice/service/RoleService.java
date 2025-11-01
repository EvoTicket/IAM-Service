package com.capstone.iamservice.service;

import com.capstone.iamservice.dto.response.RoleResponse;
import com.capstone.iamservice.entity.Role;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.RoleRepository;
import com.capstone.iamservice.util.RoleUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleUtil roleUtil;

    @Transactional
    public RoleResponse createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new AppException(ErrorCode.CONFLICT, "Role đã tồn tại với tên: " + name);
        }

        Role role = Role.builder()
                .name(name)
                .description(description)
                .build();

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    public RoleResponse getRoleById(Long id) {
        Role role = roleUtil.getRoleOrThrow(id);
        return mapToRoleResponse(role);
    }

    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(this::mapToRoleResponse);
    }

    @Transactional
    public RoleResponse updateRole(Long id, String name, String description) {
        Role role = roleUtil.getRoleOrThrow(id);

        if (!role.getName().equals(name) && roleRepository.existsByName(name)) {
            throw new AppException(ErrorCode.CONFLICT, "Role đã tồn tại với tên: " + name);
        }

        role.setName(name);
        role.setDescription(description);

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy role với ID: " + id);
        }
        roleRepository.deleteById(id);
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}