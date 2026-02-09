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


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Media getMedia() { return media; }
    public void setMedia(Media media) { this.media = media; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
