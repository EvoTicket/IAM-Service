package com.capstone.iamservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WelcomeEvent {
    Long userId;
    String email;
    String fullName;
    String username;
}
