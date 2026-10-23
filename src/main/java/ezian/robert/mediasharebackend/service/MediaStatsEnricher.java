package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Remplit les champs calculés d'une vidéo : nombre de likes, nombre de commentaires
 * et "liked" (l'utilisateur connecté a-t-il aimé cette vidéo ?).
 * Ces valeurs ne sont pas stockées dans la table medias.
 */
@Component
public class MediaStatsEnricher {

    private final LikeServiceImpl likeService;
    private final CommentServiceImpl commentService;

    public MediaStatsEnricher(LikeServiceImpl likeService, CommentServiceImpl commentService) {
        this.likeService = likeService;
        this.commentService = commentService;
    }

    // currentUser peut être null (route publique) : liked vaut alors false
    public Media enrich(Media media, User currentUser) {
        Long mediaId = media.getId();

        media.setLikes(likeService.countByMediaId(mediaId));
        media.setComments(commentService.findByMediaId(mediaId).size());
        media.setLiked(currentUser != null
                && likeService.findByUserIdAndMediaId(currentUser.getId(), mediaId) != null);

        return media;
    }

    public List<Media> enrich(List<Media> medias, User currentUser) {
        medias.forEach(media -> enrich(media, currentUser));
        return medias;
    }
}