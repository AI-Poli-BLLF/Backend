package it.polito.ai.virtuallabs;

import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;
import it.polito.ai.virtuallabs.entities.vms.VMModel;
import it.polito.ai.virtuallabs.security.entities.User;
import it.polito.ai.virtuallabs.security.repositories.UserRepository;
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
                                    VMService vmService){
        return args -> {
            /*try {
                User admin = User.builder().id("a1").username("admin").password(passwordEncoder.encode("admin")).roles(Arrays.asList("ROLE_ADMIN")).build();
                repository.saveAndFlush(admin);
                System.out.println(repository.findAll());
            }catch (Exception ignored){}*/

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