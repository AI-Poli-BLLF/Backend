package it.ai.polito.lab2.entities;

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
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private int maxCpu;
    private int maxDisk;
    private int maxRam;
    private int maxActive;
    private int maxVm;
}
