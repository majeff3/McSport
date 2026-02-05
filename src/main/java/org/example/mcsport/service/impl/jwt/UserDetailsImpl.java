package org.example.mcsport.service.impl.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.example.mcsport.entity.mariadb.UserTab;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private Long user_id;

    private String username;
    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long user_id, String username, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(UserTab userTab) {

        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleList = userTab.getRoles();
        List<String> roles = List.of(roleList.split(","));

        for(String role : roles){
            authorities.add(new SimpleGrantedAuthority("ROLE_"+role));//重中之重，所有人的职责都是USER
        }

        return new UserDetailsImpl(
                userTab.getId(),
                userTab.getName(),
                userTab.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public Long getId() {
        return user_id;
    }
}