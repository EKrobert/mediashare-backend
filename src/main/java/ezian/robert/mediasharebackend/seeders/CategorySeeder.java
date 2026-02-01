package ezian.robert.mediasharebackend.seeders;

import ezian.robert.mediasharebackend.model.Category;
import ezian.robert.mediasharebackend.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CategorySeeder {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Bean
    CommandLineRunner runCategorySeeder() {
        return args -> {
            seedCategory("Musique", "Tous les médias liés à la musique");
            seedCategory("Anime", "Média d'anime");
            seedCategory("Fun", "Funny media");
            seedCategory("Documentaires", "Médias éducatifs et documentaires");
        };
    }

    private void seedCategory(String title, String description) {
        if (!categoryRepository.existsByTitle(title)) {
            Category category = new Category();
            category.setTitle(title);
            category.setDescription(description);
            categoryRepository.save(category);
        }
    }
}

