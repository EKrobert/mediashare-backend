package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media,Long> {

    List<Media> findAllByUserId(Long userId);
}
