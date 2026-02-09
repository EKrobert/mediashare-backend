package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.Comment;
import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.CommentServiceImpl;
import ezian.robert.mediasharebackend.service.MediaServiceImpl;
import ezian.robert.mediasharebackend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentServiceImpl commentService;
    private final MediaServiceImpl mediaService;

    public CommentController(CommentServiceImpl commentService, MediaServiceImpl mediaService) {
        this.commentService = commentService;
        this.mediaService = mediaService;
    }

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            String commentText = (String) request.get("comment");
            Long mediaId = ((Number) request.get("mediaId")).longValue();

            if (commentText == null || commentText.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le commentaire est obligatoire"));
            }

            Media media;
            try {
                media = mediaService.findById(mediaId);
            } catch (Exception e) {
                logger.error("Media not found: {}", mediaId, e);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Média non trouvé"));
            }

            Comment comment = new Comment();
            comment.setComment(commentText.trim());
            comment.setUser(currentUser);
            comment.setMedia(media);

            Comment savedComment = commentService.save(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);

        } catch (Exception e) {
            logger.error("Error creating comment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création du commentaire"));
        }
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<?> getCommentsByMedia(@PathVariable Long mediaId) {
        try {
            List<Comment> comments = commentService.findByMediaId(mediaId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("Error fetching comments for media: {}", mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des commentaires"));
        }
    }
}