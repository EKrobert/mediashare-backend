package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.Like;
import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.LikeServiceImpl;
import ezian.robert.mediasharebackend.service.MediaServiceImpl;
import ezian.robert.mediasharebackend.service.MediaStatsEnricher;
import ezian.robert.mediasharebackend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    private final LikeServiceImpl likeService;
    private final MediaServiceImpl mediaService;
    private final MediaStatsEnricher mediaStats;

    public LikeController(LikeServiceImpl likeService,
                          MediaServiceImpl mediaService,
                          MediaStatsEnricher mediaStats) {
        this.likeService = likeService;
        this.mediaService = mediaService;
        this.mediaStats = mediaStats;
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
                int count = likeService.countByMediaId(mediaId);
                return ResponseEntity.ok(Map.of("message", "Like supprimé", "liked", false, "count", count));
            }

            // Like - créer le like
            Like like = new Like();
            like.setUser(currentUser);
            like.setMedia(media);

            try {
                likeService.save(like);
            } catch (DataIntegrityViolationException e) {
                // Double appui rapide : le like vient d'être créé par une autre requête
                logger.warn("Like already exists for user {} and media {}", currentUser.getId(), mediaId);
            }

            int count = likeService.countByMediaId(mediaId);
            return ResponseEntity.ok(Map.of("message", "Like ajouté", "liked", true, "count", count));

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

    // Vidéos aimées par l'utilisateur connecté (la dernière aimée en premier)
    @GetMapping("/my-likes")
    public ResponseEntity<?> getMyLikedMedias() {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            List<Media> medias = likeService.findByUserId(currentUser.getId()).stream()
                    .sorted(Comparator.comparing(Like::getId).reversed())
                    .map(Like::getMedia)
                    .collect(Collectors.toList());

            mediaStats.enrich(medias, currentUser);

            return ResponseEntity.ok(medias);
        } catch (Exception e) {
            logger.error("Error fetching liked medias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des vidéos aimées"));
        }
    }
}