package com.casino.games.controller;

import com.casino.games.dto.GameRequest;
import com.casino.games.dto.GameResponse;
import com.casino.games.model.GameStatus;
import com.casino.games.model.GameType;
import com.casino.games.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Casino Games Management API")
public class GameController {
    
    private final GameService gameService;
    
    @PostMapping
    @Operation(summary = "Create a new game", description = "Creates a new casino game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Game created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Game with gameId already exists")
    })
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody GameRequest request) {
        GameResponse response = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get game by ID", description = "Retrieves a game by its database ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game found"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    public ResponseEntity<GameResponse> getGameById(
            @Parameter(description = "Game database ID") @PathVariable Long id) {
        GameResponse response = gameService.getGameById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/game-id/{gameId}")
    @Operation(summary = "Get game by gameId", description = "Retrieves a game by its unique gameId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game found"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    public ResponseEntity<GameResponse> getGameByGameId(
            @Parameter(description = "Unique game identifier") @PathVariable String gameId) {
        GameResponse response = gameService.getGameByGameId(gameId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all games", description = "Retrieves all games with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Games retrieved successfully")
    })
    public ResponseEntity<List<GameResponse>> getAllGames(
            @Parameter(description = "Filter by game type") @RequestParam(required = false) GameType type,
            @Parameter(description = "Filter by game status") @RequestParam(required = false) GameStatus status,
            @Parameter(description = "Search query for name or description") @RequestParam(required = false) String search) {
        
        List<GameResponse> games;
        
        if (search != null && !search.isEmpty()) {
            games = gameService.searchGames(search);
        } else if (type != null && status != null) {
            games = gameService.getAllGames().stream()
                    .filter(g -> g.getType() == type && g.getStatus() == status)
                    .toList();
        } else if (type != null) {
            games = gameService.getGamesByType(type);
        } else if (status != null) {
            games = gameService.getGamesByStatus(status);
        } else {
            games = gameService.getAllGames();
        }
        
        return ResponseEntity.ok(games);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a game", description = "Updates an existing game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Game not found"),
            @ApiResponse(responseCode = "409", description = "Game with gameId already exists")
    })
    public ResponseEntity<GameResponse> updateGame(
            @Parameter(description = "Game database ID") @PathVariable Long id,
            @Valid @RequestBody GameRequest request) {
        GameResponse response = gameService.updateGame(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a game", description = "Deletes a game by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Game deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    public ResponseEntity<Void> deleteGame(
            @Parameter(description = "Game database ID") @PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
