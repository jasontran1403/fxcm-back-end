package com.something.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.CommissionWallet;

public interface CommissionWalletRepo extends JpaRepository<CommissionWallet, Integer>{
	@Query(value = "SELECT * FROM commission_wallet WHERE username = ?1", nativeQuery = true)
	CommissionWallet findByUsername(String username);
}
