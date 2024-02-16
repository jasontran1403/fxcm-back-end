package com.something.service.serviceimpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.CashWallet;
import com.something.domain.HistoryWallet;
import com.something.domain.Investment;
import com.something.repo.InvestmentRepo;
import com.something.service.CashWalletService;
import com.something.service.HistoryWalletService;
import com.something.service.InvestmentService;

@Service
public class InvestmentServiceImpl implements InvestmentService {
	@Autowired
	InvestmentRepo investRepo;

	@Autowired
	HistoryWalletService hwService;

	@Autowired
	CashWalletService cwService;

	@Override
	public Investment save(Investment invest) {
		// TODO Auto-generated method stub
		return investRepo.save(invest);
	}

	@Override
	public List<Investment> getAllInvestment() {
		// TODO Auto-generated method stub
		return investRepo.findAll();
	}

	@Override
	public Investment withdrawCapital(String code) {
		// TODO Auto-generated method stub
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDateTime = dateTime.format(formatter);
		String uuid = UUID.randomUUID().toString();

		Investment invest = investRepo.findByCode(code);

		double remain = invest.getRemain();
		double claimable = invest.getClaimable();

		if (claimable > 0) {
			invest.setRemain(remain - claimable);
			invest.setClaimable(0);
			investRepo.save(invest);

			HistoryWallet hw = new HistoryWallet();
			hw.setAmount(claimable);
			hw.setTime(formattedDateTime);
			hw.setType("Withdraw Capital");
			hw.setCode(uuid);
			hw.setHash("");
			hw.setStatus("success");
			hw.setFrominvestment(code);
			hw.setUsername(invest.getUsername());
			hwService.update(hw);

			CashWallet cw = cwService.findByUsername(invest.getUsername());
			cw.setBalance(cw.getBalance() + claimable);
			cwService.updateBalance(cw);

			// Xu ly - maxout neu co
		}
		return invest;
	}

	@Override
	public Investment updateProgress(String code) {
		// TODO Auto-generated method stub
		Investment invest = investRepo.findByCode(code);
		invest.setCount(invest.getCount() + 1);
		double capital = invest.getCapital();
		investRepo.save(invest);

		if (invest.getCount() % 30 == 0) {
			invest.setClaimable(invest.getClaimable() + capital * 10 / 100);
			investRepo.save(invest);
		}
		return invest;
	}
	
	@Override
	public List<Investment> getAllActiveByUsername(String username) {
		// TODO Auto-generated method stub
		List<Investment> listInvest = investRepo.findByUsername(username);
		List<Investment> listInvestmentActive = listInvest.stream().filter(item -> item.getRemain() > 0)
				.collect(Collectors.toList());
		return listInvestmentActive;
	}

	@Override
	public List<Investment> getAllByUsername(String username) {
		// TODO Auto-generated method stub
		List<Investment> listInvest = investRepo.findByUsername(username);
		
		return listInvest;
	}

	@Override
	public Investment findInvestmentByCode(String code) {
		// TODO Auto-generated method stub
		return investRepo.findByCode(code);
	}

	@Override
	public List<Investment> find5Invest() {
		// TODO Auto-generated method stub
		return investRepo.find5Invest();
	}

	@Override
	public double cal() {
		// TODO Auto-generated method stub
		List<Investment> invests = investRepo.findAll();
		double total = 0;
		for (Investment invest : invests) {
			total += invest.getCapital();
		}
		return total;
	}

	@Override
	public List<Investment> findAll() {
		// TODO Auto-generated method stub
		return investRepo.findAll();
	}

	@Override
	public double calculateSales(String username) {
		// TODO Auto-generated method stub
		try {
			return investRepo.calculateSales(username);
		} catch (Exception e) {
			return 0;
		}
		
	}

	@Override
	public List<Investment> getAllInvestmentBy() {
		// TODO Auto-generated method stub
		return investRepo.getAllInvestmentBy();
	}

}
