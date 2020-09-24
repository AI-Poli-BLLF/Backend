package it.polito.ai.virtuallabs.security.service;

import it.polito.ai.virtuallabs.entities.Assignment;
import it.polito.ai.virtuallabs.entities.Draft;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.entities.Team;
import it.polito.ai.virtuallabs.entities.tokens.NotificationToken;
import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import it.polito.ai.virtuallabs.repositories.ProfessorRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.repositories.TeamRepository;
import it.polito.ai.virtuallabs.security.entities.Roles;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.exceptions.StudentNotFoundException;
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
    @Autowired
    private EntityGetter entityGetter;

    public static User getPrincipal() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean ownCourse(String courseName) {
        User principal = getPrincipal();
        if (!principal.getRoles().contains(Roles.ROLE_PROFESSOR.toString()))
            return false;

        return professorRepository.getCourseNames(principal.getId())
                .stream().map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(courseName.toLowerCase());
    }

    public boolean isEnrolled(String courseName) {
        User principal = getPrincipal();
        if (!principal.getRoles().contains(Roles.ROLE_STUDENT.toString()))
            return false;

        boolean toReturn = studentRepository.getCourseNames(principal.getId())
                .stream().map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(courseName.toLowerCase());

        if (!toReturn) {
            System.out.println("User is not enrolled in the course");
        }
        return toReturn;
    }

    public boolean doesTeamBelongsToOwnedOrEnrolledCourses(Long teamId) {
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
        if (!principal.getRoles().contains(Roles.ROLE_STUDENT.toString())) {
            System.out.println("User does not have the role STUDENT");
            return false;
        }
        boolean toReturn = memberIds
                .stream().map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(principal.getId().toLowerCase());

        if (!toReturn) {
            System.out.println("User does not appear among the proposed members");
        }
        return toReturn;
    }

    public boolean ownVm(Long vmInstanceId) {
        VMInstance vmInstance = entityGetter.getVMInstance(vmInstanceId);

        User principal = getPrincipal();
        List<String> ownerIds = vmInstance.getOwners().stream().map(Student::getId).collect(Collectors.toList());

        return ownerIds.contains(principal.getId());
    }

    public boolean amIbelongToTeam(Long teamId) {
        Team t = entityGetter.getTeam(teamId);
        User principal = getPrincipal();

        return t.getMembers().contains(entityGetter.getStudent(principal.getId()));
    }

    public boolean ownDraft(String studentId, String courseName, Long assignmentId, Long draftId) {
        User principal = getPrincipal();
        if (!principal.getRoles().contains(Roles.ROLE_STUDENT.toString()))
            return false;

        if (!isEnrolled(courseName))
            return false;

        Draft draft = entityGetter.getDraft(draftId);

        if(!draft.getAssignment().getId().equals(assignmentId))
            return false;

        return draft.getStudent().getId().equals(studentId);
//        boolean toReturn = !studentRepository.findAll().stream()
//                .filter(s -> s.getId().equals(studentId))
//                .flatMap(s -> s.getDrafts().stream())
//                .filter(d -> d.getId().equals(draftId))
//                .filter(d -> d.getAssignment().getCourse().getName().equals(courseName))
//                .filter(d -> d.getAssignment().getId().equals(assignmentId))
//                .collect(Collectors.toSet())
//                .isEmpty();
//        return toReturn;
    }

    public boolean ownNotification(String tokenId) {
        NotificationToken notificationToken = entityGetter.getNotificationToken(tokenId);
        return notificationToken.getReceiverId().equals(getPrincipal().getId());
    }
}
