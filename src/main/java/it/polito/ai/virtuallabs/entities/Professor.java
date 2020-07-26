package it.polito.ai.virtuallabs.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
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

    @OneToMany(mappedBy = "professor")
    private List<Course> courses = new ArrayList<>();

    public void addCourse(Course course){
        if(course == null)
            return;

        courses.add(course);
        course.setProfessor(this);
    }

    public void removeCourse(Course course){
        if(course == null)
            return;

        courses.remove(course);
        course.setProfessor(null);
    }
}
