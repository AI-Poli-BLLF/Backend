package it.ai.polito.lab2.repositories;

import it.ai.polito.lab2.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    //Seleziona i token scaduti
    List<Token> findAllByExpiryDateBefore(Timestamp t);
    //Seleziona quelli legati ad un team
    List<Token> findAllByTeamId(Long teamId);
    void deleteAllByTeamId(Long teamId);
    int countTokenByTeamId(Long TeamId);
}
