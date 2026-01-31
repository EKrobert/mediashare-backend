package ezian.robert.mediasharebackend.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name="description")
    private String description;

    //..... RELATION......//
    //media
    @OneToMany(mappedBy = "category")
    private List <Media> medias;


}
