package com.something.dto;

import java.util.List;

import com.something.domain.Investment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticDto {
	private List<Investment> listInvestments;
	private double totalInvestment;
	private double totalDeposit;
	private double totalWithdraw;
	private double bnbWallet;
}
