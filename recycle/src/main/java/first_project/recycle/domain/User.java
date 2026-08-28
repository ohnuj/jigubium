package first_project.recycle.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long memberId;
    private String name;
    private String email;
    private String password;
    private String nickname;
    private String provider;
    private String providerId;
    private String role;
    private Integer point;
    private LocalDateTime createdAt;
    private boolean isNewUser;
}
