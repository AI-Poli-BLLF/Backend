package it.polito.ai.virtuallabs.controllers.utility;

import it.polito.ai.virtuallabs.controllers.CourseController;
import it.polito.ai.virtuallabs.controllers.ProfessorController;
import it.polito.ai.virtuallabs.controllers.StudentController;
import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;
import it.polito.ai.virtuallabs.entities.Professor;
import it.polito.ai.virtuallabs.entities.Student;
import lombok.Data;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class ModelHelper {

    public static CourseDTO enrich(CourseDTO course){
        Link self = linkTo(CourseController.class).slash(course.getName()).withSelfRel();
        Link enrolled = linkTo(CourseController.class).slash(course.getName())
                .slash("enrolled").withRel("enrolled");
        Link teams = linkTo(CourseController.class).slash(course.getName()).slash("teams").withRel("teams");
        Link availableStudents = linkTo(CourseController.class).slash(course.getName()).slash("availableStudents").withRel("availableStudents");
        Link studentsInTeam = linkTo(CourseController.class).slash(course.getName()).slash("studentsInTeam").withRel("studentsInTeam");
        course.add(self);
        course.add(enrolled);
        course.add(teams);
        course.add(availableStudents);
        course.add(studentsInTeam);
        return course;
    }

    public static StudentDTO enrich(StudentDTO student){
        Link self = linkTo(StudentController.class).slash(student.getId()).withSelfRel();
        Link courses = linkTo(StudentController.class).slash(student.getId()).slash("courses").withRel("courses");
        Link teams = linkTo(StudentController.class).slash(student.getId()).slash("teams").withRel("teams");
        Link photo = linkTo(StudentController.class).slash(student.getId()).slash("photo").withRel("photo");
        student.add(self);
        student.add(courses);
        student.add(teams);
        student.add(photo);
        return student;
    }

    public static TeamDTO enrich(TeamDTO team){
        Link self = linkTo(CourseController.class).slash("teams").slash(team.getId()).withSelfRel();
        Link members = linkTo(CourseController.class).slash("teams").slash(team.getId())
                .slash("members").withRel("members");
        team.add(self);
        team.add(members);
        return team;
    }

    public static ProfessorDTO enrich(ProfessorDTO professor){
        Link self = linkTo(ProfessorController.class).slash(professor.getId()).withSelfRel();
        Link photo = linkTo(ProfessorController.class).slash(professor.getId()).slash("photo").withRel("photo");
        professor.add(self);
        professor.add(photo);
        return professor;
    }

    public static AssignmentDTO enrich(AssignmentDTO assignmentDTO, String courseId) {
        Link self = linkTo(ProfessorController.class).slash(assignmentDTO).slash(assignmentDTO.getId()).withSelfRel();
        Link professor = linkTo(ProfessorController.class).slash(assignmentDTO.getId()).slash("getProfessor").withRel("getProfessor");
        Link drafts = linkTo(ProfessorController.class).slash("assignments").slash(assignmentDTO.getId())
                .slash("drafts").withRel("drafts");
        Link image = linkTo(StudentController.class).slash("courses").slash(courseId).slash("assignments").slash(assignmentDTO.getId())
                .slash("image").withRel("image");
        assignmentDTO.add(self);
        assignmentDTO.add(professor);
        assignmentDTO.add(drafts);
        assignmentDTO.add(image);
        return assignmentDTO;
    }

//    public static DraftDTO enrich(DraftDTO draftDTO){
//        Link self = linkTo(StudentController.class).slash(draftDTO.getStudent .slash(DraftDTO.getId()).slash().withselfrel)
//    }

    @Data
    public static class TeamProposal {
        @NotEmpty
        private String teamName;
        @NotNull
        @NotEmpty
        private List<String> memberIds;
        @NotEmpty
        private String proposerId;
        @NotNull
        private Integer timeout;
    }

    @Data
    public static class VMInstanceData{
        @NotEmpty
        private String studentId;
        @NotNull
        private VMInstanceDTO instance;
    }

    @Data
    public static class AddCourseRequest{
        @NotNull
        private CourseDTO course;
        @NotNull
        private VMModelDTO vmModel;
        @NotEmpty
        private String professorId;
    }
}
