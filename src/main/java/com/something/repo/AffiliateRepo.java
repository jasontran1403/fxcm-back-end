package com.something.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.Affiliate;

public interface AffiliateRepo extends JpaRepository<Affiliate, String>{
	@Query(value = "SELECT * FROM affiliate where uuid=?1", nativeQuery = true)
    Affiliate findByUUID(String uuid);
	
	@Query(value = "SELECT * FROM affiliate where root=?1", nativeQuery = true)
    List<Affiliate> findByRoot(String root);
	
	@Query(value = "SELECT * FROM affiliate where placement=?1 and side=?2", nativeQuery = true)
    Affiliate findByPlacementAndSide(String placement, String side);
	
	@Query(value = "SELECT * FROM affiliate where placement=?1", nativeQuery = true)
    List<Affiliate> findByPlacement(String placement);
}
