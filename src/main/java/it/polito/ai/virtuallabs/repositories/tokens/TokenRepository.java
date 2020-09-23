package it.polito.ai.virtuallabs.repositories.tokens;

import it.polito.ai.virtuallabs.entities.tokens.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    //Seleziona i token scaduti
    List<Token> findAllByExpiryDateBefore(Timestamp t);
    //Seleziona quelli legati ad un team
    List<Token> findAllByTeamId(Long teamId);
    void deleteAllByTeamId(Long teamId);
    int countTokenByTeamId(Long TeamId);
    Optional<Token> findOneByTeamIdAndStudentId(Long teamId, String studentId);
    List<Token> findByStudentIdOrderByExpiryDateDesc(String studentId);
}
