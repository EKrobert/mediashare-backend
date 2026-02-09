package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Like;

import java.util.List;

public interface LikeService {
    public List <Like> findAllByMediaId(Long mediaId);
    public Like save(Like like);
    Like findByUserIdAndMediaId(Long userId, Long mediaId);
    int countByMediaId(Long mediaId);


}
