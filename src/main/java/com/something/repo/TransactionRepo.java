package com.something.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.something.domain.Transaction;
import com.something.dto.TransactionHistoryDto;

public interface TransactionRepo extends JpaRepository<Transaction, Long>{

	@Query(value="select * from transaction where user_id = ?1", nativeQuery = true)
	List<Transaction> findByUserId(long userId);
	
	@Query(value="select * from transaction where user_id = ?1 and type = ?2", nativeQuery = true)
	List<Transaction> findByUserIdAndType(long userId, String type);
	
	@Query(value="select * from transaction where status = ?1", nativeQuery = true)
	List<Transaction> findByStatus(boolean status);
	
	@Query(value="select * from transaction where status = ?1 and type = ?2", nativeQuery = true)
	List<Transaction> findDepositByStatus(boolean status, String type);
	
	@Query(value="select * from transaction where type = ?1", nativeQuery = true)
	List<Transaction> findByType(String type);
	
	@Query(value="select sum(amount) from transaction where type = ?1", nativeQuery = true)
	double totalAmountByType(String type);
}
