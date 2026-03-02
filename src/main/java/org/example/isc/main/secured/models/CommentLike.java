package org.example.isc.main.secured.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes")
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment")
    private Comment comment;

}
