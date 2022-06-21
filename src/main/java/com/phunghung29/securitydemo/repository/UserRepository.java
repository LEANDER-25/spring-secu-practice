package com.phunghung29.securitydemo.repository;

import com.phunghung29.securitydemo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.age <= ?1")
    List<User> findByLessOrEqualThanAge(Integer age);

    @Query("SELECT u FROM User u WHERE u.age <= ?1")
    Page<User> findByLessOrEqualThanAge(Integer age, Pageable pageable);

    @Query(value = "SELECT * FROM users u WHERE u.gender like :gender", nativeQuery = true)
    List<User> findByGender(@Param("gender") String gender);
}
