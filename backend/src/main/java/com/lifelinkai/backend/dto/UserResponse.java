package com.lifelinkai.backend.dto;

import com.lifelinkai.backend.model.Role;

import java.time.LocalDateTime;

public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;
    
    public UserResponse(String id, String name, String email, String phone, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
