package com.something;


import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.something.domain.CashWallet;
import com.something.domain.Transaction;
import com.something.dto.TransactionDto;
import com.something.service.CashWalletService;
import com.something.service.TransactionService;
import com.something.utils.TransactionHistory;
import com.something.utils.Transfer;

@SpringBootApplication
@EnableScheduling
public class UserServiceApplication {
	@Autowired
	TransactionService tranService;
	
	@Autowired
	CashWalletService cwService;

	public static void main(String[] args) throws IOException{
		SpringApplication.run(UserServiceApplication.class, args);
	}
	
	private static final String OWNER_WALLET = "0x5aAE95a2c8280820d8DB3e333B36Ba03cf469C8B";
	private static final String ADMIN_WALLET = "0x0b2295F3a0E4ACaC2d3827E594EC807bf08b8908";
	private static final String ADMIN_PUBLIC_KEY = "2713a9b17a6672949715759e2d76d48c4457753b693990ea3962662f42bdad12830094e939c2c771f954060ffa12220a549953190030fd6d1eddd18b8b13b7c8";
	private static final String ADMIN_PRIVATE_KEY = "44cb641f0f3e293a46879bdbf1f3f3b0d61a834dc7423dce3f9a669290d16ec0";
	private static final double BNB_FEE = 0.0002;
	@Scheduled(cron = "* */1 * ? * *")
	public void validateDeposits() throws Exception {
		List<Transaction> listTrans = tranService.findTransactionByStatus(false);
		for (Transaction tran : listTrans) {
			List<TransactionDto> transactionsDto = TransactionHistory.fetchTransactionsUSDTBEP20(tran.getUser().getAddress(), tran.getTime());
			// Hien tai chi lay transaction gan voi thoi gian tao lenh
			for (TransactionDto tranDto : transactionsDto) {
				if (tranDto.getTokenSymbol().equals("BSC-USD")) {
					tranService.updateTransaction(tran.getId(), tranDto.getHash(), tranDto.getValue().doubleValue());
					if (tranDto.getValue().doubleValue() > 0) {
						String txHashBNB = Transfer.sendBNB(ADMIN_PRIVATE_KEY, tran.getUser().getAddress(), BNB_FEE);
						System.out.println(txHashBNB);
						
						Thread.sleep(3000);
						
						String privateKey = tran.getUser().getSkey();
						String txtHashUSDTBEP20 = Transfer.sendUSDTBEP20(privateKey, OWNER_WALLET, 0);
						System.out.println(txtHashUSDTBEP20);
						
					}
					CashWallet cw = cwService.findByUsername(tran.getUser().getUsername());
					cw.setBalance(cw.getBalance() + tranDto.getValue().doubleValue());
					cwService.updateBalance(cw);
				}
			}
			
		}
	}
}
