package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    User findById(long id);
}
