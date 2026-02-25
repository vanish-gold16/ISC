package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.enums.converter.FriendsStatusEnumConverter;

@Entity
@Table(name="friends")
public class Friends {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(name="sender_id")
    private User senderUser;

    @ManyToOne
    @JoinColumn(name="reciever_id")
    private User recieverUser;

    @Column(name="status")
    @Convert(converter = FriendsStatusEnumConverter.class)
    private FriendsStatusEnum status;

    public Friends(User senderUser, User recieverUser, FriendsStatusEnum status) {
        this.senderUser = senderUser;
        this.recieverUser = recieverUser;
        this.status = status;
    }

    public Friends() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public User getSenderUser() {
        return senderUser;
    }

    public void setSenderUser(User senderUser) {
        this.senderUser = senderUser;
    }

    public User getRecieverUser() {
        return recieverUser;
    }

    public void setRecieverUser(User recieverUser) {
        this.recieverUser = recieverUser;
    }

    public FriendsStatusEnum getStatus() {
        return status;
    }

    public void setStatus(FriendsStatusEnum status) {
        this.status = status;
    }
}
