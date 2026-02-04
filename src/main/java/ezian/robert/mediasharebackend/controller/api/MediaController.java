package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.Category;
import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.CategoryServiceImpl;
import ezian.robert.mediasharebackend.service.MediaServiceImpl;
import ezian.robert.mediasharebackend.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medias")
public class MediaController {

    private final MediaServiceImpl mediaService;
    private final CategoryServiceImpl categoryService;
    private final FileStorageService fileStorageService;

    public MediaController(MediaServiceImpl mediaService,
                           CategoryServiceImpl categoryService,
                           FileStorageService fileStorageService) {
        this.mediaService = mediaService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    // Créer un média avec upload
    @PostMapping
    public ResponseEntity<?> createMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {

        try {
            User currentUser = getCurrentUser();

            String mediaUrl = fileStorageService.store(file);
            String mediaType = determineMediaType(file.getContentType());

            Media media = new Media();
            media.setTitle(title);
            media.setDescription(description);
            media.setMediaUrl(mediaUrl);
            media.setMediaType(mediaType);
            media.setUser(currentUser);

            if (categoryId != null) {
                Category category = categoryService.findById(categoryId);
                if (category != null) {
                    media.setCategory(category);
                }
            }

            Media savedMedia = mediaService.save(media);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedia);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création du média"));
        }
    }

    // Récupérer mes médias
    @GetMapping("/my-medias")
    public ResponseEntity<?> getMyMedias() {
        User currentUser = getCurrentUser();
        List<Media> medias = mediaService.findByUserId(currentUser.getId());
        return ResponseEntity.ok(medias);
    }

    // Récupérer tous les médias
    @GetMapping
    public ResponseEntity<?> getAllMedias() {
        List<Media> medias = mediaService.findAll();
        return ResponseEntity.ok(medias);
    }

    // Récupérer un média par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMediaById(@PathVariable Long id) {
        Media media = mediaService.findById(id);

        if (media == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Média non trouvé"));
        }

        return ResponseEntity.ok(media);
    }

    // Récupérer médias par catégorie
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getMediasByCategory(@PathVariable Long categoryId) {
        List<Media> medias = mediaService.findByCategoryId(categoryId);
        return ResponseEntity.ok(medias);
    }

    // Modifier un média
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedia(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        User currentUser = getCurrentUser();
        Media media = mediaService.findById(id);

        if (media == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Média non trouvé"));
        }

        if (!media.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Non autorisé"));
        }

        if (request.get("title") != null) {
            media.setTitle(request.get("title"));
        }
        if (request.get("description") != null) {
            media.setDescription(request.get("description"));
        }

        Media updatedMedia = mediaService.save(media);
        return ResponseEntity.ok(updatedMedia);
    }

    // Supprimer un média
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedia(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Media media = mediaService.findById(id);

        if (media == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Média non trouvé"));
        }

        if (!media.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Non autorisé"));
        }

        fileStorageService.delete(media.getMediaUrl());
        mediaService.delete(id);

        return ResponseEntity.ok(Map.of("message", "Média supprimé"));
    }

    // Utilitaires
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private String determineMediaType(String contentType) {
        if (contentType.startsWith("image/")) return "IMAGE";
        if (contentType.startsWith("video/")) return "VIDEO";
        if (contentType.startsWith("audio/")) return "AUDIO";
        return "OTHER";
    }
}