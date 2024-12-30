package com.ktds.rcsp.auth.repository;

import com.ktds.rcsp.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>{
    Optional<User> findByMasterIdAndUserId(String masterId, String userId);
}
