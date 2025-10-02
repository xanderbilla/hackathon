package com.hackathon.auth.repository;

import com.hackathon.auth.entity.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocalUserRepository extends JpaRepository<LocalUser, Integer> {

    Optional<LocalUser> findByUsername(String username);

    Optional<LocalUser> findByAadhaarNumber(String aadhaarNumber);

    boolean existsByUsername(String username);

    boolean existsByAadhaarNumber(String aadhaarNumber);
}