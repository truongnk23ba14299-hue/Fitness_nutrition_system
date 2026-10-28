package com.fitness.service;

import com.fitness.dto.RegisterResponse;
import com.fitness.dto.RegisterRequest;
import com.fitness.model.User;
import com.fitness.repository.RegisterUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService {

    private final RegisterUserRepository registeruserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(RegisterUserRepository registeruserRepository) {
        this.registeruserRepository = registeruserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (registeruserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email đã tồn tại"
            );
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);

         user.setPassword(passwordEncoder.encode(request.password()));

        user.setRole("USER");

        User savedUser = registeruserRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }
}