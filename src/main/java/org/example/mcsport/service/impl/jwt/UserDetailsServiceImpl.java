package org.example.mcsport.service.impl.jwt;

import org.example.mcsport.entity.mariadb.UserTab;
import org.example.mcsport.repository.mariadb.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login_name) throws UsernameNotFoundException {
        UserTab userTab = userRepository.findUserTabByName(login_name)
                .orElseThrow(() -> new UsernameNotFoundException("Operator Not Found with login_name: " + login_name));
        return UserDetailsImpl.build(userTab);
    }

}