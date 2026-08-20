package first_project.recycle.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Member {
   private Long memberId;
   private String name;
   private String email;
   private String password;
   private String nickname;
   private String provider;
   private String role;
   private LocalDateTime createdAt;
}
