package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.Comment;
import ezian.robert.mediasharebackend.service.CommentService;
import ezian.robert.mediasharebackend.service.CommentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private CommentServiceImpl commentService;
    public CommentController(CommentServiceImpl commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
        commentService.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

}
