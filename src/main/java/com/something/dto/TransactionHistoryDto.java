package com.something.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryDto {
	private Long id;
	private double amount;
	private String hash;
	private boolean status;
	private String type;
	private long time;

}
