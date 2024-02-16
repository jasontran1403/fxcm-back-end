package com.something.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.CashWallet;

public interface CashWalletRepo extends JpaRepository<CashWallet, Integer> {
	@Query(value = "SELECT * FROM cash_wallet WHERE username = ?1", nativeQuery = true)
	CashWallet findByUsername(String username);
}
