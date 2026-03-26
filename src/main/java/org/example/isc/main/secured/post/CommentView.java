package org.example.isc.main.secured.post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentView {

    private final Long id;
    private final String text;
    private final String authorName;
    private final String authorUsername;
    private final Long authorId;
    private final boolean authorAdmin;
    private final boolean friendAuthor;
    private final long likes;
    private final boolean liked;
    private final long repliesCount;
    private final Long parentId;
    private final List<CommentView> replies = new ArrayList<>();

    public CommentView(Long id,
                       String text,
                       String authorName,
                       String authorUsername,
                       Long authorId,
                       boolean authorAdmin,
                       boolean friendAuthor,
                       long likes,
                       boolean liked,
                       long repliesCount,
                       Long parentId) {
        this.id = id;
        this.text = text;
        this.authorName = authorName;
        this.authorUsername = authorUsername;
        this.authorId = authorId;
        this.authorAdmin = authorAdmin;
        this.friendAuthor = friendAuthor;
        this.likes = likes;
        this.liked = liked;
        this.repliesCount = repliesCount;
        this.parentId = parentId;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public boolean isAuthorAdmin() {
        return authorAdmin;
    }

    public boolean isFriendAuthor() {
        return friendAuthor;
    }

    public long getLikes() {
        return likes;
    }

    public boolean isLiked() {
        return liked;
    }

    public long getRepliesCount() {
        return repliesCount;
    }

    public Long getParentId() {
        return parentId;
    }

    public List<CommentView> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    public void addReply(CommentView reply) {
        replies.add(reply);
    }
}
