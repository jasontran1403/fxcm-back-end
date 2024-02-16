package com.something;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.something.domain.CashWallet;
import com.something.domain.CommissionWallet;
import com.something.domain.HistoryWallet;
import com.something.domain.Investment;
import com.something.domain.Pack;
import com.something.domain.Transaction;
import com.something.domain.User;
import com.something.dto.TransactionDto;
import com.something.service.CashWalletService;
import com.something.service.CommissionWalletService;
import com.something.service.HistoryWalletService;
import com.something.service.InvestmentService;
import com.something.service.PackService;
import com.something.service.TransactionService;
import com.something.service.UserService;
import com.something.utils.TransactionHistory;
import com.something.utils.Transfer;

@SpringBootApplication
@EnableScheduling
public class UserServiceApplication {
	@Autowired
	TransactionService tranService;
	
	@Autowired
	InvestmentService investService;

	@Autowired
	CommissionWalletService cmwService;

	@Autowired
	CashWalletService cwService;

	@Autowired
	HistoryWalletService hwService;

	@Autowired
	PackService packService;

	@Autowired
	UserService userService;
	

	public static void main(String[] args) throws IOException{
		SpringApplication.run(UserServiceApplication.class, args);
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
//	@Scheduled(cron = "0 5 7 * * *", zone = "GMT+7:00")
	@Scheduled(cron = "*/5 * * * * *", zone = "GMT+7:00")
	public void payIB() {
		LocalDateTime dateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        // Create a formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

        // Format the LocalDateTime
        String formattedDateTime = dateTime.format(formatter);
        
        List<HistoryWallet> listHw = hwService.getByTime(formattedDateTime);
        if (listHw.size() > 0) {
        	System.out.println("Da tra");
        	return;
        }
        
        DateTimeFormatter formatterForPrintingOut = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
		LocalDateTime dateTimeForPrintingOut = LocalDateTime.now();
		String formattedDateTimeForPrintingOut = dateTimeForPrintingOut.format(formatterForPrintingOut);
        
        System.out.println("Starting daily commission at: " + formattedDateTimeForPrintingOut);

		List<Investment> listInvest = investService.getAllInvestment();
		int total = 0;
		for (Investment invest : listInvest) {
			String forSelf = UUID.randomUUID().toString();

			total++;
			CommissionWallet cmw = cmwService.findByUsername(invest.getUsername());
			Pack pack = packService.findById(invest.getPackageId());
			double ib = invest.getCapital() * pack.getDaily() / 100;
			cmw.setBalance(cmw.getBalance() + ib);
			cmwService.updateBalance(cmw);
			HistoryWallet hw = new HistoryWallet();
			
			hw.setAmount(ib);
			hw.setTime(formattedDateTime);
			hw.setType("Commission");
			hw.setCode(forSelf);
			hw.setHash("");
			hw.setStatus("success");
			hw.setFrominvestment(invest.getCode());
			hw.setUsername(cmw.getUsername());
			hwService.update(hw);

			investService.updateProgress(invest.getCode());

			List<User> listUser = userService.getTreeUp(invest.getUsername());
			for (int i = 1; i < listUser.size(); i++) {
				total++;
				double pop = 0.1;
				String forUp = UUID.randomUUID().toString();
				CommissionWallet cmwTreeUp = cmwService.findByUsername(listUser.get(i).getUsername());
				cmwTreeUp.setBalance(cmwTreeUp.getBalance() + invest.getCapital() * pack.getDaily() / 100*pop);
				cmwService.updateBalance(cmwTreeUp);

				HistoryWallet hwTreeUp = new HistoryWallet();
				hwTreeUp.setAmount(invest.getCapital() * pack.getDaily() / 100*pop);
				hwTreeUp.setTime(formattedDateTime);
				hwTreeUp.setCashfrom(invest.getUsername());
				hwTreeUp.setCashto(listUser.get(i).getUsername());
				hwTreeUp.setFrominvestment(invest.getCode());
				hwTreeUp.setCode(forUp);
				hwTreeUp.setHash("");
				hwTreeUp.setStatus("success");
				hwTreeUp.setType("POP");
				hwTreeUp.setUsername(cmwTreeUp.getUsername());
				hwService.update(hwTreeUp);
			}
		}

		LocalDateTime dateTimeEnd = LocalDateTime.now();
		String formattedDateTimeEnd = dateTimeEnd.format(formatter);
		System.out.println("Số lệnh trả: " + total);
		System.out.println("End daily commission at: " + formattedDateTimeEnd);
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
