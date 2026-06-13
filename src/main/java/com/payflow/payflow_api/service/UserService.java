package com.payflow.payflow_api.service;


import com.payflow.payflow_api.entity.User;
import com.payflow.payflow_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    // Spring Boot scans for @Repository beans and injects them here automatically.
    // At startup, it creates a proxy implementation of UserRepository and wires it into this field.

    @Autowired
    private UserRepository userRepository;
    // Spring Boot automatically injects the UserRepository bean at startup

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUpiId(String upiId) {
        return userRepository.findByUpiId(upiId);
    }
}
