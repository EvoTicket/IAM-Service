package com.capstone.iamservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {

    private String sub;   // Google user ID duy nhất

    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    private String name;        // Full name

    @JsonProperty("given_name")
    private String givenName;   // First name

    @JsonProperty("family_name")
    private String familyName;  // Last name

    private String picture;     // Avatar URL
}
