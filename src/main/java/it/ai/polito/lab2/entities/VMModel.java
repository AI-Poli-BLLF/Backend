package it.ai.polito.lab2.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "vm_model", uniqueConstraints = @UniqueConstraint(columnNames = {"course_name"}))
public class VMModel {

    public enum OS{Windows, Ubuntu, Debian}

    @Id
    private String id;

    private String os;

    private String version;

    @OneToOne
    @JoinColumn(name = "course_name")
    @MapsId //Permette di condividere la stessa primary key con corso
    private Course course;

    public void setOs(OS os) {
        this.os = os.toString();
    }
}
