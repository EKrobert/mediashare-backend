package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Like;
import ezian.robert.mediasharebackend.repository.LikeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeServiceImpl implements LikeService {
    private LikeRepository likeRepository;
    public LikeServiceImpl(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Override
    public List<Like> findAllByMediaId(Long mediaId) {
        return likeRepository.findAllByMediaId(mediaId);
    }

    @Override
    public Like save(Like like) {
        return likeRepository.save(like);
    }

    @Override
    public Like findByUserIdAndMediaId(Long userId, Long mediaId) {
        return likeRepository.findByUserIdAndMediaId(userId, mediaId);
    }

    @Override
    public int countByMediaId(Long mediaId) {
        return likeRepository.countByMediaId(mediaId);
    }

    public void delete(Long id) {
        likeRepository.deleteById(id);
    }
}
