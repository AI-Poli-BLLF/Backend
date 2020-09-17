package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.TeamDTO;
import it.polito.ai.virtuallabs.dtos.TokenDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.tokens.RegistrationToken;
import it.polito.ai.virtuallabs.entities.tokens.Token;
import it.polito.ai.virtuallabs.repositories.tokens.TokenRepository;
import it.polito.ai.virtuallabs.repositories.tokens.RegistrationTokenRepository;
import it.polito.ai.virtuallabs.security.service.exceptions.UserAlreadyExistException;
import it.polito.ai.virtuallabs.service.*;
import it.polito.ai.virtuallabs.service.exceptions.InvalidOrExpiredTokenException;
import it.polito.ai.virtuallabs.service.exceptions.TeamNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.TokenNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class NotificationServiceImpl implements NotificationService {

    public static final String from = "applicazioni.internet.test@gmail.com";
    public static final String baseURL = "https://localhost:4200";
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private VMService vmService;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityGetter entityGetter;
    @Autowired
    private ManagementService managementService;

    @Override
    @Async
    public void sendMessage(String address, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo("applicazioni.internet.test@gmail.com");
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }

    @Override
    @Transactional
    public void confirm(String token) {
        Token t = entityGetter.getToken(token);
        Long teamId = t.getTeamId();
        //Se il token è scaduto non può confermare
        if(Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault())).compareTo(t.getExpiryDate()) >= 0)
            throw new InvalidOrExpiredTokenException(token);

        tokenRepository.delete(t);
        if(tokenRepository.countTokenByTeamId(teamId) == 0) {
            teamService.setTeamStatus(teamId, Team.Status.ACTIVE);
            Team team = entityGetter.getTeam(teamId);

            // todo: vedere che eccezioni possono essere generate
            //creo una configurazione di base per le vm
            vmService.createVMConfiguration(
                    new VMConfigDTO(0, 0, 0, 0, 0),
                    teamId,
                    team.getCourse().getName()
            );

            // cancella i team (inattivi) relativi allo stesso corso in cui compaiono membri
            // del gruppo appena attivato
            List<Long> teamsToEvict = teamService.evictPendingTeamsOfMembers(teamId);
            for (Long id : teamsToEvict) {
                tokenRepository.deleteAllByTeamId(id);
            }
        }
    }

    private void deleteTeamWithTokens(Long teamId){
        tokenRepository.deleteAllByTeamId(teamId);
        teamService.evictTeam(teamId);
    }

    @Override
    @Transactional
    public void reject(String token) {
        Token t = tokenRepository.findById(token).orElseThrow(()->new InvalidOrExpiredTokenException(token));
        Long teamId = t.getTeamId();
        //Se il token è scaduto non può rigettare
        if(Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault())).compareTo(t.getExpiryDate()) >= 0)
            throw new InvalidOrExpiredTokenException(token);

        deleteTeamWithTokens(teamId);
    }

    @Override
    @Transactional
    public void notifyTeam(TeamDTO team, List<String> memberIds, String proposerId, Integer timeout) {
        if(team == null || team.getId() == null)
            throw new TeamNotFoundException();

        Timestamp expiryDate = Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault()).plusHours(timeout));

        // Se nel gruppo c'è solo il proponente non vengono creati token e mandate mail e posso attivare il gruppo
        if (memberIds.size() == 1) {
            teamService.setTeamStatus(team.getId(), Team.Status.ACTIVE);
            // cancella i team (inattivi) relativi allo stesso corso in cui compare il membro
            // del gruppo appena attivato
            List<Long> teamsToEvict = teamService.evictPendingTeamsOfMembers(team.getId());
            /*for (Long id : teamsToEvict) {
                tokenRepository.deleteAllByTeamId(id);
            }*/
            return;
        }

        //todo: inviare mail di notifica per i membri invitati
        memberIds.stream().filter(id -> !id.equals(proposerId))
                .forEach(memberId -> createAndSaveToken(team.getId(), memberId, expiryDate));

    }

    private void createAndSaveToken(Long teamId, String studentId, Timestamp expiryDate) {
        Token token = new Token(UUID.randomUUID().toString(), teamId, studentId, expiryDate);
        tokenRepository.save(token);
    }

    private String buildMessage(String tokenId, String teamName, String memberId){
        String confirm = String.format("%s/notification/confirm/%s", baseURL, tokenId);
        String reject = String.format("%s/notification/reject/%s", baseURL, tokenId);

        return String.format("Congratulazioni %s!\nSei stato invitato a far parte del team %s.\n" +
                "Se sei interessato per favore conferma la tua partecipazione altrimenti puoi anche rifiutare l'invito immediatamente:\n\n" +
                "Accetta:\t%s\n\n" +
                "Rifiuta:\t%s\n", memberId, teamName, confirm, reject);
    }

    @Override
    @Transactional
    public void deleteExpiredToken() {
        List<Token> expiredTokens = tokenRepository
                .findAllByExpiryDateBefore(Timestamp.valueOf(LocalDateTime.now()));
        if(expiredTokens.isEmpty())
            return;

        Set<Long> teams = expiredTokens.stream().map(Token::getTeamId).collect(Collectors.toSet());
        Set<Token> toEliminate = new HashSet<>(expiredTokens);
        teams.forEach(t-> toEliminate.addAll(tokenRepository.findAllByTeamId(t)));

        tokenRepository.deleteAll(toEliminate);
        if(!teams.isEmpty())
            teams.forEach(teamService::evictTeam);
    }

    @Override
    public List<String> getPendingMemberIds(Long teamId) {
        return tokenRepository.findAllByTeamId(teamId).stream()
                .map(Token::getStudentId)
                .collect(Collectors.toList());
    }

    @PreAuthorize("@securityApiAuth.isMe(#memberId)")
    @Override
    public TokenDTO getPendingMemberToken(Long teamId, String memberId) {
        Token token = tokenRepository
                .findOneByTeamIdAndStudentId(teamId, memberId)
                .orElseThrow(()-> new TokenNotFoundException(teamId, memberId));

        return mapper.map(token, TokenDTO.class);
    }


    /* REGISTRATION EMAIL */
    @Override
    @Transactional
    @Async
    public void sendConfirmEmailRegistration(String email, String userFirstName, String userLastName){
        if(!email.matches("[sSdD][0-9]{6}@(studenti\\.)?polito\\.it"))
            return;

        RegistrationToken t = new RegistrationToken(email, userFirstName, userLastName);
        registrationTokenRepository.save(t);
        buildAndSendRegistrationMessage(email, userFirstName, userLastName, t.getId());
    }

    public void buildAndSendRegistrationMessage(String email, String userFirstName, String userLastName, String tokenId){
        String subject = "[Virtual Labs] Conferma la tua registrazione";
        String body = String.format(
                "Benvenuto %s %s,\n" +
                "ti confermiamo che il tuo account è stato creato con successo nella piattaforma Virtual Labs.\n\n" +
                "Per cominciare ad usare la piattaforma clicca su link qui sotto per confermare la tua registrazione:\n\n" +
                "%s/confirm-registration/%s\n\n" +
                "Se non sei stato tu a registrarti tra 5 ore l'account verrà automaticamente cancellato.\n",
                userFirstName,
                userLastName,
                baseURL,
                tokenId
        );
        sendMessage(email, subject, body);
    }

    @Override
    @Transactional
    public void confirmRegistration(String token) {
        RegistrationToken t = entityGetter.getRegistrationToken(token);
        String id = t.getUserId().split("@")[0];
        switch (t.getUserId().toLowerCase().toCharArray()[0]){
            case 's':
                StudentDTO s = new StudentDTO(id, t.getUserLastName(), t.getUserFirstName());
                if (!teamService.addStudent(s))
                    throw new UserAlreadyExistException();
                break;
            case 'd':
                ProfessorDTO p = new ProfessorDTO(id, t.getUserLastName(), t.getUserFirstName(), new ArrayList<>());
                if (!teamService.addProfessor(p))
                    throw new UserAlreadyExistException();
                break;
            default:
                //non dovrebbe mai arrivarci perchè c'è il controllo del pattern su userId
                throw new UserAlreadyExistException();
        }

        managementService.enableUser(t.getUserId());
        registrationTokenRepository.delete(t);
    }
}
