package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like,Long> {
}
