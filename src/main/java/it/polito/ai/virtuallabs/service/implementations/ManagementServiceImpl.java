package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.security.repositories.UserRepository;
import it.polito.ai.virtuallabs.service.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ManagementServiceImpl implements ManagementService {

    @Autowired
    private UserRepository userRepository;

    /** questo service serve a ricavare ed eventualmente abilitare un utente
     * (che può essere un docente o uno studente)
     * a servizio del NotificationService
     **/

    private User getUser(String username){
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User %s not found", username))
        );
    }

    @Override
    public void enableUser(String username) {
        User user = getUser(username);
        user.setEnabled(true);
    }
}
