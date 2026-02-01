package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.Category;

import java.util.List;

interface CategoryService {
    public List<Category> findAll();
    public Category findById(Long id);
    public Category save(Category category);
    public boolean deleteById(Long id);

}
