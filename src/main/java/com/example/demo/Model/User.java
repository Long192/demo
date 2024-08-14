package com.example.demo.Model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.Enum.RoleEnum;
import com.example.demo.Enum.StatusEnum;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "fullname")
    @Nullable
    private String fullname;
    @Column(name = "address")
    @Nullable
    private String address;
    @Column(name = "etc")
    @Nullable
    private String etc;
    @Column(name = "avatar")
    @Nullable
    private String avatar;
    @Column(name = "dob")
    @Nullable
    private Date dob;
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    private Timestamp updatedAt;
    @ManyToMany
    @JoinTable(name = "favourite", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id"))
    private List<Post> likePosts;
    @OneToMany(mappedBy = "user")
    private List<Post> posts;
    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }
}
