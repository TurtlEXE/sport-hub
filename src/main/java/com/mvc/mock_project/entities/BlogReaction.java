package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.EmojiCode;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BlogReaction",
       uniqueConstraints = @UniqueConstraint(name = "UQ_BlogReaction", columnNames = {"post_id", "account_id", "emoji_code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlogReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "emoji_code", nullable = false, length = 30)
    private EmojiCode emojiCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
