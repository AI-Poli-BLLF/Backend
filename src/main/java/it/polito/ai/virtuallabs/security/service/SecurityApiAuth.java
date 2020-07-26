package it.polito.ai.virtuallabs.security.service;

import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.repositories.ProfessorRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.repositories.TeamRepository;
import it.polito.ai.virtuallabs.security.entities.Roles;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.service.exceptions.TeamNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityApiAuth {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ProfessorRepository professorRepository;

    public static User getPrincipal(){
        return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean ownCourse(String courseName){
        User principal = getPrincipal();
        if(!principal.getRoles().contains(Roles.ROLE_PROFESSOR.toString()))
            return false;

        return professorRepository.getCourseNames(principal.getId())
                .stream().map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(courseName.toLowerCase());
    }

    public boolean isEnrolled(String courseName) {
        User principal = getPrincipal();
        if(!principal.getRoles().contains(Roles.ROLE_STUDENT.toString()))
            return false;

        return studentRepository.getCourseNames(principal.getId())
                .stream().map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(courseName.toLowerCase());
    }

    public boolean doesTeamBelongsToOwnedOrEnrolledCourses(Long teamId){
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new TeamNotFoundException(teamId)
        );
        String courseName = team.getCourse().getName();
        return ownCourse(courseName) || isEnrolled(courseName);
    }

    public boolean isMe(String id) {
        return getPrincipal().getId()
                .toLowerCase().equals(id.toLowerCase());
    }

    public boolean amIbelongToMembers(List<String> memberIds) {
        User principal = getPrincipal();
        if(!principal.getRoles().contains(Roles.ROLE_STUDENT.toString()))
            return false;
        return memberIds
                .stream().map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(principal.getId().toLowerCase());
    }
}
