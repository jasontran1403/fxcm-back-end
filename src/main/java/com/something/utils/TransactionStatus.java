package com.something.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class TransactionStatus {
	private static final String API_KEY = "ICD1VXC868YWNATPYTIQ3PIRR3J1U1EYYF";
	
	public static void checkTransactionStatus(String txHash) throws IOException {
        String url = "https://api.bscscan.com/api?module=transaction&action=gettxreceiptstatus&txhash=" + txHash  + "&apikey=" + API_KEY;
//        String url = "https://api-testnet.bscscan.com/api?module=transaction&action=gettxreceiptstatus&txhash=" + txHash + "&apikey=" + API_KEY;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        
        // Setting HTTP request header
        con.setRequestMethod("GET");
        
        // Reading HTTP response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        JSONObject jsonObj = new JSONObject(response.toString());
        // Parsing JSON response to check transaction status
        String status = jsonObj.getJSONObject("result").getString("status");
        
        if (status.equals("0")) {
            System.out.println("Transaction " + txHash + " failed.");
        } else if (status.equals("1")) {
            System.out.println("Transaction " + txHash + " succeeded.");
        } else {
            System.out.println("Transaction " + txHash + " status unknown.");
        }
    }
}
