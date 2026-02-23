package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import org.example.isc.main.enums.FriendsStatusEnum;

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
    private FriendsStatusEnum status;

}
