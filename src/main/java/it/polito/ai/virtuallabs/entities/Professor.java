package it.polito.ai.virtuallabs.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.expression.spel.ast.Assign;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Professor {

    @Id //starts with d + matricola
    private String id;
    private String name;
    private String firstName;
    private String photoName;

    @ManyToMany(mappedBy = "professors")
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "professor")
    private List<Assignment> assignments = new ArrayList<>();

    public void addCourse(Course course){
        if(course == null)
            return;

        courses.add(course);
        course.getProfessors().add(this);
    }

    public void removeCourse(Course course){
        if(course == null)
            return;

        courses.remove(course);
        course.getProfessors().remove(this);
    }

    public void addAssignment(Assignment assignment){
        if(assignment == null)
            return;
        assignments.add(assignment);
        assignment.setProfessor(this);
    }

    public void removeAssignment(Assignment assignment){
        if(assignment == null)
            return;
        assignments.remove(assignment);
        assignment.setProfessor(null);
    }

    public String getEmail(){
        return String.format("%s@polito.it", id);
    }
}
