package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.TeamDTO;
import it.polito.ai.virtuallabs.dtos.tokens.NotificationTokenDTO;
import it.polito.ai.virtuallabs.dtos.tokens.TokenDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.entities.Course;
import it.polito.ai.virtuallabs.entities.Professor;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.tokens.NotificationToken;
import it.polito.ai.virtuallabs.entities.tokens.RegistrationToken;
import it.polito.ai.virtuallabs.entities.tokens.Token;
import it.polito.ai.virtuallabs.repositories.tokens.NotificationTokenRepository;
import it.polito.ai.virtuallabs.repositories.tokens.RegistrationTokenRepository;
import it.polito.ai.virtuallabs.repositories.tokens.TokenRepository;
import it.polito.ai.virtuallabs.security.service.exceptions.UserAlreadyExistException;
import it.polito.ai.virtuallabs.service.*;
import it.polito.ai.virtuallabs.service.exceptions.*;
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
    @Autowired
    private NotificationTokenRepository notificationTokenRepository;


    // dato un indirizzo email e un body, invia un messaggio email.
    // i parametri sono settati nell'application.properties
    @Override
    @Async
    public void sendMessage(String address, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
//        message.setTo("applicazioni.internet.test@gmail.com");
        message.setTo("applicazioni.internet.spam@gmail.com");
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }

    // dato un token, esegue i controlli di validità, lo elimina
    // esegue il controllo per team, se è l'ultima conferma creerà il team per un determinato corso
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

    // gestisce il rifiuto di uno studente a partecipare a un team
    // solo se il suo token è ancora valido ovviamente
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

    // elimina tutti i token pendenti per il team e lo smonta
    private void deleteTeamWithTokens(Long teamId){
        tokenRepository.deleteAllByTeamId(teamId);
        teamService.evictTeam(teamId);
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

    // funzione che parte in automatico come task schedulato
    // in presenza di token scaduti provvede ad eliminarli
    // chiamando le funzioni viste in precedenza (evictTeam)
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

    // restituisce la lista degli studenti invitati ma che non hanno ancora né accettato né rifiutato
    @Override
    public List<String> getPendingMemberIds(Long teamId) {
        return tokenRepository.findAllByTeamId(teamId).stream()
                .map(Token::getStudentId)
                .collect(Collectors.toList());
    }

    // restituisce allo studente il suo token pendente
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

    // conferma la registrazione di uno user, sia esso studente o docente
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

    // lo studente può richiedere a uno dei prof del corso di iscriversi al corso
    // al docente viene inviata una notifica con un token
    @PreAuthorize("@securityApiAuth.isMe(#studentId)")
    @Override
    @Transactional
    public void requestForCourseEnrolling(String studentId, String courseName) {
        Course c = entityGetter.getCourse(courseName);
        Student s = entityGetter.getStudent(studentId);

        if(c.getStudents().contains(s))
            throw new StudentAlreadyEnrolledToCourseException(studentId, courseName);

        String message = String.format("" +
                "Lo studente %s %s (%s) ha richiesto di poter essere iscritto al corso %s",
                s.getFirstName(), s.getName(), s.getId(), courseName
        );
        List<Professor> professors = c.getProfessors();
        List<NotificationToken> tokens = professors.stream()
                .filter(p-> !isSameTokenPresent(studentId, p.getId(), courseName))
                .map(p -> new NotificationToken(studentId, p.getId(), courseName, message, NotificationToken.NotificationType.STUDENT_ENROLLING))
                .collect(Collectors.toList());

        if(!tokens.isEmpty())
            notificationTokenRepository.saveAll(tokens);
    }

    // un professore può chiedere a un collega di gestire insieme un corso
    // allora uno può chiedere, tramite notifica e token, all'altro di cooperare
    @PreAuthorize("@securityApiAuth.isMe(#senderProfessorId)")
    @Override
    @Transactional
    public void cooperateWithProfessor(String senderProfessorId, List<String> receiverProfessorIds, String courseName) {
        Course c = entityGetter.getCourse(courseName);
        Professor sender = entityGetter.getProfessor(senderProfessorId);
        List<Professor> receivers = receiverProfessorIds.stream().map(entityGetter::getProfessor)
                .collect(Collectors.toList());

        if(!c.getProfessors().contains(sender))
            throw new ProfessorNotOwnCourseException(senderProfessorId, courseName);

        String message = String.format("Il professor %s %s (%s) ha richiesto la tua collaborazione per il corso %s",
                sender.getName(), sender.getFirstName(), senderProfessorId, courseName);
        List<NotificationToken> notificationTokens = receivers.stream()
                .filter(p-> !isSameTokenPresent(senderProfessorId, p.getId(), courseName))
                .map(p->{
                    if(c.getProfessors().contains(p))
                        throw new ProfessorAlreadyOwnCourseException(p.getId(), courseName);

                    return new NotificationToken(senderProfessorId, p.getId(), courseName, message, NotificationToken.NotificationType.PROFESSOR_COOPERATION);
                }).collect(Collectors.toList());

        if(!notificationTokens.isEmpty())
            notificationTokenRepository.saveAll(notificationTokens);
    }

    // L'user legge le notifiche, settando la notifica come letta
    @PreAuthorize("@securityApiAuth.ownNotification(#tokenId)")
    @Override
    @Transactional
    public void readNotification(String tokenId) {
        NotificationToken notificationToken = entityGetter.getNotificationToken(tokenId);
        notificationToken.setNotificationRead(true);
    }

    // permette al docente di accettare la richiesta di uno studente di iscriversi a un corso tramite notifica
    @PreAuthorize("@securityApiAuth.ownNotification(#tokenId)")
    @Override
    @Transactional
    public void acceptEnrollingRequest(String tokenId) {
        NotificationToken notificationToken = entityGetter.getNotificationToken(tokenId);

        if(notificationToken.getType() != NotificationToken.NotificationType.STUDENT_ENROLLING)
            throw new InvalidOrExpiredTokenException(tokenId);

        Student sender = entityGetter.getStudent(notificationToken.getSenderId());
        Course c = entityGetter.getCourse(notificationToken.getCourseName());

        //Cancella il token di richiesta di iscrizione
        notificationTokenRepository.delete(notificationToken);
        notificationTokenRepository.deleteBySenderIdAndCourseName(sender.getId(), c.getName());

        teamService.addStudentToCourse(sender.getId(), c.getName());

        String message = String.format("La tua richiesta di iscrizione al corso %s è stata accettata", notificationToken.getCourseName());

        NotificationToken answer = new NotificationToken(
                notificationToken.getReceiverId(),
                notificationToken.getSenderId(),
                notificationToken.getCourseName(),
                message,
                NotificationToken.NotificationType.RESPONSE
                );

        notificationTokenRepository.save(answer);
    }

    // permette al docente di rifiutare la richiesta di uno studente di iscriversi a un corso tramite notifica
    @PreAuthorize("@securityApiAuth.ownNotification(#tokenId)")
    @Override
    @Transactional
    public void rejectEnrollingRequest(String tokenId) {
        NotificationToken notificationToken = entityGetter.getNotificationToken(tokenId);

        if(notificationToken.getType() != NotificationToken.NotificationType.STUDENT_ENROLLING)
            throw new InvalidOrExpiredTokenException(tokenId);

        Student sender = entityGetter.getStudent(notificationToken.getSenderId());
        Course c = entityGetter.getCourse(notificationToken.getCourseName());

        //Cancella il token di richiesta di iscrizione
        notificationTokenRepository.delete(notificationToken);
        notificationTokenRepository.deleteBySenderIdAndCourseName(sender.getId(), c.getName());

        String message = String.format("La tua richiesta di iscrizione al corso %s è stata rifiutata", notificationToken.getCourseName());
        NotificationToken answer = new NotificationToken(
                notificationToken.getReceiverId(),
                notificationToken.getSenderId(),
                notificationToken.getCourseName(),
                message,
                NotificationToken.NotificationType.RESPONSE
        );

        notificationTokenRepository.save(answer);
    }

    // permette al docente di accettare la richiesta di un altro docente di cooperare per un
    // determinato corso tramite notifica
    @PreAuthorize("@securityApiAuth.ownNotification(#tokenId)")
    @Transactional
    @Override
    public void acceptCooperation(String tokenId) {
        NotificationToken notificationToken = entityGetter.getNotificationToken(tokenId);

        if(notificationToken.getType() != NotificationToken.NotificationType.PROFESSOR_COOPERATION)
            throw new InvalidOrExpiredTokenException(tokenId);

        Professor sender = entityGetter.getProfessor(notificationToken.getSenderId());
        Professor receiver = entityGetter.getProfessor(notificationToken.getReceiverId());
        Course c = entityGetter.getCourse(notificationToken.getCourseName());

        if(!c.getProfessors().contains(sender))
            throw new ProfessorNotOwnCourseException(sender.getId(), c.getName());

        String messageToSender = String.format("Il professor %s %s (%s) ha accettato " +
                "la tua richiesta di collaborazione per il corso %s",
                receiver.getFirstName(), receiver.getName(), receiver.getId(), c.getName());

        String messageToOtherProfessors = String.format("Il professor %s %s (%s) ha cominciato a collaborare per il corso %s",
                receiver.getFirstName(), receiver.getName(), receiver.getId(), c.getName());

        //Messaggio per il professore che ha fatto l'invito
        NotificationToken answerToSender = new NotificationToken(
                receiver.getId(),
                sender.getId(),
                c.getName(),
                messageToSender,
                NotificationToken.NotificationType.RESPONSE
        );
        //Messaggi per i professori appartenenti al corso tranne quello che ha fatto l'invito
        List<NotificationToken> answers = c.getProfessors().stream().filter(p -> !p.equals(sender))
                .map(p-> new NotificationToken(
                        receiver.getId(),
                        p.getId(),
                        c.getName(),
                        messageToOtherProfessors,
                        NotificationToken.NotificationType.RESPONSE
                )).collect(Collectors.toList());

        //Metto tutti i token assieme e li salvo nel db
        answers.add(answerToSender);
        notificationTokenRepository.saveAll(answers);
        //Aggiungo il prof e cancello il token di partenza
        c.getProfessors().add(receiver);
        notificationTokenRepository.delete(notificationToken);
    }

    // permette al docente di rifiutare la richiesta di un altro docente di cooperare per un
    // determinato corso tramite notifica
    @PreAuthorize("@securityApiAuth.ownNotification(#tokenId)")
    @Transactional
    @Override
    public void rejectCooperation(String tokenId) {
        NotificationToken notificationToken = entityGetter.getNotificationToken(tokenId);

        if(notificationToken.getType() != NotificationToken.NotificationType.PROFESSOR_COOPERATION)
            throw new InvalidOrExpiredTokenException(tokenId);

        Professor sender = entityGetter.getProfessor(notificationToken.getSenderId());
        Professor receiver = entityGetter.getProfessor(notificationToken.getReceiverId());
        Course c = entityGetter.getCourse(notificationToken.getCourseName());

        if(!c.getProfessors().contains(sender))
            throw new ProfessorNotOwnCourseException(sender.getId(), c.getName());

        String message = String.format("Il professor %s %s (%s) ha rifiutato " +
                        "la tua richiesta di collaborazione per il corso %s",
                receiver.getFirstName(), receiver.getName(), receiver.getId(), c.getName());

        //Messaggio per il professore che ha fatto l'invito
        NotificationToken answer = new NotificationToken(
                receiver.getId(),
                sender.getId(),
                c.getName(),
                message,
                NotificationToken.NotificationType.RESPONSE
        );

        notificationTokenRepository.delete(notificationToken);
        notificationTokenRepository.save(answer);
    }

    // permette di ritornare le notifiche per un determinato utente
    @PreAuthorize("@securityApiAuth.isMe(#receiverId)")
    @Override
    public List<NotificationTokenDTO> getUserNotifications(String receiverId) {
        return notificationTokenRepository.findByReceiverIdOrderByCreationDesc(receiverId)
                .stream().map(n -> mapper.map(n, NotificationTokenDTO.class))
                .collect(Collectors.toList());
    }

    private boolean isSameTokenPresent(String senderId, String receiverId, String courseName){
        return notificationTokenRepository
                .countBySenderIdAndReceiverIdAndCourseName(senderId, receiverId, courseName) > 0;
    }
}
