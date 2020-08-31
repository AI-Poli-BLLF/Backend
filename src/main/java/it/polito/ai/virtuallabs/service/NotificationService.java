package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.TeamDTO;

import java.util.List;

public interface NotificationService {
    void sendMessage(String address, String subject, String body);
    void confirm(String token);
    void reject(String token);
    void notifyTeam(TeamDTO dto, List<String> memberIds, Integer timeout);
    void deleteExpiredToken();
}
