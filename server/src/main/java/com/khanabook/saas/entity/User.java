package com.khanabook.saas.entity;

import com.khanabook.saas.sync.entity.BaseSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseSyncEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private String createdAt;
}
