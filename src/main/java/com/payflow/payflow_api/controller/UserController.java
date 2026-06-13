package com.payflow.payflow_api.controller;


import com.payflow.payflow_api.entity.User;
import com.payflow.payflow_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Register a new user
    @PostMapping
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Get user by UPI ID
    @GetMapping("/upi/{upiId}")
    public User getUserByUpiId(@PathVariable String upiId) {
        return userService.findByUpiId(upiId);
    }
}

