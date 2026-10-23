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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    // Longueur par défaut d'une colonne texte JPA (VARCHAR 255)
    private static final int MAX_COMMENT_LENGTH = 255;

    private final CommentServiceImpl commentService;
    private final MediaServiceImpl mediaService;

    public CommentController(CommentServiceImpl commentService, MediaServiceImpl mediaService) {
        this.commentService = commentService;
        this.mediaService = mediaService;
    }

    // Réponse légère : on n'envoie jamais l'objet User ni Media complet
    private Map<String, Object> toResponse(Comment comment) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", comment.getId());
        body.put("comment", comment.getComment());
        body.put("createdAt", comment.getCreatedAt());
        body.put("username", comment.getUser() != null ? comment.getUser().getUsername() : null);
        return body;
    }

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            Object mediaIdValue = request.get("mediaId");
            Object commentValue = request.get("comment");

            if (!(mediaIdValue instanceof Number)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "L'identifiant du média est obligatoire"));
            }

            if (!(commentValue instanceof String) || ((String) commentValue).trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le commentaire est obligatoire"));
            }

            String commentText = ((String) commentValue).trim();
            if (commentText.length() > MAX_COMMENT_LENGTH) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le commentaire ne doit pas dépasser " + MAX_COMMENT_LENGTH + " caractères"));
            }

            Long mediaId = ((Number) mediaIdValue).longValue();

            Media media;
            try {
                media = mediaService.findById(mediaId);
            } catch (Exception e) {
                logger.error("Media not found: {}", mediaId, e);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Média non trouvé"));
            }

            Comment comment = new Comment();
            comment.setComment(commentText);
            comment.setUser(currentUser);
            comment.setMedia(media);

            Comment savedComment = commentService.save(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedComment));

        } catch (Exception e) {
            logger.error("Error creating comment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création du commentaire"));
        }
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<?> getCommentsByMedia(@PathVariable Long mediaId) {
        try {
            // Du plus récent au plus ancien (id décroissant)
            List<Map<String, Object>> comments = commentService.findByMediaId(mediaId).stream()
                    .sorted(Comparator.comparing(Comment::getId).reversed())
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("Error fetching comments for media: {}", mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des commentaires"));
        }
    }
}