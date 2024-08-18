package com.example.demo.Model;

import com.example.demo.Enum.FriendStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "friend")
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_requester_id", referencedColumnName = "id")
    private User friendRequester;

    @ManyToOne
    @JoinColumn(name = "user_receiver_id", referencedColumnName = "id")
    private User friendReceiver;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FriendStatusEnum status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", updatable = false, nullable = false)
    private Timestamp updatedAt;
}
