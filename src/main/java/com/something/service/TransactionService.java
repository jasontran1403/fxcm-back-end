package com.something.service;

import java.util.List;

import com.something.domain.Transaction;
import com.something.domain.User;

public interface TransactionService {
	List<Transaction> findTransactionByUserId(long userId);
	List<Transaction> findTransactionByStatus(boolean status);
	List<Transaction> findTransactionByUserIdAndType(long userId, String type);
	List<Transaction> getAllTransactionByType(String type);
	double totalAmountByType(String type);
	void saveTransaction(User user, double amount);
	void cancelTransaction(long transactionId);
	void updateTransaction(long transactionId, String hash, double actualAmount);
	void saveWithdraw(User user, double amount, String address);
}
