package it.polito.ai.virtuallabs.entities.vms;

import it.polito.ai.virtuallabs.entities.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "vm_config")
@AllArgsConstructor
@NoArgsConstructor
public class VMConfig {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "team_id")
    @MapsId //Permette di condividere la stessa primary key con team
    private Team team;

    private int maxCpu;
    private int maxDisk;
    private int maxRam;
    private int maxActive;
    private int maxVm;
}
