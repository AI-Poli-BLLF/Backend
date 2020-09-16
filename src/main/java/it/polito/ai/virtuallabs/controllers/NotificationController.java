package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.exceptions.NotificationException;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;


@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/confirm")
    @ResponseStatus(value = HttpStatus.OK)
    private void confirmToken(@RequestBody @Valid String token){
        try{
            notificationService.confirm(token);
        }catch (NotificationException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/reject")
    @ResponseStatus(value = HttpStatus.OK)
    private void rejectToken(@RequestBody @Valid String token){
        try {
            notificationService.reject(token);
        }catch (NotificationException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/confirm-registration")
    @ResponseStatus(value = HttpStatus.OK)
    private void confirmRegistration(@RequestBody String token){
        try{
            notificationService.confirmRegistration(token);
        }catch (TeamServiceException | NotificationException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

}
