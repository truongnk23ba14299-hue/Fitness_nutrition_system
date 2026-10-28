package com.fitness.service;

import com.example.demo.dto.RegisterResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService {

    private final RegisterUserRepository registeruserRepository;

    public RegisterService(RegisterUserRepository registeruserRepository) {
        this.registeruserRepository = registeruserRepository;
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

        user.setPassword(request.password());

        user.setRole("USER");

        User savedUser = registeruserRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getRole()
        );
    }
}