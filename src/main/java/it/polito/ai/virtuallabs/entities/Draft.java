package it.polito.ai.virtuallabs.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Draft {

    public enum State{NULL, READ, SUBMITTED, REVIEWED}

    @Id
    @GeneratedValue
    private Long id;

    private State state;

    private int grade;

    private String photoName;

    private Timestamp timestamp;

    private boolean locker; //se true, il docente ha deciso che non è più modificabile

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    public void setStatus(State state){
        this.state = state;
    }


    public void setAssignment(Assignment assignment){
        if(this.assignment != null)
            this.assignment.getDrafts().remove(this);
        if(assignment != null && !assignment.getDrafts().contains(this))
            assignment.getDrafts().add(this);
        this.assignment = assignment;
    }

    public void setStudent(Student student){
        if(student == null)
            return;
        this.student = student;
        student.getDrafts().add(this);
    }

}
