package com.something.service;

import dev.samstevens.totp.exceptions.QrGenerationException;

public interface AuthenticatorService {
	public String generateSecretKey();
	public String getQRCode(String secret) throws QrGenerationException;
	public boolean verifyTotp(String code, String secret);
}
