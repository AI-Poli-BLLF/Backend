package it.polito.ai.virtuallabs.service.scheduledtasks;

import it.polito.ai.virtuallabs.service.NotificationService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@Log(topic = "ScheduledTask")
public class ExpiredTokensTask {
    @Autowired
    private NotificationService notificationService;

    @Scheduled(initialDelay = 10*1000, fixedRate = 2*60*1000) //every 2 minutes
    public void deleteExpiredToken() {
        log.info("Erasing expired tokens: " + Timestamp.valueOf(LocalDateTime.now()));
        notificationService.deleteExpiredToken();
    }

    @Scheduled(initialDelay = 10*1000, fixedRate = 60*60*1000) //every hour
    public void deleteExpiredNotification(){
        log.info("Erasing expired notifications: " + Timestamp.valueOf(LocalDateTime.now()));
        notificationService.deleteExpiredNotification();
    }
}
