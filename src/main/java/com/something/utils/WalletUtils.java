package com.something.utils;

import java.math.BigInteger;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import com.something.dto.WalletResponse;

public class WalletUtils {
	public static WalletResponse generateBSCWallet() throws Exception {
		WalletResponse response = new WalletResponse();
		ECKeyPair keyPair = Keys.createEcKeyPair();
        byte[] privateKeyBytes = keyPair.getPrivateKey().toByteArray();
        byte[] publicKeyBytes = keyPair.getPublicKey().toByteArray();
        
        String privateKey = Hex.toHexString(privateKeyBytes);
        String publicKey = Hex.toHexString(publicKeyBytes);
        String address = Keys.toChecksumAddress(Keys.getAddress(keyPair.getPublicKey()));
        
        System.out.println("Private key: " + privateKey);
        System.out.println("Public key: " + publicKey);
        System.out.println("Address: " + address);
        
        response.setAddress(address);
        response.setPublicKey(publicKey);
        response.setSecretKey(privateKey);
        return response;
	}
	
	public static double getBNBBalance(String privateKey) throws Exception {
		// Set up web3j instance and credentials
		Web3j web3 = Web3j.build(new HttpService("https://bsc-dataseed1.binance.org:443"));

		Credentials credentials = Credentials.create(privateKey);
		
		EthGetBalance balance = web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
		BigInteger bnbBalance = balance.getBalance();
		return bnbBalance.doubleValue() / 1.0e+18;
	}
}
