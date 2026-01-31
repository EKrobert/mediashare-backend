package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {
}
