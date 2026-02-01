package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Media;

import java.util.List;

interface  MediaService {
    public List<Media> findAll();
    public List<Media> findAllByUserId(Long userId);
    public Media findById(Long id);
    public Media save(Media media);
    public boolean delete(Long id);

}
