package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like,Long> {
    List<Like> findAllByMediaId(Long mediaId);
    Like findByUserIdAndMediaId(Long userId, Long mediaId);
    int countByMediaId(Long mediaId);
}
