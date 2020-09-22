package it.polito.ai.virtuallabs.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Solution {
    @Id
    @GeneratedValue
    private Long id;

    private String photoName;
    private Timestamp timestamp;

    @OneToOne
    @JoinColumn(name = "draftId")
    private Draft draft;

}
