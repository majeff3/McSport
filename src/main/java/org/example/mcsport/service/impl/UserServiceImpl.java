package org.example.mcsport.service.impl;

import jakarta.annotation.Resource;
import org.example.mcsport.entity.mariadb.UserTab;
import org.example.mcsport.repository.mariadb.UserRepository;
import org.example.mcsport.service.UserService;
import org.example.mcsport.service.impl.jwt.UserDetailsImpl;
import org.example.mcsport.util.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.relational.core.sql.In;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Object login(String login_name, String password) {
        // 先檢查用戶是否存在
        UserTab userTab = userRepository.findUserTabByName(login_name).orElse(null);
        if (userTab == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("msg", "用戶名不存在");
            return result;
        }

        // 再驗證密碼
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, userTab.getPassword())) {
            Map<String, Object> result = new HashMap<>();
            result.put("msg", "密碼錯誤");
            return result;
        }

        // 手動構建 Authentication
        UserDetailsImpl userDetails = UserDetailsImpl.build(userTab);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        userTab.setLastLoginTime(Instant.now());
        userRepository.save(userTab);

        String redisKey = "login_token:" + userDetails.getId();
        redisTemplate.opsForValue().set(redisKey, jwt, 1, TimeUnit.DAYS);

        Map<String, Object> result = new HashMap<>();
        result.put("jwt", jwt);
        result.put("author_level", userTab.getRoles());
        result.put("user_name", userTab.getName());
        return result;
    }

    @Override
    public Object register(String save_code, String user_name, String password) {
        Map<String, Object> result = new HashMap<>();
        if(!save_code.equals("1368")&&!save_code.equals("Jane&Antony")){
            result.put("msg","Fail to register");
            return result;
        }
        UserTab userTab = new UserTab();
        userTab.setName(user_name);
        if(save_code.equals("Jane&Antony")) {
            userTab.setRoles("ADMIN,USER");
        } else {
            userTab.setRoles("USER");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String encode_password = encoder.encode(password);

        userTab.setPassword(encode_password);
        userTab.setLastLoginTime(Instant.now());
        userRepository.save(userTab);
        result.put("msg","Registration success");
        return result;
    }

    @Override
    public Object getAllUser() {
        List<UserTab> allUser = userRepository.findAll();
        List<Map<String, Object>> userInfolist = new ArrayList<>();
        for(UserTab userTab : allUser){
            Map<String, Object> temp = new HashMap<>();
            temp.put("user_id", userTab.getId());
            temp.put("user_name", userTab.getName());
            userInfolist.add(temp);
        }
        return userInfolist;
    }
}
