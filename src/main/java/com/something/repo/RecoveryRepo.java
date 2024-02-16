package com.something.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.Recovery;

public interface RecoveryRepo extends JpaRepository<Recovery, String>{
	@Query(value = "SELECT * FROM recovery where uuid=?1", nativeQuery = true)
    Recovery findByUUID(String uuid);

}
