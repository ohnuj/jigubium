package first_project.recycle.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long memberId;
    private String name;
    private String email;
    private String password;
    private String nickname;
    private String provider;
    private String providerId;
    private String role;
    private LocalDateTime createdAt;
    private boolean isNewUser;
}
