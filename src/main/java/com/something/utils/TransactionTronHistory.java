package com.something.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.something.dto.TransactionDto;


public class TransactionTronHistory {
//    public static String fetchTRX(String walletAddress, long timeStart) throws IOException {
//    	String endpointUrl = "https://apilist.tronscanapi.com/api/transfer/trx?address=" + walletAddress + "&start=0&limit=20&direction=0&reverse=true&fee=true&db_version=1&start_timestamp=" + timeStart + "&end_timestamp=";
//        
//        URL url = new URL(endpointUrl);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//
//        // Lấy phản hồi từ server
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line;
//            StringBuilder response = new StringBuilder();
//
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            reader.close();
//            
//            return response.toString();
//        } else {
//            throw new IOException("Lỗi khi gọi API: " + responseCode);
//        }
//    }
    
    public static List<TransactionDto> fetchTRX(String walletAddress, long timeStart) throws IOException {
    	String endpointUrl = "https://apilist.tronscanapi.com/api/transfer/trx?address=" + walletAddress + "&start=0&limit=20&direction=0&reverse=true&fee=true&db_version=1&start_timestamp=" + timeStart + "&end_timestamp=";
        
    	URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Lấy phản hồi từ server
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.toString());
            JsonNode dataNode = root.get("data");
            JsonNode tokenInfoNode = root.get("tokenInfo");

            
            String tokenSymbol = tokenInfoNode.get("tokenAbbr").asText();
            

            // Convert JSON data to List<Transaction>
            List<TransactionDto> transactions = new ArrayList<>();
            for (JsonNode transactionNode : dataNode) {
            	BigDecimal amount = new BigDecimal(transactionNode.get("amount").asText());
                int decimals = transactionNode.get("decimals").asInt();
                BigDecimal divisor = BigDecimal.TEN.pow(decimals); // 10^decimals
                BigDecimal actualValue = amount.divide(divisor);

                TransactionDto transaction = new TransactionDto();
                transaction.setHash(transactionNode.get("hash").asText());
                transaction.setFrom(transactionNode.get("from").asText());
                transaction.setTo(transactionNode.get("to").asText());
                transaction.setTimeStamp(transactionNode.get("block_timestamp").asLong());
                transaction.setValue(actualValue);
                transaction.setTokenDecimal(decimals);
                transaction.setTokenSymbol(tokenSymbol);
                transaction.setContractAddress("TRON NETWORK");
                transaction.setStatus(transactionNode.get("contract_ret").asText());

                transactions.add(transaction);
            }

            return transactions;
        } else {
            throw new IOException("Lỗi khi gọi API: " + responseCode);
        }
    }
    
//    public static String fetchUSDTTRC20(String walletAddress, long timeStart) throws IOException {
//        String endpointUrl = "https://apilist.tronscanapi.com/api/transfer/trc20?address=" + walletAddress + "&trc20Id=TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t&start=0&limit=20&direction=0&reverse=true&db_version=1&start_timestamp=" + timeStart + "&end_timestamp=";
//        URL url = new URL(endpointUrl);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//
//        // Lấy phản hồi từ server
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line;
//            StringBuilder response = new StringBuilder();
//
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            reader.close();
//            
//            return response.toString();
//        } else {
//            throw new IOException("Lỗi khi gọi API: " + responseCode);
//        }
//    }
    
    public static List<TransactionDto> fetchUSDTTRC20(String walletAddress, long timeStart) throws IOException {
        String endpointUrl = "https://apilist.tronscanapi.com/api/transfer/trc20?address=" + walletAddress + "&trc20Id=TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t&start=0&limit=20&direction=0&reverse=true&db_version=1&start_timestamp=" + timeStart + "&end_timestamp=";
        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Lấy phản hồi từ server
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.toString());
            JsonNode dataNode = root.get("data");
            JsonNode tokenInfoNode = root.get("tokenInfo");

            
            String tokenSymbol = tokenInfoNode.get("tokenAbbr").asText();

            // Convert JSON data to List<Transaction>
            List<TransactionDto> transactions = new ArrayList<>();
            for (JsonNode transactionNode : dataNode) {
            	BigDecimal amount = new BigDecimal(transactionNode.get("amount").asText());
                int decimals = transactionNode.get("decimals").asInt();
                BigDecimal divisor = BigDecimal.TEN.pow(decimals); // 10^decimals
                BigDecimal actualValue = amount.divide(divisor);
                
                TransactionDto transaction = new TransactionDto();
                transaction.setHash(transactionNode.get("hash").asText());
                transaction.setFrom(transactionNode.get("from").asText());
                transaction.setTo(transactionNode.get("to").asText());
                transaction.setTimeStamp(transactionNode.get("block_timestamp").asLong());
                transaction.setValue(actualValue);
                transaction.setTokenDecimal(decimals);
                transaction.setTokenSymbol(tokenSymbol);
                transaction.setContractAddress(transactionNode.get("id").asText());
                transaction.setStatus(transactionNode.get("contract_ret").asText());

                transactions.add(transaction);
            }

            return transactions;
        } else {
            throw new IOException("Lỗi khi gọi API: " + responseCode);
        }
    }

}
