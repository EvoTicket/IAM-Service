package com.capstone.iamservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AccountDetailResponse {
    private Long id;
    private String name;
    private String type; // "Organizer" | "Buyer"
    private String status; // "Active" | "Pending Approval" | "Restricted" | "Suspended"
    private String registeredDate; // dd/MM/yyyy
    private Stats stats;
    private Profile profile;
    private List<DocumentDto> documents;
    private PayoutAccount payoutAccount;
    private OperationalContext operationalContext;
    private AdminContext adminContext;

    @Data
    @Builder
    public static class Stats {
        private String verificationStatus;
        private int documentsSubmitted;
        private int totalDocuments;
        private int eventsCreated;
        private int pendingEvents;
        private String lastActive;
        private String riskLevel;
        private String riskReason;
    }

    @Data
    @Builder
    public static class Profile {
        private String representative;
        private String orgType;
        private String email;
        private String phone;
        private String taxId;
        private String address;
    }

    @Data
    @Builder
    public static class DocumentDto {
        private String name;
        private String status; // "Verified" | "Pending" | "Missing" | "Rejected"
        private String url;
    }

    @Data
    @Builder
    public static class PayoutAccount {
        private String bank;
        private String accountNumber;
        private String accountName;
        private String status;
    }

    @Data
    @Builder
    public static class OperationalContext {
        private Summary summary;
        private List<EventSummaryDto> recentEvents;
        private List<AdminLogDto> adminLogs;
        private List<ActivityDto> activities;
        private List<ProcessingHistoryDto> history;
    }

    @Data
    @Builder
    public static class Summary {
        private int recentEventsCount;
        private int flaggedEventsCount;
        private int pendingPayoutCount;
        private int openSupportNotesCount;
    }

    @Data
    @Builder
    public static class EventSummaryDto {
        private String id;
        private String name;
        private String date;
        private String status;
        private boolean isFlagged;
    }

    @Data
    @Builder
    public static class AdminLogDto {
        private String user;
        private String timestamp;
        private String content;
        private String role;
    }

    @Data
    @Builder
    public static class ActivityDto {
        private String timestamp;
        private String title;
        private String description;
        private String icon;
    }

    @Data
    @Builder
    public static class ProcessingHistoryDto {
        private String timestamp;
        private String actor;
        private String action;
        private String note;
        private String actionType;
    }

    @Data
    @Builder
    public static class AdminContext {
        private String internalNote;
        private LastAction lastAction;
    }

    @Data
    @Builder
    public static class LastAction {
        private String adminUser;
        private String timestamp;
        private String description;
    }
}
