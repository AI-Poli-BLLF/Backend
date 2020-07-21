package it.ai.polito.lab2.service;

import it.ai.polito.lab2.dtos.TeamDTO;

import java.util.List;

public interface NotificationService {
    void sendMessage(String address, String subject, String body);
    void confirm(String token);
    void reject(String token);
    void notifyTeam(TeamDTO dto, List<String> memberIds);
    void deleteExpiredToken();
}
