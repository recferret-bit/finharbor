package com.casino.users.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private UUID userId;
    private String email;
    private String status;
    private String kycStatus;
    private LocalDateTime updatedAt;
}
