package it.polito.ai.virtuallabs.entities;

import it.polito.ai.virtuallabs.entities.vms.VMModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
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


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_course",
            joinColumns = @JoinColumn(name = "course_name"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<Team> teams = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "professor_course",
            joinColumns = @JoinColumn(name = "course_name"),
            inverseJoinColumns = @JoinColumn(name = "professor_id")
    )
    private List<Professor> professors = new ArrayList<>();

    @OneToOne(mappedBy = "course")
    private VMModel vmModel;

    @OneToMany(mappedBy = "course")
    private List<Assignment> assignments = new ArrayList<>();

    public void addTeam(Team team){
        if(team == null)
            return;

        teams.add(team);
        team.setCourse(this);
    }

    public void addStudent(Student student){
        if(student == null)
            return;

        students.add(student);
        student.getCourses().add(this);
    }

    public void removeTeam(Team team){
        if(team == null)
            return;

        teams.remove(team);
        team.setCourse(null);
    }

    public void addProfessor(Professor professor) {
        if(professor == null)
            return;

        professors.add(professor);
        professor.getCourses().add(this);
    }

    public void removeProfessor(Professor professor){
        if(professor == null)
            return;

        professors.remove(professor);
        professor.getCourses().remove(this);
    }

    public String getAcronym() {
        String[] pieces = name.split("\\s+");
        return Arrays.stream(pieces).map(p -> Character.toString(Character.toUpperCase(p.toCharArray()[0])))
                .reduce((p1, p2) -> p1 + p2).orElse(this.name);
    }
}
