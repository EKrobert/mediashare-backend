package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media,Long> {
}
