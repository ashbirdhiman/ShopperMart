package com.shoppermart.userservice.controller;

import com.shoppermart.userservice.model.User;
import com.shoppermart.userservice.service.UserService;
import com.shoppermart.userservice.GlobalException.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        log.info("Fetching user with ID: {}", userId);
        Optional<User> user = userService.getUserById(userId);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Update user by ID
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
            @RequestBody User updatedUser) {
        log.info("Updating user with ID: {}", userId);
        
        Optional<User> user = userService.updateUser(userId, updatedUser);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    /**
     * Delete user by ID
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        log.info("Deleting user with ID: {}", userId);
        
        boolean isDeleted = userService.deleteUser(userId);
        
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    @GetMapping("/getEmail/{userId}")
    public String getEmailByUserId(@PathVariable String userId){

        String email=userService.getEmailByUserId(userId);

        if(email!=null){
            return email;
        }
        else throw  new ResourceNotFoundException("User not found with ID: " + userId);

    }

}

