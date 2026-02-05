package org.example.mcsport.service;

public interface UserService {
    Object login(String login_name, String password);

    Object register(String save_code, String user_name, String Encode_password);

    Object getAllUser();
}
