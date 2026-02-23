package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.SQLJoinTableRestriction;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;


    @JoinColumn(name = "reciever_id",  nullable = false)
    private User reciever;



}
