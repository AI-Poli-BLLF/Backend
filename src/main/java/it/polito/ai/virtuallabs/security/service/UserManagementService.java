package it.polito.ai.virtuallabs.security.service;

import it.polito.ai.virtuallabs.security.dtos.UserDTO;

public interface UserManagementService {
    UserDTO createUser(UserDTO user);
}
