package com.backend.Backend.service;


import com.backend.Backend.model.User;
import com.backend.Backend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {

    @Autowired
    private  UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User myuser = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.withUsername(myuser.getUsername())
                .password(myuser.getPassword())     // encoded password
                .roles(myuser.getRole())// ADMIN / USER
                .build();
    }
}
