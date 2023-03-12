package com.example.dailychallenge.repository.badge;

import com.example.dailychallenge.entity.badge.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
}