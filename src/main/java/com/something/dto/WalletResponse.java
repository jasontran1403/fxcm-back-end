package com.something.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {
	private String address;
	private String publicKey;
	private String secretKey;
}
