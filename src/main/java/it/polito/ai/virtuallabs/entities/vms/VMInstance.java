package it.polito.ai.virtuallabs.entities.vms;

import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.entities.Team;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "vm_instance")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VMInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "vm_seq")
    @EqualsAndHashCode.Include
    private Long id;

    private boolean active;
    private int cpu;
    private int ramSize;
    private int diskSize;

    @ManyToOne
    @JoinColumn(name = "vm_model_id", nullable = false)
    private VMModel vmModel;

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

    public void addOwner(Student student){
        if (student == null)
            return;

        owners.add(student);
        student.getVms().add(this);
    }

    public void removeOwner(Student student){
        if(student == null)
            return;

        owners.remove(student);
        student.getVms().remove(this);
    }

    public void setVmModel(VMModel vmModel) {
        if(this.vmModel != null)
            this.vmModel.getVmInstances().remove(this);

        if(vmModel != null && !vmModel.getVmInstances().contains(this))
            vmModel.getVmInstances().add(this);

        this.vmModel = vmModel;
    }

    public void setTeam(Team team) {
        if(this.team != null)
            this.team.getVms().remove(this);

        if(team != null && !team.getVms().contains(this))
            team.getVms().add(this);

        this.team = team;
    }

    public Map<String, Integer> config(){
        Map<String,Integer> map = new HashMap<>();

        map.put("cpu", cpu);
        map.put("disk_size", diskSize);
        map.put("ram_size", ramSize);
        return map;
    }
}

