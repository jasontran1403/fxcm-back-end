package com.something.service;

import java.util.List;

import com.something.domain.Investment;

public interface InvestmentService {
	public Investment findInvestmentByCode(String code);
	List<Investment> findAll();
	List<Investment> find5Invest();
	List<Investment> getAllInvestmentBy();
	public Investment save(Investment invest);
	public List<Investment> getAllInvestment();
	public Investment withdrawCapital(String code);
	public Investment updateProgress(String code);
	public List<Investment> getAllByUsername(String username);
	public List<Investment> getAllActiveByUsername(String username);
	double cal();
	double calculateSales(String username);
}
