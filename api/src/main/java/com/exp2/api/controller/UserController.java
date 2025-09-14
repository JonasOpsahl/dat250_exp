package com.exp2.api.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exp2.api.model.User;
import com.exp2.api.service.PollManager;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {
    
    private PollManager pollManager;

    public UserController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    @RequestMapping
    public List<User> getUsers() {
        return pollManager.getUsers();

    }

    @RequestMapping("/{id}")
    public User getUser(@PathVariable Integer id) {
        return pollManager.getUser(id);
    }

    @PostMapping
    public User createUser(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
        return pollManager.createUser(username, email, password);
    }
    
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Integer id, @RequestParam Optional<String> username, @RequestParam Optional<String> email, @RequestParam Optional<String> password) {
        return pollManager.updateUser(id, username, email, password);
    }

    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Integer id) {
        return pollManager.deleteUser(id);
    }

}
