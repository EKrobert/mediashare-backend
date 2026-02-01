package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Comment;
import ezian.robert.mediasharebackend.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private CommentRepository commentRepository;
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    @Override
    public List<Comment> findAll() {
        return commentRepository.findAll() ;
    }

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> findAllByMediaId(Long mediaId) {
        return commentRepository.findAllByMediaId(mediaId);
    }
}
