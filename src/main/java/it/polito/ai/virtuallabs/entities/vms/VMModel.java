package it.polito.ai.virtuallabs.entities.vms;

import it.polito.ai.virtuallabs.entities.Course;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "vm_model", uniqueConstraints = @UniqueConstraint(columnNames = {"course_name"}))
public class VMModel {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "os")
    private VMOs os;

    private String version;

    @OneToOne
    @JoinColumn(name = "course_name")
    @MapsId //Permette di condividere la stessa primary key con corso
    private Course course;

    @OneToMany(mappedBy = "vmModel")
    private List<VMInstance> vmInstances;

    public void setOs(VMOs os) {
        if(this.os != null)
            this.os.getVmModels().remove(this);

        this.os = os;
        if(os == null)
            return;
        os.getVmModels().add(this);
    }
}
