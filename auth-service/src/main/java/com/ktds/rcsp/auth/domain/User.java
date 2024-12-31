package com.ktds.rcsp.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "master_id", nullable = false)
    private String masterId;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String status;
    
//    @Column(name = "login_attempt")
//    private int loginAttempt;
}
