package it.polito.ai.virtuallabs.service.scheduledtasks;

import it.polito.ai.virtuallabs.service.AssignmentService;
import it.polito.ai.virtuallabs.service.NotificationService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@Log(topic = "ScheduledTask")
public class AssignmentTask {
    @Autowired
    private AssignmentService assignmentService;

    @Scheduled(initialDelay = 10*1000, fixedRate = 10*60*1000) //every 10 minutes
    public void passiveDraftSubmit() {
        log.info("Passively submitting drafts: " + Timestamp.valueOf(LocalDateTime.now()));
//        notificationService.deleteExpiredToken();
        assignmentService.passiveDraftSubmit();
    }
}
