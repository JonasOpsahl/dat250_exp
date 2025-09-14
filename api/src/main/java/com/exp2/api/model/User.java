package com.exp2.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    
    private String username;
    private String email;
    private Integer userId;
    private String password;
    

    public User() {
        
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public String getUsername() {
        return this.username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }
}
