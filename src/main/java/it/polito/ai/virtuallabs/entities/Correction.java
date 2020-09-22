package it.polito.ai.virtuallabs.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
public class Correction {
    @Id
    @GeneratedValue
    private Long id;

    private String photoName;
    private Timestamp timestamp;

    @OneToOne
    @JoinColumn(name = "draft_id")
    private Draft draft;

    public Correction() {
        timestamp = Timestamp.valueOf(LocalDateTime.now());
    }


    public void setDraft(Draft draft) {
        if(this.draft != null)
            this.draft.setCorrection(null);

        this.draft = draft;
        draft.setCorrection(this);
    }
}
