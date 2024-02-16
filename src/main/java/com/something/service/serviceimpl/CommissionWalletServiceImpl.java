package com.something.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.CommissionWallet;
import com.something.repo.CommissionWalletRepo;
import com.something.service.CommissionWalletService;

@Service
public class CommissionWalletServiceImpl implements CommissionWalletService{
	@Autowired
	CommissionWalletRepo cmwRepo;

	@Override
	public CommissionWallet findByUsername(String username) {
		// TODO Auto-generated method stub
		return cmwRepo.findByUsername(username);
	}

	@Override
	public CommissionWallet updateBalance(CommissionWallet cmw) {
		// TODO Auto-generated method stub
		return cmwRepo.save(cmw);
	}

	@Override
	public CommissionWallet createCommissionWallet(CommissionWallet cmw) {
		// TODO Auto-generated method stub
		return cmwRepo.save(cmw);
	}

}
