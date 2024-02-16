package com.something.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.Activation;

public interface ActivationRepo extends JpaRepository<Activation, String>{
	@Query(value = "SELECT * FROM activation where uuid=?1", nativeQuery = true)
    Activation findByUUID(String uuid);
	
	@Query(value = "SELECT * FROM activation where username=?1", nativeQuery = true)
    Activation findByUsername(String username);
}
