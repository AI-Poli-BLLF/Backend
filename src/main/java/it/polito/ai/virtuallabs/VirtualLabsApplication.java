package it.polito.ai.virtuallabs;

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

@SpringBootApplication
@EnableScheduling
public class VirtualLabsApplication {
    
    @Bean
    public CommandLineRunner runner(UserRepository repository, PasswordEncoder passwordEncoder,
                                    VMService vmService, TeamService teamService){
        return args -> {
            try {
                User admin = User.builder().id("a1").username("admin@polito.it").password(passwordEncoder.encode("admin")).roles(Arrays.asList("ROLE_ADMIN")).build();
                User test_front = User.builder().id("a2").username("test@polito.it").password(passwordEncoder.encode("test")).roles(Arrays.asList("ROLE_ADMIN")).build();
                User test_student = User.builder().id("test").username("test@studenti.polito.it").password(passwordEncoder.encode("test")).roles(Arrays.asList("ROLE_STUDENT")).build();
                User borghesi = User.builder().id("s268199").username("s268199@studenti.polito.it").password(passwordEncoder.encode("matteo")).roles(Arrays.asList("ROLE_STUDENT")).build();
                repository.saveAndFlush(admin);
                repository.saveAndFlush(test_front);
                repository.saveAndFlush(test_student);
                repository.saveAndFlush(borghesi);
                System.out.println(repository.findAll());

            }catch (Exception ignored){
                System.out.println("fail");
            }

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
