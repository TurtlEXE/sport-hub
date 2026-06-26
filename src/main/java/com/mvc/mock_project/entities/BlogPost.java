package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BlogPost")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_account_id", nullable = false)
    private Account author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(nullable = false, length = 20)
    private String status; // PUBLISHED, DRAFT

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
