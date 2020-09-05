package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.exceptions.NotificationException;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMModelNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;


@Controller
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/confirm/{token}")
    private String confirmToken(@PathVariable String token, Model model){
        try{
            notificationService.confirm(token);
            model.addAttribute("notificationResult", "Hai confermato con successo la partecipazione al gruppo");
        }catch (NotificationException e){
            model.addAttribute("notificationResult", "Link inesistente o scaduto");
        } catch (VMModelNotFoundException e1){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VM model is not set yet for this course");
        }
        return "notification";
    }

    @GetMapping("/reject/{token}")
    private String rejectToken(@PathVariable String token, Model model){
        try {
            notificationService.reject(token);
            model.addAttribute("notificationResult", "Hai rifiutato con successo la partecipazione al gruppo");
        }catch (NotificationException e){
            model.addAttribute("notificationResult", "Link inesistente o scaduto");
        }
        return "notification";
    }
}
