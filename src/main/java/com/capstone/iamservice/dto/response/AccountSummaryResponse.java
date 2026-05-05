package com.capstone.iamservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountSummaryResponse {
    private long totalAccounts;
    private long activeOrganizers;
    private long pendingApprovals;
    private long restrictedAccounts;
}
