package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import org.example.isc.main.enums.NotificationEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    public Notification(User receiver, User sender, NotificationEnum type, String title, String body, LocalDateTime createdAt, LocalDateTime readAt, String data) {
        this.receiver = receiver;
        this.sender = sender;
        this.type = type;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.data = data;
    }

    public Notification() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationEnum type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "data")
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public NotificationEnum getType() {
        return type;
    }

    public void setType(NotificationEnum type) {
        this.type = type;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }
}
