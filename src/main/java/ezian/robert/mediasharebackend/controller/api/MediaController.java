package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.MediaServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final MediaServiceImpl mediaServiceImpl;

    public MediaController(MediaServiceImpl mediaServiceImpl) {
        this.mediaServiceImpl = mediaServiceImpl;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createMedia(@RequestBody Map<String, String> request) {
        // Récupérer l'utilisateur connecté via JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Non authentifié"));
        }

        User currentUser = (User) authentication.getPrincipal();

        Media media = new Media();
        media.setTitle(request.get("title"));
        media.setDescription(request.get("description"));
        media.setMediaUrl(request.get("url"));
        media.setUser(currentUser); // Lier le média à l'utilisateur connecté

        // TODO: Gérer la catégorie si nécessaire
        // Long categoryId = Long.parseLong(request.get("category_id"));

        Media mediasave = mediaServiceImpl.save(media);

        if (mediasave != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(mediasave);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création du média"));
        }
    }
}