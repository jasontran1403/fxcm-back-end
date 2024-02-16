package com.something.service;

import com.something.domain.CashWallet;

public interface CashWalletService {
	public CashWallet findByUsername(String username);
	public CashWallet updateBalance(CashWallet cw);
	public CashWallet createCashWallet(CashWallet cw);
}
