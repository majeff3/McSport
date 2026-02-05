package org.example.mcsport.entity.req.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterReq {
    private String save_code;
    private String user_name;
    private String Encode_password;
}
