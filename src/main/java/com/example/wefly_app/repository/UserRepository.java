package com.example.wefly_app.repository;

import com.example.wefly_app.entity.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("FROM User u WHERE LOWER(u.email) = LOWER(?1)")
    User findOneByEmail(String email);

    @Query("FROM User u WHERE u.otp = ?1")
    User findOneByOTP(String otp);

    @Query("FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    User checkExistingEmail(String email);

//    @Query("FROM User u WHERE LOWER(u.username) = LOWER(?1)")
//    User findOneByUsername(String username);
//
//    @Query("FROM User u WHERE LOWER(u.username) = LOWER(:username)")
//    User checkExistingUsername(String username);
}