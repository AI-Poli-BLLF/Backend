package it.polito.ai.virtuallabs.entities.vms;

import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.entities.Team;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.*;

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

    @ManyToOne
    @JoinColumn(name = "creator_id")
    @Column(nullable = false)
    private Student creator;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_owner_vm",
            joinColumns = @JoinColumn(name = "vm_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> owners = new ArrayList<>();


    public void setOwners(List<Student> owners) {
        //Tolgo sempre se è presente il creator
        owners.remove(creator);
        //Se owners non è vuoto cancello la VM dagli owners
        if(!this.owners.isEmpty())
            this.owners.forEach(o-> o.getVms().remove(this));

        //Setto gli owner correnti
        this.owners = owners;
        //Aggiungo la VM a tutti gli owner
        owners.forEach(o->o.getVms().add(this));

    }

    public List<Student> getOwners() {
        List<Student> owners = new ArrayList<>(this.owners);
        owners.add(creator);
        return owners;
    }

    public void setCreator(Student creator) {
        if(this.creator != null){
            if( this.creator.equals(creator))
                return;
            this.creator.getCreatedVms().remove(this);
        }
        this.creator = creator;
        List<VMInstance> createdVms = creator.getCreatedVms();
        if(!createdVms.contains(this))
            createdVms.add(this);
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

