package com.something.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.something.dto.TransactionDto;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TransactionHistory {
	private static final String API_KEY = "ICD1VXC868YWNATPYTIQ3PIRR3J1U1EYYF";
	private static final String BASE_URL = "https://api.bscscan.com/api";
	private static final String BUSD_CONTRACT_ADDRESS = "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56";
	private static final String USDTBEP20_CONTRACT_ADDRESS = "0x55d398326f99059fF775485246999027B3197955";

	public static List<TransactionDto> fetchTransactionsBNB(String address, long time) {
		List<TransactionDto> transactions = new ArrayList<>();

		try {
			OkHttpClient client = new OkHttpClient();

			// Build the request URL with parameters
			String requestUrl = String.format(
					"%s?module=account&action=txlist&address=%s&startblock=0&endblock=99999999&sort=desc&apikey=%s",
					BASE_URL, address, API_KEY);

			Request request = new Request.Builder().url(requestUrl).build();

			Response response = client.newCall(request).execute();
			String json = response.body().string();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);

			if (rootNode.has("result") && rootNode.get("result").isArray()) {
				for (JsonNode txNode : rootNode.get("result")) {
					String hash = txNode.get("hash").asText();
					String from = txNode.get("from").asText();
					String to = txNode.get("to").asText();
					long timestamp = Long.parseLong(txNode.get("timeStamp").asText());
					double amount = Double.parseDouble(txNode.get("value").asText()) / 1e18;
					String value = String.valueOf(amount);
					String gasPrice = txNode.get("gasPrice").asText();
					String gasUsed = txNode.get("gasUsed").asText();
					String tokenDecimal = "18";
					String tokenSymbol = "BNB";
					
					System.out.println(txNode);

					if (txNode.has("tokenDecimal")) {
						tokenDecimal = txNode.get("tokenDecimal").asText();
					}

					if (txNode.has("tokenSymbol")) {
						tokenSymbol = txNode.get("tokenSymbol").asText();
					}

					String contractAddressFromNode = "";
					if (txNode.has("contractAddress")) {
						contractAddressFromNode = txNode.get("contractAddress").asText();
					}

					TransactionDto transaction = new TransactionDto();
					transaction.setHash(hash);
					transaction.setFrom(from);
					transaction.setTo(to);
					transaction.setTimeStamp(timestamp);
					transaction.setValue(new BigDecimal(value));
					transaction.setGasPrice(new BigDecimal(gasPrice));
					transaction.setGasUsed(new BigDecimal(gasUsed));
					transaction.setTokenDecimal(Integer.parseInt(tokenDecimal));
					transaction.setTokenSymbol(tokenSymbol);
					transaction.setContractAddress(contractAddressFromNode);

					transactions.add(transaction);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		for (Transaction tran : transactions) {
//			System.out.println(tran);
//		}

		List<TransactionDto> filteredTransactions = transactions.stream().filter(t -> t.getTo().equalsIgnoreCase(address))
				.toList();
		filteredTransactions = filteredTransactions.stream().filter(t -> t.getTimeStamp() >= time).toList();
		return filteredTransactions;
	}

	public static List<TransactionDto> fetchTransactionsBUSD(String address, long time) {
		List<TransactionDto> transactions = new ArrayList<>();

		try {
			OkHttpClient client = new OkHttpClient();

			// Build the request URL with parameters
			String requestUrl = String.format(
					"%s?module=account&action=tokentx&contractaddress=%s&address=%s&startblock=0&endblock=99999999&sort=desc&apikey=%s",
					BASE_URL, BUSD_CONTRACT_ADDRESS, address, API_KEY);

			Request request = new Request.Builder().url(requestUrl).build();

			Response response = client.newCall(request).execute();
			String json = response.body().string();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);

			if (rootNode.has("result") && rootNode.get("result").isArray()) {
				for (JsonNode txNode : rootNode.get("result")) {
					String hash = txNode.get("hash").asText();
					String from = txNode.get("from").asText();
					String to = txNode.get("to").asText();
					long timestamp = Long.parseLong(txNode.get("timeStamp").asText());
					String value = txNode.get("value").asText();
					String gasPrice = txNode.get("gasPrice").asText();
					String gasUsed = txNode.get("gasUsed").asText();
					String tokenDecimal = "18";
					String tokenSymbol = "BUSD";
					String contractAddressFromNode = txNode.get("contractAddress").asText();

					if (txNode.has("tokenDecimal")) {
						tokenDecimal = txNode.get("tokenDecimal").asText();
					}

					if (txNode.has("tokenSymbol")) {
						tokenSymbol = txNode.get("tokenSymbol").asText();
					}

					if (contractAddressFromNode.equalsIgnoreCase(BUSD_CONTRACT_ADDRESS)) {
						TransactionDto transaction = new TransactionDto();
						transaction.setHash(hash);
						transaction.setFrom(from);
						transaction.setTo(to);
						transaction.setTimeStamp(timestamp);
						double amount = Double.parseDouble(value) / 1e18;
						transaction.setValue(new BigDecimal(amount));
						transaction.setGasPrice(new BigDecimal(gasPrice));
						transaction.setGasUsed(new BigDecimal(gasUsed));
						transaction.setTokenDecimal(Integer.parseInt(tokenDecimal));
						transaction.setTokenSymbol(tokenSymbol);
						transaction.setContractAddress(contractAddressFromNode);

						transactions.add(transaction);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<TransactionDto> filteredTransactions = transactions.stream().filter(t -> t.getTo().equalsIgnoreCase(address))
				.toList();
		filteredTransactions = filteredTransactions.stream().filter(t -> t.getTimeStamp() >= time).toList();
		return filteredTransactions;
	}

	public static List<TransactionDto> fetchTransactionsUSDTBEP20(String address, long time) {
		List<TransactionDto> transactions = new ArrayList<>();

		try {
			OkHttpClient client = new OkHttpClient();

			// Build the request URL with parameters
			String requestUrl = String.format(
					"%s?module=account&action=tokentx&contractaddress=%s&address=%s&startblock=0&endblock=99999999&sort=desc&apikey=%s",
					BASE_URL, USDTBEP20_CONTRACT_ADDRESS, address, API_KEY);

			Request request = new Request.Builder().url(requestUrl).build();

			Response response = client.newCall(request).execute();
			String json = response.body().string();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);

			if (rootNode.has("result") && rootNode.get("result").isArray()) {
				for (JsonNode txNode : rootNode.get("result")) {
					String hash = txNode.get("hash").asText();
					String from = txNode.get("from").asText();
					String to = txNode.get("to").asText();
					long timestamp = Long.parseLong(txNode.get("timeStamp").asText());
					double amount = Double.parseDouble(txNode.get("value").asText()) / 1e18;
					String value = String.valueOf(amount);
					String gasPrice = txNode.get("gasPrice").asText();
					String gasUsed = txNode.get("gasUsed").asText();

					String tokenDecimal = "18";
					String tokenSymbol = "USDT BEP20";

					if (txNode.has("tokenDecimal")) {
						tokenDecimal = txNode.get("tokenDecimal").asText();
					}

					if (txNode.has("tokenSymbol")) {
						tokenSymbol = txNode.get("tokenSymbol").asText();
					}

					String contractAddressFromNode = USDTBEP20_CONTRACT_ADDRESS;

					TransactionDto transaction = new TransactionDto();
					transaction.setHash(hash);
					transaction.setFrom(from);
					transaction.setTo(to);
					transaction.setTimeStamp(timestamp);
					transaction.setValue(new BigDecimal(value));
					transaction.setGasPrice(new BigDecimal(gasPrice));
					transaction.setGasUsed(new BigDecimal(gasUsed));
					transaction.setTokenDecimal(Integer.parseInt(tokenDecimal));
					transaction.setTokenSymbol(tokenSymbol);
					transaction.setContractAddress(contractAddressFromNode);

					transactions.add(transaction);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<TransactionDto> filteredTransactions = transactions.stream().filter(t -> t.getTo().equalsIgnoreCase(address))
				.toList();
		filteredTransactions = filteredTransactions.stream().filter(t -> t.getTimeStamp() >= time).toList();
		return filteredTransactions;
	}

}
