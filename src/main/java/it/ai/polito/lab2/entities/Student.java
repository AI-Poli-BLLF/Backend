package it.ai.polito.lab2.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Student {

    private static final String joinTable = "student_course";
    private static final String joinColumn = "student_id";
    private static final String inverseJoinCol = "course_name";

    @Id //Starts with s + matricola
    private String id;
    private String name;
    private String firstName;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = joinTable,
            joinColumns = @JoinColumn(name = joinColumn),
            inverseJoinColumns = @JoinColumn(name = inverseJoinCol)
    )
    private List<Course> courses = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private List<Team> teams = new ArrayList<>();

    @ManyToMany(mappedBy = "owners")
    private List<VMInstance> vms = new ArrayList<>();

    public void addCourse(Course course){
        if(course == null)
            return;

        courses.add(course);
        course.getStudents().add(this);
    }

    public void addTeam(Team team){
        if(team == null)
            return;

        teams.add(team);
        team.getMembers().add(this);
    }

    public void removeTeam(Team team){
        if(team == null)
            return;

        teams.remove(team);
        team.getMembers().remove(this);
    }
}
