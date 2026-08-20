package first_project.recycle.domain;

import lombok.Data;

@Data
public class MemberInfo {
    private String nickname;
    private String password;
    private  String newpassword;
    private String newpasswordconfirm;

}
