package org.example.mcsport.controller;

import jakarta.annotation.Resource;
import org.example.mcsport.entity.req.user.LoginReq;
import org.example.mcsport.entity.req.user.RegisterReq;
import org.example.mcsport.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginReq req){
        return ResponseEntity.ok(userService.login(req.getLogin_name(), req.getPassword()));
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterReq req){
        return ResponseEntity.ok(userService.register(req.getSave_code(),
                req.getUser_name(), req.getEncode_password()));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/getAllUser")
    public ResponseEntity<Object> getAllUser(){
        return ResponseEntity.ok(userService.getAllUser());
    }

}
