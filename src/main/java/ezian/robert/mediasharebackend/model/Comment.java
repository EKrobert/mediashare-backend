package ezian.robert.mediasharebackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "comment")
    private String comment;

    //..... RELATIONS .....//
    //comment and user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //comment and media
    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Media getMedia() { return media; }
    public void setMedia(Media media) { this.media = media; }
}
