package com.dev.hotelbooking.model;

import com.dev.hotelbooking.enums.ConfirmationType;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "confirmations")
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Confirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "confirmation_key", unique = true, nullable = false, length = 255)
    String key;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("user_id")
    User user;

    @Enumerated(EnumType.STRING)
    ConfirmationType type;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    public Confirmation(User user, ConfirmationType type) {
        this.user = user;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.key = UUID.randomUUID().toString();
    }
}
