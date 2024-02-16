package com.something.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

public class Transfer {
	public static String sendBNB(String privateKey, String toWalletAdress, double amount) throws Exception {
		// Set up web3j instance and credentials
		Web3j web3 = Web3j.build(new HttpService("https://bsc-dataseed1.binance.org:443"));

		Credentials credentials = Credentials.create(privateKey);

		// Correcting the amount to real type
		amount = amount * 1e18;

		// Get the latest nonce
		EthGetTransactionCount ethGetTransactionCount = web3
				.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
		BigInteger nonce = ethGetTransactionCount.getTransactionCount();

		// Build the transaction

		String recipientAddress = toWalletAdress;

		BigInteger gasPrice = Convert.toWei("5", Convert.Unit.GWEI).toBigInteger();
		BigInteger gasLimit = BigInteger.valueOf(21_000);
		
		EthGetBalance balance = web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();

		BigInteger bnbBalance = balance.getBalance(); //200000000000000
		
		BigInteger gasCost = gasPrice.multiply(gasLimit); //1000000000000000
		BigDecimal test = Convert.toWei(String.valueOf(amount), Convert.Unit.WEI);
		System.out.println(bnbBalance + " -- " + gasCost + " -- " + test);

		RawTransaction transaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, recipientAddress,
				Convert.toWei(String.valueOf(amount), Convert.Unit.WEI).toBigInteger());

		// Sign the transaction
		byte[] signedMessage = TransactionEncoder.signMessage(transaction, credentials);
		String hexValue = Numeric.toHexString(signedMessage);

		// Send the transaction
		EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
		if (ethSendTransaction.getError() != null) {
			System.out.println(ethSendTransaction.getError().getMessage());
			return ethSendTransaction.getError().getMessage();
		}
		String transactionHash = ethSendTransaction.getTransactionHash();

		// Wait for transaction to be mined
		Optional<TransactionReceipt> transactionReceipt = null;
		while (transactionReceipt == null) {
			Thread.sleep(1000);
			transactionReceipt = web3.ethGetTransactionReceipt(transactionHash).sendAsync().get()
					.getTransactionReceipt();
		}

		if (transactionHash == null) {
			return "Transaction failed: error";
		} else {
			return transactionHash;
		}
	}

	public static String sendBUSD(String privateKey, String toAddress, double amount) throws Exception {
		// Set up web3j instance and credentials
		Web3j web3 = Web3j.build(new HttpService("https://bsc-dataseed1.binance.org:443"));
//		Web3j web3 = Web3j.build(new HttpService("https://data-seed-prebsc-1-s1.binance.org:8545"));

		Credentials credentials = Credentials.create(privateKey);

		amount = amount * 1e18;

		// Smartcontact of USDT
		String smartContract = "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56";
//		String smartContract = "0xeD24FC36d5Ee211Ea25A80239Fb8C4Cfd80f12Ee";

		// Load the USDT contract
		ERC20 busd = ERC20.load(smartContract, web3, credentials, new DefaultGasProvider());

		// Calculate the amount in the token's base unit (i.e., 18 decimal places for
		// USDT)
		BigInteger tokenAmount = Convert.toWei(String.valueOf(amount), Convert.Unit.WEI).toBigInteger();

		// Get the latest nonce
		EthGetTransactionCount ethGetTransactionCount = web3
				.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
		BigInteger nonce = ethGetTransactionCount.getTransactionCount();

		// Build the transaction
		BigInteger gasPrice = Convert.toWei("10", Convert.Unit.GWEI).toBigInteger(); // set gas price to 10 Gwei
		BigInteger gasLimit = BigInteger.valueOf(100000); // set gas limit to 100,000

		RawTransaction transaction = RawTransaction
				.createTransaction(nonce, gasPrice, gasLimit, busd.getContractAddress(),
						FunctionEncoder.encode(new Function("transfer",
								Arrays.asList(new Address(toAddress), new Uint256(tokenAmount)),
								Arrays.asList(new TypeReference<Type>() {
								}))));

		// Sign the transaction
		byte[] signedMessage = TransactionEncoder.signMessage(transaction, credentials);
		String hexValue = Numeric.toHexString(signedMessage);

		// Send the transaction
		EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
		String transactionHash = ethSendTransaction.getTransactionHash();

		// Wait for transaction to be mined
		Optional<TransactionReceipt> transactionReceipt = null;
		while (transactionReceipt == null) {
			Thread.sleep(1000);
			transactionReceipt = web3.ethGetTransactionReceipt(transactionHash).sendAsync().get()
					.getTransactionReceipt();
		}

		if (transactionHash == null) {
			return "Transaction failed: not enough fee";
		} else {
			return transactionHash;
		}

	}

	public static String sendUSDTBEP20(String privateKey, String toAddress, double amount) throws Exception {
		// Set up web3j instance and credentials
		Web3j web3 = Web3j.build(new HttpService("https://bsc-dataseed1.binance.org:443"));
		
		Credentials credentials = Credentials.create(privateKey);

		amount = amount * 1e18;

		// Smartcontact of USDT
		String smartContract = "0x55d398326f99059fF775485246999027B3197955";

		// Load the USDT contract
		ERC20 usdtToken = ERC20.load(smartContract, web3, credentials, new DefaultGasProvider());
		
		BigInteger usdtBalance = usdtToken.balanceOf(credentials.getAddress()).send();

		// Calculate the amount in the token's base unit (i.e., 18 decimal places for USDT)
		BigInteger tokenAmount = Convert.toWei(String.valueOf(amount), Convert.Unit.WEI).toBigInteger();

		// Get the latest nonce
		EthGetTransactionCount ethGetTransactionCount = web3
				.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
		BigInteger nonce = ethGetTransactionCount.getTransactionCount();

		// Build the transaction
		BigInteger gasPrice = Convert.toWei("5", Convert.Unit.GWEI).toBigInteger();
		BigInteger gasLimit = BigInteger.valueOf(40000); 
		
		RawTransaction transaction = RawTransaction
				.createTransaction(nonce, gasPrice, gasLimit, usdtToken.getContractAddress(),
						FunctionEncoder.encode(new Function("transfer",
								Arrays.asList(new Address(toAddress), new Uint256(usdtBalance)),
								Arrays.asList(new TypeReference<Type>() {
								}))));

		// Sign the transaction
		byte[] signedMessage = TransactionEncoder.signMessage(transaction, credentials);
		String hexValue = Numeric.toHexString(signedMessage);
		
		EthGetBalance balance = web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();

		BigInteger bnbBalance = balance.getBalance(); //200000000000000
		
		BigInteger gasCost = gasPrice.multiply(gasLimit); //1000000000000000

		// Send the transaction
		EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
		if (ethSendTransaction.getError() != null) {
			System.out.println(ethSendTransaction.getError().getMessage());
			return ethSendTransaction.getError().getMessage();
		}
		String transactionHash = ethSendTransaction.getTransactionHash();

		// Wait for transaction to be mined
		Optional<TransactionReceipt> transactionReceipt = null;
		while (transactionReceipt == null) {
			Thread.sleep(1000);
			transactionReceipt = web3.ethGetTransactionReceipt(transactionHash).sendAsync().get()
					.getTransactionReceipt();
		}

		if (transactionHash == null) {
			return "Transaction failed: error";
		} else {
			return transactionHash;
		}
	}

}