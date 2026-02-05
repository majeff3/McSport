package org.example.mcsport.entity.req.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReq {
    private String login_name;
    private String password;
}
