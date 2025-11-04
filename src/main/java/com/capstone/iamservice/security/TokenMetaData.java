package com.capstone.iamservice.security;

public record TokenMetaData(Long userId, boolean isOrganization, Long organizationId) {
}
