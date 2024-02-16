package com.something.service;

import com.something.domain.CommissionWallet;

public interface CommissionWalletService {
	public CommissionWallet findByUsername(String username);
	public CommissionWallet updateBalance(CommissionWallet cmw);
	public CommissionWallet createCommissionWallet(CommissionWallet cmw);
}
