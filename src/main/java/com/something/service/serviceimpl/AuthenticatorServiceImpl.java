package com.something.service.serviceimpl;

import org.springframework.stereotype.Service;

import com.something.service.AuthenticatorService;

import dev.samstevens.totp.exceptions.QrGenerationException;

@Service
public class AuthenticatorServiceImpl implements AuthenticatorService {

	@Override
    public boolean verifyTotp(String code, String secret) {
        return false;
    }

	@Override
	public String generateSecretKey() {
		// TODO Auto-generated method stub
		
		return "OK";
	}

	@Override
	public String getQRCode(String secret) throws QrGenerationException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
