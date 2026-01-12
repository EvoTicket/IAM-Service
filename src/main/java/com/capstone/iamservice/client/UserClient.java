package com.capstone.iamservice.client;

import com.capstone.iamservice.dto.response.UserClientResponse;
import com.capstone.iamservice.entity.User;
import com.capstone.iamservice.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/users")
@RequiredArgsConstructor
public class UserClient {
    private final UserUtil userUtil;

    @GetMapping("/{id}")
    public ResponseEntity<UserClientResponse> getUserById(@PathVariable Long id) {
        User user = userUtil.getUserOrThrow(id);
        UserClientResponse response = UserClientResponse.builder()
                .userFullName(user.getFullName())
                .userAvatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(response);
    }
}
