package it.ai.polito.lab2.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "vm_instance")
public class VMInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "vm_seq")
    private Long id;

    private boolean online;
    private int cpu;
    private int ramSize;
    private int diskSize;

    @ManyToOne
    @JoinColumn(name = "team_id")
    //Tramite team è possibile risalire al modello della VM, al corso ed alla configurazione
    private Team team;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_owner_vm",
            joinColumns = @JoinColumn(name = "vm_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> owners = new ArrayList<>();
}

