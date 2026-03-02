package org.example.isc.main.secured.post;

import org.example.isc.main.secured.models.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentView {

    private final Comment comment;
    private final long likes;
    private final boolean liked;
    private final long repliesCount;
    private final List<CommentView> replies = new ArrayList<>();

    public CommentView(Comment comment, long likes, boolean liked, long repliesCount) {
        this.comment = comment;
        this.likes = likes;
        this.liked = liked;
        this.repliesCount = repliesCount;
    }

    public Comment getComment() {
        return comment;
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

    public List<CommentView> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    public void addReply(CommentView reply) {
        replies.add(reply);
    }
}
