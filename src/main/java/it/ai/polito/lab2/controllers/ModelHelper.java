package it.ai.polito.lab2.controllers;

import it.ai.polito.lab2.dtos.CourseDTO;
import it.ai.polito.lab2.dtos.ProfessorDTO;
import it.ai.polito.lab2.dtos.StudentDTO;
import it.ai.polito.lab2.dtos.TeamDTO;
import lombok.Data;
import org.springframework.hateoas.Link;

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
        student.add(self);
        student.add(courses);
        student.add(teams);
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
        professor.add(self);
        return professor;
    }

    @Data
    public static class TeamProposal {
        @NotEmpty
        private String teamName;
        @NotNull
        @NotEmpty
        private List<String> memberIds;
    }
}
