package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.TeamDTO;
import it.polito.ai.virtuallabs.dtos.TokenDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.Token;
import it.polito.ai.virtuallabs.repositories.TokenRepository;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.VMService;
import it.polito.ai.virtuallabs.service.exceptions.TeamNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.InvalidOrExpiredTokenException;
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
    public static final String baseURL = "http://localhost:8080";
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private VMService vmService;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityGetter entityGetter;

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
        Token t = tokenRepository.findById(token).orElseThrow(()-> new InvalidOrExpiredTokenException(token));
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

        for (String id : memberIds){
            if (!id.equals(proposerId)) {
                Token token = createAndSaveToken(UUID.randomUUID().toString(), team.getId(), id, expiryDate);
                //String message = buildMessage(token.getId(), team.getName(), id);
                //String email = String.format("%s@studenti.polito.it", id); //The <s> is included in the id
                //sendMessage(email, "Gruppo Applicazioni Internet", message);
            }
        }

        // Se nel gruppo c'è solo il proponente non vengono mandate mail e posso attivare il gruppo
        if (memberIds.size() == 1) {
            teamService.setTeamStatus(team.getId(), Team.Status.ACTIVE);
            // cancella i team (inattivi) relativi allo stesso corso in cui compare il membro
            // del gruppo appena attivato
            List<Long> teamsToEvict = teamService.evictPendingTeamsOfMembers(team.getId());
            for (Long id : teamsToEvict) {
                tokenRepository.deleteAllByTeamId(id);
            }
        }
}

    private Token createAndSaveToken(String tokenId, Long teamId, String studentId, Timestamp expiryDate) {
        Token token = new Token(tokenId, teamId, studentId, expiryDate);
        tokenRepository.save(token);
        return token;
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
                .findAllByExpiryDateBefore(Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault())));
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
                .map(t -> t.getStudentId())
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
}
