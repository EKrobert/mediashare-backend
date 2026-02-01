package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Media;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.repository.MediaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaServiceImpl implements MediaService  {

    private  MediaRepository mediaRepository;
    public MediaServiceImpl(MediaRepository mediaRepository) {
         this.mediaRepository = mediaRepository;
    }

    @Override
    public List<Media> findAll() {
        return mediaRepository.findAll();
    }

    @Override
    public List<Media> findAllByUserId(Long user) {
        return mediaRepository.findAllByUserId(user);
    }

    @Override
    public Media findById(Long id) {
        return mediaRepository.findById(id).get();
    }

    @Override
    public Media save(Media media) {
        return mediaRepository.save(media);
    }

    @Override
    public boolean delete(Long id) {
         if (!mediaRepository.existsById(id)) {
             return false;
         }else  {
            mediaRepository.deleteById(id);
            return true;
         }
    }
}
