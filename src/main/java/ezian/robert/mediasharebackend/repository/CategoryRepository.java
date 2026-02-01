package ezian.robert.mediasharebackend.repository;

import ezian.robert.mediasharebackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    public boolean existsByTitle(String title);
}
