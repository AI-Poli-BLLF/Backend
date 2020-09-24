package it.polito.ai.virtuallabs.controllers;

import it.polito.ai.virtuallabs.dtos.tokens.NotificationTokenDTO;
import it.polito.ai.virtuallabs.service.NotificationService;
import it.polito.ai.virtuallabs.service.exceptions.NotificationException;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;


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

    @PostMapping("/accept-enrolling-request")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void acceptEnrollingRequest(@RequestBody String tokenId){
        try {
            notificationService.acceptEnrollingRequest(tokenId);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/reject-enrolling-request")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void rejectEnrollingRequest(@RequestBody String tokenId){
        try {
            notificationService.rejectEnrollingRequest(tokenId);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/accept-cooperation")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void acceptCooperation(@RequestBody String tokenId){
        try {
            notificationService.acceptCooperation(tokenId);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/reject-cooperation")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void rejectCooperation(@RequestBody String tokenId){
        try {
            notificationService.rejectCooperation(tokenId);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("/read-notification")
    private void readNotification(@RequestBody String tokenId){
        try {
            notificationService.readNotification(tokenId);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{receiverId}")
    private List<NotificationTokenDTO> getNotifications(@PathVariable String receiverId) {
        try {
            return notificationService.getUserNotifications(receiverId);
        } catch (NotificationException | TeamServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
