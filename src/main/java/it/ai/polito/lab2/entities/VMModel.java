package it.ai.polito.lab2.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "vm_model", uniqueConstraints = @UniqueConstraint(columnNames = {"course_name"}))
public class VMModel {

    public enum OS{Windows, Ubuntu, Debian}

    @Id
    @GeneratedValue
    private Long id;

    private String os;

    private String version;

    @OneToOne
    @JoinColumn(name = "course_name")
    private Course course;

    public void setOs(OS os) {
        this.os = os.toString();
    }
}
