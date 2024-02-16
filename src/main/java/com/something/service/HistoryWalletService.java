package com.something.service;

import java.util.List;

import com.something.domain.HistoryWallet;
import com.something.dto.StatDto;

public interface HistoryWalletService {
	HistoryWallet findById(int id);
	HistoryWallet update(HistoryWallet hw);
	List<HistoryWallet> getAllHistory();
	List<HistoryWallet> findCommissionHistoryByUsername(String username);
	List<HistoryWallet> findCommissionHistoryForDashboardByUsername(String username);
	List<HistoryWallet> findSwapHistoryByUsername(String username);
	List<HistoryWallet> findDepositHistoryByUsername(String username);
	List<HistoryWallet> findWithdrawHistoryByUsername(String username);
	List<HistoryWallet> findTransferHistoryByUsername(String username);
	List<HistoryWallet> getTransferHistory();
	List<StatDto> get7Days(String username);
	List<HistoryWallet> getByTime(String time);
}
