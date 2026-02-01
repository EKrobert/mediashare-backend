package ezian.robert.mediasharebackend.seeders;

import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserSeeder {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Bean
    CommandLineRunner runUserSeeder() {
        return args -> {
            if (userRepository.count() == 0) {
                User admin1 = new User(
                        "admin1@mediashare.com",
                        "admin1",
                        passwordEncoder.encode("admin123"),
                        null,
                        "ADMIN"
                );
                userRepository.save(admin1);

                User admin2 = new User(
                        "admin2@mediashare.com",
                        "admin2",
                        passwordEncoder.encode("admin123"),
                        null,
                        "ADMIN"
                );
                userRepository.save(admin2);

                System.out.println("2 Admin users created!");
            }
        };
    }


}
