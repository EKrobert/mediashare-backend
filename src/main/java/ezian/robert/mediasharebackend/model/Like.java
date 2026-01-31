package ezian.robert.mediasharebackend.model;

import jakarta.persistence.*;

@Entity
@Table(name="likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "media_id"}))
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //....RELATIONS....//
    //media
    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;
    //user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
