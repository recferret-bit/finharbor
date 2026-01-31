package com.casino.users.dto;

import com.casino.users.entity.User;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    private User.UserStatus status;
    
    private User.KycStatus kycStatus;
}
