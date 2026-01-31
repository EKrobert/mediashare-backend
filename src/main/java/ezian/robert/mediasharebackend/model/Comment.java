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

}
