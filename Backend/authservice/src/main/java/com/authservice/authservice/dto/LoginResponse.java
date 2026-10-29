package com.authservice.authservice.dto;

public class LoginResponse {

    private String token;
    private Long userid;
    private String username;
    private String role;

    public LoginResponse() {}

    public LoginResponse(String token, Long userid, String username, String role) {
        this.token = token;
        this.userid = userid;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserid() { return userid; }
    public void setUserid(Long userid) { this.userid = userid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}