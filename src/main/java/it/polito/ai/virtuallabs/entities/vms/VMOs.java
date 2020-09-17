package it.polito.ai.virtuallabs.entities.vms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "vm_os")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VMOs {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "os_seq")
    private Long id;
    private String osName;

    @OneToMany(mappedBy = "os")
    private List<VMModel> vmModels = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<String> versions = new HashSet<>();


    public void addVersion(String version){
        versions.add(version);
    }

    public void removeVersion(String version){
        versions.remove(version);
    }
}
