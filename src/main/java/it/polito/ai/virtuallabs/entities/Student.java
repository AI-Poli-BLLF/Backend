package it.polito.ai.virtuallabs.entities;

import it.polito.ai.virtuallabs.entities.vms.VMInstance;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Student {

    @Id //Starts with s + matricola
    private String id;
    private String name;
    private String firstName;
    private String photoName;

    @ManyToMany(mappedBy = "students")
    private List<Course> courses = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private List<Team> teams = new ArrayList<>();

    @ManyToMany(mappedBy = "owners")
    private List<VMInstance> vms = new ArrayList<>();

    @OneToMany(mappedBy = "creator")
    private List<VMInstance> createdVms = new ArrayList<>();

    @OneToMany(mappedBy = "student")
    private List<Draft> drafts = new ArrayList<>();

    public void addCourse(Course course){
        if(course == null)
            return;

        courses.add(course);
        course.getStudents().add(this);
    }

    public void removeCourse(Course course){
        if(course == null)
            return;

        courses.remove(course);
        course.getStudents().remove(this);
    }

    public void addTeam(Team team){
        if(team == null)
            return;

        teams.add(team);
        team.getMembers().add(this);
    }

    public void removeTeam(Team team){
        if(team == null)
            return;

        teams.remove(team);
        team.getMembers().remove(this);
    }

    public void addOwnedVm(VMInstance vm){
        if (vm == null)
            return;

        vms.add(vm);
        vm.getOwners().add(this);
    }

    public void addDraft(Draft draft){
        if(draft == null)
            return;
        drafts.add(draft);
        draft.setStudent(this);
    }

    public void removeDraft(Draft draft){
        if(draft == null)
            return;
        drafts.remove(draft);
        draft.setStudent(null);
    }

    public String getEmail(){
        return String.format("%s@studenti.polito.it", id);
    }
}
