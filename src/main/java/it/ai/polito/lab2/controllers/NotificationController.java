package it.ai.polito.lab2.controllers;

import it.ai.polito.lab2.service.NotificationService;
import it.ai.polito.lab2.service.exceptions.NotificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


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
