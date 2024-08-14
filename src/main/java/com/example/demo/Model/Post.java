package com.example.demo.Model;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.Enum.StatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Column(name = "content")
    private String content;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    @OneToMany(mappedBy = "post")
    private List<Comment> comments;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Image> images;
    @ManyToMany(mappedBy = "likePosts")
    private List<User> likedByUsers;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp updatedAt;
}
