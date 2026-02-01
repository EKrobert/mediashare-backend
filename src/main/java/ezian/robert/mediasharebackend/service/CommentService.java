package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Comment;

import java.util.List;

public interface CommentService {

    public List<Comment> findAll();
    public Comment save(Comment comment);
    public List<Comment> findAllByMediaId(Long mediaId);


}
