package com.something.service.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.HistoryWallet;
import com.something.dto.StatDto;
import com.something.repo.HistoryWalletRepo;
import com.something.service.HistoryWalletService;

@Service
public class HistoryWalletServiceImpl implements HistoryWalletService {
	@Autowired
	HistoryWalletRepo hwRepo;

	@Override
	public HistoryWallet findById(int id) {
		// TODO Auto-generated method stub
		return hwRepo.findByWalletId(id);
	}

	@Override
	public HistoryWallet update(HistoryWallet hw) {
		// TODO Auto-generated method stub
		return hwRepo.save(hw);
	}

	@Override
	public List<HistoryWallet> findCommissionHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> !"Buy Package".equals(item.getType()) && !"Transfer".equals(item.getType()) && !"Withdraw".equals(item.getType()) && !"Deposit".equals(item.getType()) && !"Swap commission".equals(item.getType())).collect(Collectors.toList());
		
		return result;
	}

	@Override
	public List<HistoryWallet> findSwapHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Swap commission".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

	@Override
	public List<HistoryWallet> findDepositHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Deposit".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

	@Override
	public List<HistoryWallet> findWithdrawHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Withdraw".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

	@Override
	public List<HistoryWallet> findTransferHistoryByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> "Transfer".equals(item.getType())).collect(Collectors.toList());

		return result;
	}

	@Override
	public List<HistoryWallet> getAllHistory() {
		// TODO Auto-generated method stub
		return hwRepo.findAll();
	}

	@Override
	public List<HistoryWallet> findCommissionHistoryForDashboardByUsername(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryCommission = hwRepo.findByUsername(username);
		Collections.reverse(listHistoryCommission);

		List<HistoryWallet> result = listHistoryCommission.stream()
				.filter(item -> !"Buy Package".equals(item.getType()) && !"Transfer".equals(item.getType()) && !"Withdraw".equals(item.getType()) && !"Deposit".equals(item.getType()) && !"Swap commission".equals(item.getType())).collect(Collectors.toList());
		
		int limit = Math.min(result.size(), 10);
	    return result.subList(0, limit);

	}

	@Override
	public List<HistoryWallet> getTransferHistory() {
		// TODO Auto-generated method stub
		return hwRepo.findTransfer();
	}

	@Override
	public List<StatDto> get7Days(String username) {
		// TODO Auto-generated method stub
		List<HistoryWallet> listHistoryWallet = hwRepo.findByUsernameAndType(username);

	    Map<LocalDateTime, List<HistoryWallet>> historyByDateTime = listHistoryWallet.stream()
	            .collect(Collectors.groupingBy(
	                    history -> LocalDateTime.parse(history.getTime(), DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")),
	                    Collectors.toList()
	            ));

	    // Convert Map<LocalDateTime, List<HistoryWallet>> to Map<LocalDate, List<HistoryWallet>>
	    Map<LocalDate, List<HistoryWallet>> historyByDate = historyByDateTime.entrySet().stream()
	            .collect(Collectors.groupingBy(
	                    entry -> entry.getKey().toLocalDate(),
	                    Collectors.mapping(Map.Entry::getValue, Collectors.flatMapping(List::stream, Collectors.toList()))
	            ));

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	    List<StatDto> result = historyByDate.entrySet().stream()
	            .map(entry -> {
	                LocalDate date = entry.getKey();
	                String formattedDate = date.format(formatter);
	                List<HistoryWallet> histories = entry.getValue();

	                double totalAmount = histories.stream()
	                        .mapToDouble(HistoryWallet::getAmount)
	                        .sum();

	                return new StatDto(formattedDate, totalAmount);
	            })
	            .sorted(Comparator.comparing(StatDto::getTime))
	            .collect(Collectors.toList());

	    return result;
	}

	@Override
	public List<HistoryWallet> getByTime(String time) {
		// TODO Auto-generated method stub
		return hwRepo.findByTime(time);
	}

}
