package com.lifelinkai.backend.dto;

import com.lifelinkai.backend.model.Role;

public class AuthResponse {
    private String token;
    private String id;
    private String name;
    private String email;
    private Role role;

    public AuthResponse(String token, String id, String name, String email, Role role) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
}
