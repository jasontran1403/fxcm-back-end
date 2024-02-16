package com.something.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.Pack;

public interface PackRepo extends JpaRepository<Pack, Integer> {
	@Query(value="select * from pack where name = ?1", nativeQuery = true)
	List<Pack> findByName(String name);
}
