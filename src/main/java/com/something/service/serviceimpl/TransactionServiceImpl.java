package com.something.service.serviceimpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.Transaction;
import com.something.domain.User;
import com.something.repo.TransactionRepo;
import com.something.service.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService{
	@Autowired
	TransactionRepo tranRepo;

	@Override
	public List<Transaction> findTransactionByUserId(long userId) {
		// TODO Auto-generated method stub
		return tranRepo.findByUserId(userId);
	}

	@Override
	public void saveTransaction(User user, double amount) {
		// TODO Auto-generated method stub
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000;
		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setStatus(false);
		transaction.setHash("");
		transaction.setUser(user);
		transaction.setTime(timestamp);
		transaction.setType("Deposit");
		transaction.setNetwork("BSC");
		
		tranRepo.save(transaction);
	}

	@Override
	public void cancelTransaction(long transactionId) {
		// TODO Auto-generated method stub
		Optional<Transaction> tran = tranRepo.findById(transactionId);
		
		if (tran.isEmpty()) {
			throw new RuntimeException("This transaction id " + transactionId + " is not existed!");
		}
		
		tranRepo.delete(tran.get());
	}

	@Override
	public void updateTransaction(long transactionId, String hash, double actualAmount) {
		// TODO Auto-generated method stub
		Transaction tran = tranRepo.findById(transactionId).get();
		tran.setStatus(true);
		tran.setHash(hash);
		tran.setAmount(actualAmount);
		
		tranRepo.save(tran);
	}

	@Override
	public List<Transaction> findTransactionByStatus(boolean status) {
		// TODO Auto-generated method stub
		return tranRepo.findDepositByStatus(status, "Deposit");
	}

	@Override
	public List<Transaction> findTransactionByUserIdAndType(long userId, String type) {
		// TODO Auto-generated method stub
		return tranRepo.findByUserIdAndType(userId, type);
	}

	@Override
	public List<Transaction> getAllTransactionByType(String type) {
		// TODO Auto-generated method stub
		return tranRepo.findByType(type);
	}

	@Override
	public double totalAmountByType(String type) {
		// TODO Auto-generated method stub
		List<Transaction> trans = tranRepo.findByType(type);
		double total = 0;
		for (Transaction tran : trans) {
			total += tran.getAmount();
		}
		return total;
	}

	@Override
	public void saveWithdraw(User user, double amount, String address) {
		// TODO Auto-generated method stub
		Transaction transaction = new Transaction();
		Date currentDateTime = new Date();
		
		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000;
		transaction.setAmount(amount);
		transaction.setStatus(false);
		transaction.setHash("");
		transaction.setUser(user);
		transaction.setTime(timestamp);
		transaction.setType("Withdraw");
		transaction.setNetwork("BSC");
		
		tranRepo.save(transaction);
	}

}
