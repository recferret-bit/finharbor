package com.casino.games.service;

import com.casino.games.dto.GameRequest;
import com.casino.games.dto.GameResponse;
import com.casino.games.model.Game;
import com.casino.games.model.GameStatus;
import com.casino.games.model.GameType;
import com.casino.games.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    
    private final GameRepository gameRepository;
    
    @Transactional
    public GameResponse createGame(GameRequest request) {
        log.info("Creating game with gameId: {}", request.getGameId());
        
        if (gameRepository.existsByGameId(request.getGameId())) {
            throw new IllegalArgumentException("Game with gameId " + request.getGameId() + " already exists");
        }
        
        Game game = Game.builder()
                .gameId(request.getGameId())
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .status(request.getStatus())
                .minBet(request.getMinBet())
                .maxBet(request.getMaxBet())
                .rtp(request.getRtp())
                .provider(request.getProvider())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();
        
        Game savedGame = gameRepository.save(game);
        log.info("Game created successfully with id: {}", savedGame.getId());
        
        return mapToResponse(savedGame);
    }
    
    @Transactional(readOnly = true)
    public GameResponse getGameById(Long id) {
        log.info("Fetching game with id: {}", id);
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + id));
        return mapToResponse(game);
    }
    
    @Transactional(readOnly = true)
    public GameResponse getGameByGameId(String gameId) {
        log.info("Fetching game with gameId: {}", gameId);
        Game game = gameRepository.findByGameId(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with gameId: " + gameId));
        return mapToResponse(game);
    }
    
    @Transactional(readOnly = true)
    public List<GameResponse> getAllGames() {
        log.info("Fetching all games");
        return gameRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GameResponse> getGamesByType(GameType type) {
        log.info("Fetching games by type: {}", type);
        return gameRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GameResponse> getGamesByStatus(GameStatus status) {
        log.info("Fetching games by status: {}", status);
        return gameRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GameResponse> searchGames(String query) {
        log.info("Searching games with query: {}", query);
        return gameRepository.searchByNameOrDescription(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public GameResponse updateGame(Long id, GameRequest request) {
        log.info("Updating game with id: {}", id);
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + id));
        
        // Check if gameId is being changed and if new gameId already exists
        if (!game.getGameId().equals(request.getGameId()) && 
            gameRepository.existsByGameId(request.getGameId())) {
            throw new IllegalArgumentException("Game with gameId " + request.getGameId() + " already exists");
        }
        
        game.setGameId(request.getGameId());
        game.setName(request.getName());
        game.setDescription(request.getDescription());
        game.setType(request.getType());
        game.setStatus(request.getStatus());
        game.setMinBet(request.getMinBet());
        game.setMaxBet(request.getMaxBet());
        game.setRtp(request.getRtp());
        game.setProvider(request.getProvider());
        game.setThumbnailUrl(request.getThumbnailUrl());
        
        Game updatedGame = gameRepository.save(game);
        log.info("Game updated successfully with id: {}", updatedGame.getId());
        
        return mapToResponse(updatedGame);
    }
    
    @Transactional
    public void deleteGame(Long id) {
        log.info("Deleting game with id: {}", id);
        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Game not found with id: " + id);
        }
        gameRepository.deleteById(id);
        log.info("Game deleted successfully with id: {}", id);
    }
    
    private GameResponse mapToResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .gameId(game.getGameId())
                .name(game.getName())
                .description(game.getDescription())
                .type(game.getType())
                .status(game.getStatus())
                .minBet(game.getMinBet())
                .maxBet(game.getMaxBet())
                .rtp(game.getRtp())
                .provider(game.getProvider())
                .thumbnailUrl(game.getThumbnailUrl())
                .createdAt(game.getCreatedAt())
                .updatedAt(game.getUpdatedAt())
                .build();
    }
}
