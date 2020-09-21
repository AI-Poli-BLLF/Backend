package it.polito.ai.virtuallabs.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
public class Assignment {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Date releaseDate;
    private Date expiryDate;
    private String photoName;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "assignment")
    private List<Draft> drafts = new ArrayList<>();

    public void setProfessor(Professor professor) {
        if(this.professor != null)
            this.professor.getAssignments().remove(this);

        if(professor != null && !professor.getAssignments().contains(this))
            professor.getAssignments().add(this);

        this.professor = professor;
    }

    public void addDraft(Draft draft){
        if(draft == null)
            return;
        drafts.add(draft);
        draft.setAssignment(this);
    }
    public void removeDraft(Draft draft){
        if(draft == null)
            return;
        drafts.remove(draft);
        draft.setAssignment(null);
    }
}
