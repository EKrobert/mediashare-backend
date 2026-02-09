package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.util.SecurityUtils;
import ezian.robert.mediasharebackend.model.Category;
import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.CategoryServiceImpl;
import ezian.robert.mediasharebackend.service.FileStorageService;
import ezian.robert.mediasharebackend.service.MediaServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medias")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

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

    @PostMapping
    public ResponseEntity<?> createMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categoryId", required = true) Long categoryId) {

        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le fichier est vide"));
            }

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le titre est obligatoire"));
            }

            String mediaUrl = fileStorageService.store(file);
            String mediaType = determineMediaType(file.getContentType());
            String thumbnail = null;

            // Génération automatique du thumbnail
            if (mediaType.equals("VIDEO")) {
                thumbnail = fileStorageService.generateVideoThumbnail(mediaUrl);
            } else if (mediaType.equals("IMAGE")) {
                thumbnail = mediaUrl;
            }

            Media media = new Media();
            media.setTitle(title.trim());
            media.setDescription(description != null ? description.trim() : null);
            media.setMediaUrl(mediaUrl);
            media.setMediaType(mediaType);
            media.setThumbnail(thumbnail);
            media.setUser(currentUser);

            if (categoryId != null) {
                try {
                    Category category = categoryService.findById(categoryId);
                    media.setCategory(category);
                } catch (Exception e) {
                    logger.error("Category not found: {}", categoryId, e);
                    fileStorageService.delete(mediaUrl);
                    if (thumbnail != null && !thumbnail.equals(mediaUrl)) {
                        fileStorageService.delete(thumbnail);
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Catégorie introuvable"));
                }
            }

            Media savedMedia = mediaService.save(media);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedia);

        } catch (Exception e) {
            logger.error("Error creating media", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création du média: " + e.getMessage()));
        }
    }

    @GetMapping("/my-medias")
    public ResponseEntity<?> getMyMedias() {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            List<Media> medias = mediaService.findByUserId(currentUser.getId());
            return ResponseEntity.ok(medias);
        } catch (Exception e) {
            logger.error("Error fetching user medias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des médias"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllMedias() {
        try {
            List<Media> medias = mediaService.findAll();
            return ResponseEntity.ok(medias);
        } catch (Exception e) {
            logger.error("Error fetching all medias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des médias"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMediaById(@PathVariable Long id) {
        try {
            Media media = mediaService.findById(id);
            return ResponseEntity.ok(media);
        } catch (Exception e) {
            logger.error("Media not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Média non trouvé"));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getMediasByCategory(@PathVariable Long categoryId) {
        try {
            categoryService.findById(categoryId);
            List<Media> medias = mediaService.findByCategoryId(categoryId);
            return ResponseEntity.ok(medias);
        } catch (Exception e) {
            logger.error("Category not found: {}", categoryId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Catégorie non trouvée"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedia(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            Media media;
            try {
                media = mediaService.findById(id);
            } catch (Exception e) {
                logger.error("Media not found for update: {}", id, e);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Média non trouvé"));
            }

            if (!media.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Non autorisé"));
            }

            if (request.containsKey("title")) {
                String title = (String) request.get("title");
                if (title == null || title.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Le titre ne peut pas être vide"));
                }
                media.setTitle(title.trim());
            }

            if (request.containsKey("description")) {
                String description = (String) request.get("description");
                media.setDescription(description != null ? description.trim() : null);
            }

            if (request.containsKey("categoryId")) {
                Object categoryIdObj = request.get("categoryId");
                if (categoryIdObj == null) {
                    media.setCategory(null);
                } else {
                    try {
                        Long categoryId = ((Number) categoryIdObj).longValue();
                        Category category = categoryService.findById(categoryId);
                        media.setCategory(category);
                    } catch (Exception e) {
                        logger.error("Category not found during update: {}", categoryIdObj, e);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Catégorie introuvable"));
                    }
                }
            }

            Media updatedMedia = mediaService.save(media);
            return ResponseEntity.ok(updatedMedia);

        } catch (Exception e) {
            logger.error("Error updating media: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour du média"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedia(@PathVariable Long id) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non authentifié"));
            }

            Media media;
            try {
                media = mediaService.findById(id);
            } catch (Exception e) {
                logger.error("Media not found for deletion: {}", id, e);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Média non trouvé"));
            }

            if (!media.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Non autorisé"));
            }

            fileStorageService.delete(media.getMediaUrl());

            if (media.getThumbnail() != null && !media.getThumbnail().equals(media.getMediaUrl())) {
                fileStorageService.delete(media.getThumbnail());
            }

            mediaService.delete(id);

            return ResponseEntity.ok(Map.of("message", "Média supprimé avec succès"));

        } catch (Exception e) {
            logger.error("Error deleting media: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression du média"));
        }
    }

    private String determineMediaType(String contentType) {
        if (contentType == null) return "OTHER";
        if (contentType.startsWith("image/")) return "IMAGE";
        if (contentType.startsWith("video/")) return "VIDEO";
        if (contentType.startsWith("audio/")) return "AUDIO";
        return "OTHER";
    }
}