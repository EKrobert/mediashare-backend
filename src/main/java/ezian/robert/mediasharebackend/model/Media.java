package ezian.robert.mediasharebackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medias")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;

    //....RELATIONS......//
    //user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


}
