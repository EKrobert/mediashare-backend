package ezian.robert.mediasharebackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "comment")
    private String comment;
    // Date de création : remplie automatiquement à l'enregistrement
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    //..... RELATIONS .....//
    //comment and user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //comment and media
    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Media getMedia() { return media; }
    public void setMedia(Media media) { this.media = media; }
}