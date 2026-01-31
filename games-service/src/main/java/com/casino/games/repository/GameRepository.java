package com.casino.games.repository;

import com.casino.games.model.Game;
import com.casino.games.model.GameStatus;
import com.casino.games.model.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    Optional<Game> findByGameId(String gameId);
    
    List<Game> findByType(GameType type);
    
    List<Game> findByStatus(GameStatus status);
    
    List<Game> findByTypeAndStatus(GameType type, GameStatus status);
    
    @Query("SELECT g FROM Game g WHERE g.name LIKE %:name% OR g.description LIKE %:name%")
    List<Game> searchByNameOrDescription(@Param("name") String name);
    
    boolean existsByGameId(String gameId);
}
