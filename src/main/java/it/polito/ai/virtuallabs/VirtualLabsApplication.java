package it.polito.ai.virtuallabs;

import it.polito.ai.virtuallabs.dtos.vms.VMOsDTO;
import it.polito.ai.virtuallabs.entities.vms.VMOs;
import it.polito.ai.virtuallabs.repositories.vms.VMOsRepository;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.security.repositories.UserRepository;
import it.polito.ai.virtuallabs.service.TeamService;
import it.polito.ai.virtuallabs.service.VMService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
@EnableScheduling
public class VirtualLabsApplication {
    
    @Bean
    public CommandLineRunner runner(UserRepository repository, PasswordEncoder passwordEncoder,
                                    VMOsRepository vmOsRepository){
        return args -> {
            try {
                User admin = User.builder().id("a1").username("admin@polito.it").password(passwordEncoder.encode("admin"))
                        .roles(Arrays.asList("ROLE_ADMIN")).enabled(true).build();
                repository.saveAndFlush(admin);
                System.out.println(repository.findAll());

            }catch (Exception ignored){}

            //Creazione VMOS versions
            try {
                VMOs windows = VMOs.builder().osName("Windows")
                        .versions(new HashSet<>(Arrays.asList("Vista", "7", "10"))).build();
                VMOs ubuntu = VMOs.builder().osName("Ubuntu")
                        .versions(new HashSet<>(Arrays.asList("18.04", "19.10", "20.04"))).build();
                VMOs mac = VMOs.builder().osName("MacOS")
                        .versions(new HashSet<>(Arrays.asList("High Sierra", "Catalina"))).build();

                vmOsRepository.saveAndFlush(windows);
                vmOsRepository.saveAndFlush(ubuntu);
                vmOsRepository.saveAndFlush(mac);
            }catch (Exception ignored){}

        };
    }


    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    public static void main(String[] args) {
        SpringApplication.run(VirtualLabsApplication.class, args);
    }

}
