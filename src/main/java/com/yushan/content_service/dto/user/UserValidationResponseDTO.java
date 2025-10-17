package com.yushan.content_service.dto.user;

/**
 * DTO for user validation response from User Service.
 * Contains user information after JWT validation.
 */
public class UserValidationResponseDTO {
    private boolean valid;
    private String userId;
    private String username;
    private String authorName;
    private String role;
    private String message;

    // Constructors
    public UserValidationResponseDTO() {
    }

    public UserValidationResponseDTO(boolean valid, String userId, String username, String authorName, String role, String message) {
        this.valid = valid;
        this.userId = userId;
        this.username = username;
        this.authorName = authorName;
        this.role = role;
        this.message = message;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UserValidationResponseDTO{" +
                "valid=" + valid +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", authorName='" + authorName + '\'' +
                ", role='" + role + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
