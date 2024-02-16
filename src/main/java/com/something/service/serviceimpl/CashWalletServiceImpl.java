package com.something.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.CashWallet;
import com.something.repo.CashWalletRepo;
import com.something.service.CashWalletService;

@Service
public class CashWalletServiceImpl implements CashWalletService{
	@Autowired
	CashWalletRepo cwRepo;
	
	@Override
	public CashWallet findByUsername(String username) {
		// TODO Auto-generated method stub
		return cwRepo.findByUsername(username);
	}

	@Override
	public CashWallet updateBalance(CashWallet cw) {
		// TODO Auto-generated method stub
		return cwRepo.save(cw);
	}

	@Override
	public CashWallet createCashWallet(CashWallet cw) {
		// TODO Auto-generated method stub
		return cwRepo.save(cw);
	}

}
