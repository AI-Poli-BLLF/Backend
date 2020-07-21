package it.ai.polito.lab2.entities;

import it.ai.polito.lab2.security.entities.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    private int min;
    private int max;
    private boolean enabled;

    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<Team> teams = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    public void addStudent(Student student){
        if(student == null)
            return;

        students.add(student);
        student.getCourses().add(this);
    }

    public void addTeam(Team team){
        if(team == null)
            return;

        teams.add(team);
        team.setCourse(this);
    }

    public void removeTeam(Team team){
        if(team == null)
            return;

        teams.remove(team);
        team.setCourse(null);
    }

    public void setProfessor(Professor professor) {
        if(this.professor != null)
            this.professor.getCourses().remove(this);

        if(professor != null && !professor.getCourses().contains(this))
            professor.getCourses().add(this);

        this.professor = professor;
    }
}
