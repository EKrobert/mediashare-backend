package ezian.robert.mediasharebackend.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "email")
    private String email;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column (name = "role")
    private String role;

    //user et medias
    @OneToMany(mappedBy = "user")
    private List <Media> medias;
    //user et comment
    @OneToMany(mappedBy = "user")
    private List <Comment> comments;
    //user et likes
    @OneToMany (mappedBy = "user")
    private List <Like> likes;
}
