package com.casino.games.dto;

import com.casino.games.model.GameStatus;
import com.casino.games.model.GameType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRequest {
    
    @NotBlank(message = "Game ID is required")
    @Size(max = 100, message = "Game ID must not exceed 100 characters")
    private String gameId;
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Game type is required")
    private GameType type;
    
    @NotNull(message = "Status is required")
    private GameStatus status;
    
    @NotNull(message = "Minimum bet is required")
    @DecimalMin(value = "0.01", message = "Minimum bet must be at least 0.01")
    private BigDecimal minBet;
    
    @NotNull(message = "Maximum bet is required")
    @DecimalMin(value = "0.01", message = "Maximum bet must be at least 0.01")
    private BigDecimal maxBet;
    
    @DecimalMin(value = "0.0", message = "RTP must be at least 0.0")
    @DecimalMax(value = "100.0", message = "RTP must not exceed 100.0")
    private BigDecimal rtp;
    
    @Size(max = 255, message = "Provider must not exceed 255 characters")
    private String provider;
    
    @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
    private String thumbnailUrl;
}
