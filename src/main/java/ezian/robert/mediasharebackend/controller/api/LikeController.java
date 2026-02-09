package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.Like;
import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.LikeServiceImpl;
import ezian.robert.mediasharebackend.service.MediaServiceImpl;
import ezian.robert.mediasharebackend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    private final LikeServiceImpl likeService;
    private final MediaServiceImpl mediaService;

    public LikeController(LikeServiceImpl likeService, MediaServiceImpl mediaService) {
        this.likeService = likeService;
        this.mediaService = mediaService;
    }

    @PostMapping("/{mediaId}")
    public ResponseEntity<?> toggleLike(@PathVariable Long mediaId) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            Media media;
            try {
                media = mediaService.findById(mediaId);
            } catch (Exception e) {
                logger.error("Media not found: {}", mediaId, e);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Média non trouvé"));
            }

            // Vérifier si le like existe déjà
            Like existingLike = likeService.findByUserIdAndMediaId(currentUser.getId(), mediaId);

            if (existingLike != null) {
                // Unlike - supprimer le like
                likeService.delete(existingLike.getId());
                return ResponseEntity.ok(Map.of("message", "Like supprimé", "liked", false));
            } else {
                // Like - créer le like
                Like like = new Like();
                like.setUser(currentUser);
                like.setMedia(media);

                Like savedLike = likeService.save(like);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Like ajouté", "liked", true, "likeId", savedLike.getId()));
            }

        } catch (Exception e) {
            logger.error("Error toggling like for media: {}", mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du like"));
        }
    }

    @GetMapping("/media/{mediaId}/count")
    public ResponseEntity<?> getLikesCount(@PathVariable Long mediaId) {
        try {
            int count = likeService.countByMediaId(mediaId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            logger.error("Error counting likes for media: {}", mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du comptage des likes"));
        }
    }
}