package org.example.isc.main.secured.models;

import jakarta.persistence.*;

@Entity
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="sender")
    private User sender;

    @ManyToOne
    @JoinColumn(name="post")
    private Post post;

    public Like(Long id, User sender, Post post) {
        this.id = id;
        this.sender = sender;
        this.post = post;
    }

    public Like() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
