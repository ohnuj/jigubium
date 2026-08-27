package first_project.recycle.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private String nickname;
    private String provider;
    private String role;
}
