package com.fitness.service;

import com.fitness.model.User;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        // Tìm user bằng cả Email hoặc Username
        return userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(input, input)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy người dùng với thông tin: " + input
                ));
    }
}