package com.casino.games.dto;

import com.casino.games.model.GameStatus;
import com.casino.games.model.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    private Long id;
    private String gameId;
    private String name;
    private String description;
    private GameType type;
    private GameStatus status;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private BigDecimal rtp;
    private String provider;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
