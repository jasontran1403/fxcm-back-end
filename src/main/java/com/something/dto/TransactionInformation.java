package com.something.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInformation {
	private String privateKey;
	private String toAddressWallet;
	private double amount;
}
