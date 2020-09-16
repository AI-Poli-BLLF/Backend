package it.polito.ai.virtuallabs.repositories.tokens;

import it.polito.ai.virtuallabs.entities.tokens.RegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, String> {
    List<RegistrationToken> findAllByExpiryDateBefore(Timestamp t);
}
