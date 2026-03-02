package org.example.isc.main.secured.models;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "text")
    private String text;

    @Column(name = "parent_comment")
    private Comment parentComment;

    @Column(name = "responses_count")
    private Long responsesCount;

    @Column(name = "like_count")
    private Long likes;

    public Comment(Post post, User user, String text, Comment parentComment) {
        this.post = post;
        this.user = user;
        this.text = text;
        this.parentComment = parentComment;
    }

    public Comment() {
    }

    public Long getResponsesCount() {
        return responsesCount;
    }

    public void setResponsesCount(Long responsesCount) {
        this.responsesCount = responsesCount;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
