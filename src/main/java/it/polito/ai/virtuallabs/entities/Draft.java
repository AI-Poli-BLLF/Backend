package it.polito.ai.virtuallabs.entities;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Draft {

    public enum DraftState {NULL, READ, SUBMITTED, REVIEWED}

    @Id
    @GeneratedValue
    private Long id;

    private DraftState state;

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

    @OneToOne(mappedBy = "draft")
    private Correction correction;

    public void setStatus(DraftState state){
        this.state = state;
    }

    public Draft() {
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
        this.grade = 0;
        this.locker = false;
    }

    public Draft(DraftState state, Assignment assignment, Student student) {
        this();
        this.state = state;
        this.assignment = assignment;
        this.student = student;
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
