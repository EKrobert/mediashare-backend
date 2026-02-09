package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findAllByMediaId(Long mediaId);
    List<Comment> findByMediaId(Long mediaId);
}
