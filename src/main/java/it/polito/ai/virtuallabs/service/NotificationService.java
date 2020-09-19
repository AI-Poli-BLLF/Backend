package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.TeamDTO;
import it.polito.ai.virtuallabs.dtos.tokens.TokenDTO;

import java.util.List;

public interface NotificationService {
    void sendMessage(String address, String subject, String body);
    void confirm(String token);
    void reject(String token);
    void notifyTeam(TeamDTO dto, List<String> memberIds, String proposerId, Integer timeout);
    void deleteExpiredToken();
    List<String> getPendingMemberIds(Long teamId);
    TokenDTO getPendingMemberToken(Long teamId, String memberId);
    void sendConfirmEmailRegistration(String email, String userFirstName, String userLastName);
    void confirmRegistration(String token);

    //NOTIFICATION TOKEN
    void requestForCourseEnrolling(String studentId, String courseName);
    void cooperateWithProfessor(String senderProfessorId, List<String> receiverProfessorIds, String courseName);
    void readNotification(String tokenId);
    void acceptEnrollingRequest(String tokenId);
    void rejectEnrollingRequest(String tokenId);
}
