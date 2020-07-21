package it.ai.polito.lab2;

import it.ai.polito.lab2.dtos.StudentDTO;
import it.ai.polito.lab2.security.entities.User;
import it.ai.polito.lab2.security.repositories.UserRepository;
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
public class Lab2Application {

    @Bean
    public CommandLineRunner runner(UserRepository repository, PasswordEncoder passwordEncoder){
        return args -> {
            try {
                User admin = User.builder().id("a1").username("admin").password(passwordEncoder.encode("admin")).roles(Arrays.asList("ROLE_ADMIN")).build();
                repository.saveAndFlush(admin);
                System.out.println(repository.findAll());
            }catch (Exception ignored){}
        };
    }


    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    public static void main(String[] args) {
        SpringApplication.run(Lab2Application.class, args);
    }

}
