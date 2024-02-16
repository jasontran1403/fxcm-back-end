package com.something.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.User;


public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    
    @Query(value = "select * from user where root_username = ?1", nativeQuery = true)
    User findByRootUsername(String rootUsername);
    
    @Query(value = "select * from user where root_username = ?1", nativeQuery = true)
    List<User> findListByRootUsername(String rootUsername);
    
    @Query(value = "SELECT * FROM user WHERE email = ?1", nativeQuery = true)
    User findByEmail(String email);
    
    @Query(value = "SELECT * FROM user WHERE username = ?1", nativeQuery = true)
    User findByPlacement(String username);
    
    @Query(value = "SELECT * FROM user WHERE placement = ?1", nativeQuery = true)
    List<User> findAllByPlacement(String username);
    
    @Query(value = "SELECT * FROM user WHERE root_username = ?1", nativeQuery = true)
    List<User> findByRoot(String username);
    
    @Query(value = "SELECT * FROM user WHERE root_username = ?1 and placement = ?2", nativeQuery = true)
    List<User> findByRootAndPlacement(String root, String placement);
    
    @Query(value = "SELECT * FROM user WHERE root_username = ?1 and placement = ?2", nativeQuery = true)
    List<User> findNextByRootAndPlacement(String root, String placement);
    
    @Query(value = "SELECT * FROM user WHERE root_username = ?1", nativeQuery = true)
    User getOneByRoot(String username);
    
    @Query(value = "SELECT * FROM user", nativeQuery = true)
    List<User> findByKycStatus(int status);
    
    @Query(value = "select * from user where ref = ?1", nativeQuery = true)
    User findByRefLink(String ref);
}
