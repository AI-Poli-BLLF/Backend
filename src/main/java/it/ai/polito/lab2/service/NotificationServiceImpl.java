package it.ai.polito.lab2.service;

import it.ai.polito.lab2.dtos.TeamDTO;
import it.ai.polito.lab2.entities.Team;
import it.ai.polito.lab2.entities.Token;
import it.ai.polito.lab2.repositories.TokenRepository;
import it.ai.polito.lab2.service.exceptions.TeamNotFoundException;
import it.ai.polito.lab2.service.exceptions.InvalidOrExpiredTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class NotificationServiceImpl implements NotificationService{

    public static final String from = "applicazioni.internet.test@gmail.com";
    public static final String baseURL = "http://localhost:8080";
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TeamService teamService;

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
    @Async
    public void notifyTeam(TeamDTO team, List<String> memberIds) {
        if(team == null || team.getId() == null)
            throw new TeamNotFoundException();

        Timestamp expiryDate = Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault()).plusHours(1));

        for(String id : memberIds){
            Token token = createAndSaveToken(UUID.randomUUID().toString(), team.getId(), expiryDate);
            String message = buildMessage(token.getId(), team.getName(), id);
            String email = String.format("%s@studenti.polito.it", id); //The <s> is included in the id
            sendMessage(email, "Gruppo Applicazioni Internet", message);
        }
}

    private Token createAndSaveToken(String tokenId, Long teamId, Timestamp expiryDate) {
        Token token = new Token(tokenId, teamId, expiryDate);
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
}
