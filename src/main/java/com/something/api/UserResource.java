package com.something.api;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.something.domain.Activation;
import com.something.domain.Affiliate;
import com.something.domain.CashWallet;
import com.something.domain.CommissionWallet;
import com.something.domain.Email;
import com.something.domain.HistoryWallet;
import com.something.domain.Investment;
import com.something.domain.Pack;
import com.something.domain.Recovery;
import com.something.domain.Role;
import com.something.domain.Transaction;
import com.something.domain.User;
import com.something.dto.AuthenRequest;
import com.something.dto.InfoDto;
import com.something.dto.KycRequest;
import com.something.dto.StatDto;
import com.something.dto.StatisticDto;
import com.something.dto.TransactionDto;
import com.something.dto.TransactionHistoryDto;
import com.something.dto.UserDTO;
import com.something.dto.UserTreeviewDto;
import com.something.service.ActivationService;
import com.something.service.AffiliateService;
import com.something.service.AuthenticatorService;
import com.something.service.CashWalletService;
import com.something.service.CommissionWalletService;
import com.something.service.HistoryWalletService;
import com.something.service.InvestmentService;
import com.something.service.MaillerService;
import com.something.service.PackService;
import com.something.service.RecoveryService;
import com.something.service.TransactionService;
import com.something.service.UserService;
import com.something.totp.TotpAutoConfiguration;
import com.something.utils.FileUpload;
import com.something.utils.TransactionHistory;
import com.something.utils.Transfer;
import com.something.utils.WalletUtils;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserResource {
	@Autowired
	FileUpload fileUpload;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	TransactionService tranService;

	@Autowired
	RecoveryService recoService;

	@Autowired
	ActivationService actiService;

	@Autowired
	PackService packService;

	@Autowired
	CommissionWalletService cmwService;

	@Autowired
	UserService userService;

	@Autowired
	MaillerService mailerServie;

	@Autowired
	AffiliateService affService;

	@Autowired
	CashWalletService cwService;

	@Autowired
	HistoryWalletService hwService;

	@Autowired
	InvestmentService investService;

	@Autowired
	AuthenticatorService authService;

	@Autowired
	TotpAutoConfiguration verifyCode;

	@Autowired
	private SecretGenerator secretGenerator;

	@Autowired
	private QrDataFactory qrDataFactory;

	@Autowired
	private QrGenerator qrGenerator;

	@GetMapping("/user/getTree/{username}")
	public List<UserTreeviewDto> getTree(@PathVariable String username) {
		User rootUser = userService.getUser(username);

		if (rootUser != null) {
			return convertToDto(username); // Giới hạn độ sâu ở 3 tầng
		}

		return new ArrayList<>();
	}

	private List<UserTreeviewDto> convertToDto(String rootUsername) {
	    List<UserTreeviewDto> result = new ArrayList<>();

	    // Tầng 1
	    List<User> subUsersF1 = userService.findByRootUsername(rootUsername);
	    for (User userF1 : subUsersF1) {
	        if (!userF1.getUsername().equals(rootUsername)) {

	            UserTreeviewDto itemF1 = new UserTreeviewDto();
	            itemF1.setUsername(userF1.getUsername());
	            itemF1.setTotalSales(userF1.getSales());
	            List<UserTreeviewDto> listF2 = new ArrayList<>();

	            // Tầng 2
	            List<User> subUsersF2 = userService.findByRootUsername(userF1.getUsername());
	            for (User userF2 : subUsersF2) {
	                UserTreeviewDto itemF2 = new UserTreeviewDto();
	                itemF2.setUsername(userF2.getUsername());
	                itemF2.setTotalSales(investService.calculateSales(userF2.getUsername()));
	                List<UserTreeviewDto> listF3 = new ArrayList<>();

	                // Tầng 3
	                List<User> subUsersF3 = userService.findByRootUsername(userF2.getUsername());
	                for (User userF3 : subUsersF3) {
	                    UserTreeviewDto itemF3 = new UserTreeviewDto();
	                    itemF3.setUsername(userF3.getUsername());
	                    itemF3.setTotalSales(investService.calculateSales(userF3.getUsername()));
	                    itemF3.setSubUsers(new ArrayList<>());
	                    listF3.add(itemF3);
	                }

	                itemF2.setSubUsers(listF3);
	                listF2.add(itemF2);
	            }

	            itemF1.setSubUsers(listF2);
	            result.add(itemF1);
	        }
	    }

	    return result;
	}


	@GetMapping("/wallet/ib")
	public ResponseEntity<String> payIB() {
		LocalDateTime dateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        // Create a formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

        // Format the LocalDateTime
        String formattedDateTime = dateTime.format(formatter);
        
        List<HistoryWallet> listHw = hwService.getByTime(formattedDateTime);
        if (listHw.size() > 0) {
        	return ResponseEntity.ok("Da tra");
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

		
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/wallet/generateWallet")
	public ResponseEntity<String> generateWallet() throws Exception {
		WalletUtils.generateBSCWallet();
		return ResponseEntity.ok().body("OK la");
	}

	@GetMapping("/wallet/sendBNB/{toAddress}")
	public ResponseEntity<String> sendBNB(@PathVariable String toAddress) throws Exception {
		String txHash = Transfer.sendBNB("44cb641f0f3e293a46879bdbf1f3f3b0d61a834dc7423dce3f9a669290d16ec0", toAddress,
				0.0002);
		return ResponseEntity.ok().body(txHash);
	}

	@GetMapping("/wallet/sendUSDTBEP20")
	public ResponseEntity<String> sendUSDTBEP20() throws Exception {
		String txHash = Transfer.sendUSDTBEP20("00a9cc5e4bfea932e908d1d95970b1b52f191c0d7afcffdca21eb95edd2114ccd7",
				"0x5aAE95a2c8280820d8DB3e333B36Ba03cf469C8B", 0);
		return ResponseEntity.ok().body(txHash);
	}

	@GetMapping("/wallet/fetchTransactionHistoryUSDTBEP20/wallet={walletAddress}&time={time}")
	public ResponseEntity<List<TransactionDto>> fetchTransactionHistoryUSDTBEP20(
			@PathVariable("walletAddress") String walletAddress, @PathVariable("time") long time) throws Exception {
		List<TransactionDto> transactions = TransactionHistory.fetchTransactionsUSDTBEP20(walletAddress, time);
		return ResponseEntity.ok().body(transactions);
	}

	@GetMapping("/wallet/fetchTransactionHistoryBNB/wallet={walletAddress}&time={time}")
	public ResponseEntity<List<TransactionDto>> fetchTransactionHistory(
			@PathVariable("walletAddress") String walletAddress, @PathVariable("time") long time) throws Exception {
		List<TransactionDto> transactions = TransactionHistory.fetchTransactionsBNB(walletAddress, time);
		return ResponseEntity.ok().body(transactions);
	}

	@GetMapping("/generate-qr/{username}/{amount}")
	public ResponseEntity<byte[]> generateQRCode(@PathVariable String username, @PathVariable double amount)
			throws WriterException {
		User user = userService.getUser(username);

		String wallet = user.getAddress();

		List<Transaction> trans = tranService.findTransactionByUserId(user.getId());
		for (Transaction tran : trans) {
			if (!tran.isStatus()) {
				return null;
			}
		}

		tranService.saveTransaction(user, amount);

		byte[] qrCode = generateQRCodeImage(wallet);

		return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qrcode.png").body(qrCode);
	}

	@GetMapping("/user/getInfo/{username}")
	public ResponseEntity<InfoDto> getInfo(@PathVariable("username") String username) {
		InfoDto info = new InfoDto();
		User user = userService.getUser(username);
		info.setEmail(user.getEmail());
		info.setFullname(user.getName());
		info.setContact(user.getContact());
		info.setIdentity(user.getIdentity());
		info.setPhone(user.getPhoneNumber());
		info.setUsername(username);

		return ResponseEntity.ok(info);
	}

	@GetMapping("/user/stat/{username}")
	public ResponseEntity<List<StatDto>> get7days(@PathVariable("username") String username) {
		List<StatDto> result = hwService.get7Days(username);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/user/update")
	public ResponseEntity<String> updateUserInfo(@RequestBody InfoDto info) {
		User userByUsername = userService.getUser(info.getUsername());

		if (userByUsername == null) {
			return ResponseEntity.ok("This username is not existed!");
		}

		if (!userByUsername.getEmail().equals(info.getEmail())) {
			// If the email has changed, check if the new email already exists
			User userByEmail = userService.findByEmail(info.getEmail());

			if (userByEmail != null) {
				return ResponseEntity.ok("This email existed!");
			}
		}

		// If the username exists and the email is either the same or not already taken,
		// update the info
		userService.updateInfo(info);
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/withdraw/{username}/{amount}/{address}")
	public ResponseEntity<String> withdraw(@RequestParam String username, @RequestParam double amount,
			@RequestParam String address) {
		User user = userService.getUser(username);
		tranService.saveWithdraw(user, amount, address);
		return ResponseEntity.ok("success");
	}

	@GetMapping("/cancel-deposit/{transactionId}")
	public ResponseEntity<String> generateQRCode(@PathVariable long transactionId) {
		tranService.cancelTransaction(transactionId);

		return ResponseEntity.ok("OK");
	}

	@PostMapping("/user/kyc")
	public ResponseEntity<String> kycupload(@RequestParam MultipartFile file1, @RequestParam MultipartFile file2,
			@RequestParam MultipartFile file3, @RequestParam MultipartFile file4, @RequestParam String username)
			throws InterruptedException {
		User user = userService.getUser(username);

		if (user.getKycStatus() == 1) {
			return ResponseEntity.ok("da kyc");
		}
		String fileName1 = "fxcm/kyc_id_f_" + username;
		String fileName2 = "fxcm/kyc_id_b_" + username;
		String fileName3 = "fxcm/kyc_cf_f_" + username;
		String fileName4 = "fxcm/kyc_cf_b_" + username;
		List<String> urls = new ArrayList<>();
		String url1 = fileUpload.uploadImage(file1, fileName1);
		Thread.sleep(500);
		urls.add(url1);
		String url2 = fileUpload.uploadImage(file2, fileName2);
		Thread.sleep(500);
		urls.add(url2);
		String url3 = fileUpload.uploadImage(file3, fileName3);
		Thread.sleep(500);
		urls.add(url3);
		String url4 = fileUpload.uploadImage(file4, fileName4);
		Thread.sleep(500);
		urls.add(url4);

		userService.saveKyc(username, urls);
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/user/getKycImage/{username}")
	public ResponseEntity<List<String>> kycImage(@PathVariable String username) {
		User user = userService.getUser(username);
		List<String> result = new ArrayList<>();
		result.add(user.getIdFront());
		result.add(user.getIdBack());
		result.add(user.getConfirmFront());
		result.add(user.getConfirmBack());

		return ResponseEntity.ok(result);
	}

	@GetMapping("/authentication/showQR/{username}")
	public List<String> generate2FA(@PathVariable("username") String username)
			throws QrGenerationException, WriterException, IOException, CodeGenerationException {
		User user = userService.getUser(username);
		QrData data = qrDataFactory.newBuilder().label(user.getUsername()).secret(user.getSecret())
				.issuer("Something Application").period(30).build();

		String qrCodeImage = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
		List<String> info2FA = new ArrayList<>();
		String isEnabled = "";
		if (user.isMfaEnabled()) {
			isEnabled = "true";
		} else {
			isEnabled = "false";
		}
		info2FA.add(isEnabled);
		info2FA.add(user.getSecret());
		info2FA.add(qrCodeImage);

		return info2FA;
	}

	@PostMapping("/authentication/enabled")
	public String enabled(@RequestParam("username") String username, @RequestParam("code") String code) {
		User user = userService.getUser(username);
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.getSecret(), code)) {
			userService.enabledAuthen(user);
			return "success";
		} else {
			return "failed";
		}
	}

	@PostMapping("/user/changePassword")
	public String changePassword(@RequestParam("username") String username,
			@RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword,
			@RequestParam("confirmNewPassword") String confirmNewPassword, @RequestParam("authen") String authen) {
		User user = userService.getUser(username);
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		if (encoder.matches(currentPassword, user.getPassword())) {
			if (user.isMfaEnabled()) {
				if (verify.isValidCode(user.getSecret(), authen)) {
					user.setPassword(encoder.encode(newPassword));
					userService.changePassword(user);
					return "Change password success";
				} else {
					return "2FA code is incorrect";
				}
			} else {
				user.setPassword(encoder.encode(newPassword));
				userService.changePassword(user);
				return "Change password success";
			}
		} else {
			return "Old password is incorrect";
		}
	}

	@PostMapping("/authentication/disabled")
	public String disabled(@RequestParam("username") String username, @RequestParam("code") String code) {
		User user = userService.getUser(username);
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.getSecret(), code)) {
			userService.disabledAuthen(user);
			// xóa secret 2fa
			String secret = secretGenerator.generate();

			user.setSecret(secret);
			userService.updateSecrect(user);
			return "success";
		} else {
			return "failed";
		}

	}

	@GetMapping("/getAllData/users")
	public ResponseEntity<List<User>> getUsers() {
		return ResponseEntity.ok().body(userService.getUsers());
	}

	@GetMapping("/getTransaction/{username}/{type}")
	public ResponseEntity<List<TransactionHistoryDto>> getUserTransaction(@PathVariable("username") String username,
			@PathVariable("type") String type) {
		User user = userService.getUser(username);
		System.out.println(user.getId() + " -- " + type);
		List<Transaction> listTransactions = tranService.findTransactionByUserIdAndType(user.getId(), type);
		List<TransactionHistoryDto> response = new ArrayList<>();

		for (Transaction transaction : listTransactions) {
			TransactionHistoryDto item = new TransactionHistoryDto();
			item.setId(transaction.getId());
			item.setHash(transaction.getHash());
			item.setAmount(transaction.getAmount());
			item.setStatus(transaction.isStatus());
			item.setTime(transaction.getTime());
			item.setType(transaction.getType());
			response.add(item);
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/user/{username}")
	public ResponseEntity<UserDTO> getUserInfo(@PathVariable("username") String username) {
		UserDTO userDTO = new UserDTO();
		User user = userService.getUser(username);
		userDTO.setUser(user);
		CashWallet cw = cwService.findByUsername(username);
		CommissionWallet cmw = cmwService.findByUsername(username);

		userDTO.setCashbalance(cw.getBalance());
		userDTO.setCommissionbalance(cmw.getBalance());

		return ResponseEntity.ok().body(userDTO);
	}

	@PostMapping("/user/validation")
	public ResponseEntity<String> getUserIsActivated(@RequestBody AuthenRequest request) {
		User user = userService.getUser(request.getUsername());
		try {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			if (user.isActived() == false) {
				if (encoder.matches(request.getPassword(), user.getPassword())) {
					return ResponseEntity.ok().body("Not Actived");
				} else {
					return ResponseEntity.ok().body("Password is not correct");
				}
			} else {
				if (encoder.matches(request.getPassword(), user.getPassword())) {
					if (user.isMfaEnabled()) {
						TimeProvider timeProvider = new SystemTimeProvider();
						CodeGenerator codeGenerator = new DefaultCodeGenerator();
						DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
						verify.setAllowedTimePeriodDiscrepancy(0);
						if (verify.isValidCode(user.getSecret(), request.getFaCode())) {
							String access_token = generateJWT(request.getUsername(), request.getPassword());
							return ResponseEntity.ok().body(access_token);
						} else {
							return ResponseEntity.ok().body("Wrong 2FA");
						}
					} else {
						String access_token = generateJWT(request.getUsername(), request.getPassword());
						return ResponseEntity.ok().body(access_token);
					}

				} else {
					return ResponseEntity.ok().body("Password is not correct");
				}
			}
		} catch (Exception e) {
			return ResponseEntity.ok().body("Username is not exist");
		}
	}

	@PostMapping("/admin/validation")
	public ResponseEntity<String> adminValidation(@RequestBody AuthenRequest request) {
		User user = userService.getUser(request.getUsername());
		try {
			Collection<Role> roles = user.getRoles();
			for (Role role : roles) {
				if (role.getName().equals("ROLE_ADMIN")) {
					BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

					if (user.isActived() == false) {
						if (encoder.matches(request.getPassword(), user.getPassword())) {
							return ResponseEntity.ok().body("Not Actived");
						} else {
							return ResponseEntity.ok().body("Password is not correct");
						}
					} else {
						if (encoder.matches(request.getPassword(), user.getPassword())) {
							if (user.isMfaEnabled()) {
								TimeProvider timeProvider = new SystemTimeProvider();
								CodeGenerator codeGenerator = new DefaultCodeGenerator();
								DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
								verify.setAllowedTimePeriodDiscrepancy(0);
								if (verify.isValidCode(user.getSecret(), request.getFaCode())) {
									String access_token = generateJWT(request.getUsername(), request.getPassword());
									return ResponseEntity.ok().body(access_token);
								} else {
									return ResponseEntity.ok().body("Wrong 2FA");
								}
							} else {
								String access_token = generateJWT(request.getUsername(), request.getPassword());
								return ResponseEntity.ok().body(access_token);
							}

						} else {
							return ResponseEntity.ok().body("Password is not correct");
						}
					}
				} else {
					return ResponseEntity.ok().body("This account wasn't ROLE_ADMIN");
				}
			}
		} catch (Exception e) {
			return ResponseEntity.ok().body("Username is not exist");
		}
		return ResponseEntity.ok().body("Error");
	}

	@PostMapping("/affiliate/generate")
	public ResponseEntity<Affiliate> addAffiliate(@RequestParam("root") String root,
			@RequestParam("placement") String placement, @RequestParam("side") String side) {
		return ResponseEntity.ok().body(affService.addRegisURL(root, placement, side));
	}

	@GetMapping("/packages")
	public ResponseEntity<List<Pack>> getAllPackages() {
		return ResponseEntity.ok().body(packService.getAllPackges());
	}

	@GetMapping("/investment/withdrawCapital/{investmentcode}")
	public ResponseEntity<Investment> withdrawCapital(@PathVariable("investmentcode") String investmentcode) {
		Investment invest = investService.findInvestmentByCode(investmentcode);
		User user = userService.getUser(invest.getUsername());
		double newSale = user.getSales() - invest.getClaimable();
		userService.updateSaleFromWithdraw(user.getUsername(), (long) newSale);

		List<User> listUser = userService.getTreeUpToRoot(user.getUsername());

		for (User item : listUser) {
			if (item.getLevel() != 0 || item.getUsername().equalsIgnoreCase(user.getUsername())) {
				long newTeamSale = (long) (invest.getClaimable());
				userService.updateteamSaleWithdraw(item.getUsername(), newTeamSale);
			} else {
				continue;
			}
		}
		userService.calRank();

		return ResponseEntity.ok().body(investService.withdrawCapital(investmentcode));
	}

	@GetMapping("/cashWallet/balance/{username}")
	public ResponseEntity<CashWallet> getCashWalletBalance(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(cwService.findByUsername(username));
	}

	@GetMapping("/commissionWallet/balance/{username}")
	public ResponseEntity<CommissionWallet> getCommissionWalletBalance(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(cmwService.findByUsername(username));
	}

	@GetMapping("/history/commission/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllCommissionHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findCommissionHistoryByUsername(username));
	}

	@GetMapping("/history/commissionForDashboard/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllCommissionHistoriesForDashboard(
			@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findCommissionHistoryForDashboardByUsername(username));
	}

	@GetMapping("/history/swap/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllSwapHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findSwapHistoryByUsername(username));
	}

	@GetMapping("/history/withdraw/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllWithdrawHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findWithdrawHistoryByUsername(username));
	}

	@GetMapping("/history/deposit/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllDepositHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findDepositHistoryByUsername(username));
	}

	@GetMapping("/history/transfer/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllTransferHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findTransferHistoryByUsername(username));
	}

	@GetMapping("/history/runningInvestment/{username}")
	public ResponseEntity<List<Investment>> getAllInvestmentRunningHistories(
			@PathVariable("username") String username) {
		return ResponseEntity.ok().body(investService.getAllActiveByUsername(username));
	}

	@GetMapping("/history/investment/{username}")
	public ResponseEntity<List<Investment>> getAllInvestmentHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(investService.getAllByUsername(username));
	}

	@PostMapping("/wallet/deposit")
	public ResponseEntity<String> deposit(@RequestParam("username") String username,
			@RequestParam("amount") double amount) {
		System.out.println(username);
		System.out.println(amount);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping("/wallet/withdraw")
	public ResponseEntity<String> withdraw(@RequestParam("walletaddress") String walletaddress,
			@RequestParam("amount") double amount) {
		User user = new User();
		if (user.isLocked()) {
			return ResponseEntity.ok().body("Your account is locked trade method, please contact to customer service");
		}
		System.out.println(walletaddress);
		System.out.println(amount);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping("/package/buy")
	public ResponseEntity<String> buyPackage(@RequestParam("packid") int packid,
			@RequestParam("username") String username, @RequestParam("timeEnd") String timeEnd) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDateTime = dateTime.format(formatter);
		String randomString = UUID.randomUUID().toString();
		String uuid = randomString.substring(24, randomString.length());
		String forDirect = UUID.randomUUID().toString();
		String forSelf = UUID.randomUUID().toString();
		Pack pack = packService.findById(packid);
		User user = userService.getUser(username);
		CashWallet cw = cwService.findByUsername(username);
		HistoryWallet hw = new HistoryWallet();
		Investment invest = new Investment();
		invest.setPackageId(packid);
		invest.setUsername(username);
		invest.setTime(formattedDateTime);
		invest.setCapital(pack.getPrice());
		invest.setTimeEnd(timeEnd);
		invest.setCode(uuid);
		invest.setCapital(pack.getPrice());
		invest.setCount(0);
		invest.setClaimable(0);
		invest.setRemain(pack.getPrice());
		boolean check = false;
		if (pack.getPrice() <= cw.getBalance()) {
			check = true;
		}

		if (check) {
			investService.save(invest);
			userService.updateSale(user.getUsername(), pack.getPrice());
			userService.updateMaxOut(user, pack.getPrice(), "buy");

			User sponsor = userService.getUser(user.getRootUsername());
			CommissionWallet cmwSponsor = cmwService.findByUsername(sponsor.getUsername());
			HistoryWallet hwSponsor = new HistoryWallet();

			double commissionrate = 5;
			int rank = sponsor.getLevel();

//			switch (rank) {
//			case 1:
//				commissionrate = 5;
//				break;
//			case 2:
//				commissionrate = 5.5;
//				break;
//			case 3:
//				commissionrate = 6;
//				break;
//			case 4:
//				commissionrate = 7;
//				break;
//			case 5:
//				commissionrate = 8;
//				break;
//			case 6:
//				commissionrate = 9;
//				break;
//			case 7:
//				commissionrate = 10;
//				break;
//			case 8:
//				commissionrate = 12;
//				break;
//			case 9:
//				commissionrate = 15;
//				break;
//			}

			if (sponsor.getLevel() > -999) {
				double directCommission = cmwSponsor.getBalance() + (pack.getPrice() * commissionrate / 100);

				cmwSponsor.setBalance(directCommission);

				hwSponsor.setAmount(pack.getPrice() * commissionrate / 100);
				hwSponsor.setCashfrom(user.getUsername());
				hwSponsor.setCashto(sponsor.getUsername());
				hwSponsor.setFrominvestment(uuid);
				hwSponsor.setTime(formattedDateTime);
				hwSponsor.setType("Direct Commission");
				hwSponsor.setCode(forDirect);
				hwSponsor.setHash("");
				hwSponsor.setStatus("success");
				hwSponsor.setUsername(cmwSponsor.getUsername());
				hwService.update(hwSponsor);
				cmwService.updateBalance(cmwSponsor);

			}

			cw.setBalance(cw.getBalance() - pack.getPrice());
			hw.setAmount(pack.getPrice());
			hw.setTime(formattedDateTime);
			hw.setCode(forSelf);
			hw.setHash("");
			hw.setStatus("success");
			hw.setType("Buy Package");
			hw.setUsername(cw.getUsername());
			hwService.update(hw);
			cwService.updateBalance(cw);

//			List<User> listUser = userService.getTreeUpToRoot(username);
//
//			for (User item : listUser) {
//				if (item.getLevel() != 0 || item.getUsername().equalsIgnoreCase(user.getUsername())) {
//					userService.updateteamSale(item.getUsername(), pack.getPrice());
//				} else {
//					continue;
//				}
//			}

//			userService.calRank();
			return ResponseEntity.ok().body("OK");
		} else {
			return ResponseEntity.ok().body("Failed, balance is not enough to buy this package");
		}
	}

	@GetMapping("/affiliate/getByRoot/investment")
	public ResponseEntity<List<Investment>> getAll() {
		return ResponseEntity.ok().body(investService.getAllInvestment());
	}

	// Lấy reflink từ {username}
	@GetMapping("/affiliate/getByRoot/{root}")
	public ResponseEntity<List<Affiliate>> getAffiliateByRoot(@PathVariable("root") String root) {
		return ResponseEntity.ok().body(affService.getByRoot(root));
	}

	// Lấy reflink từ {username}
	@GetMapping("/affiliate/getByPlacement/{placement}")
	public ResponseEntity<List<Affiliate>> getAffiliateByPlacement(@PathVariable("placement") String placement) {
		return ResponseEntity.ok().body(affService.getByPlacement(placement));
	}

	// Lấy reflink bên trái/phải
	@GetMapping("/affiliate/getByPlacement/{placement}/{side}")
	public ResponseEntity<Affiliate> getAffiliateByPlacementAndSide(@PathVariable("placement") String placement,
			@PathVariable("side") String side) {
		return ResponseEntity.ok().body(affService.getByPlacementAndSide(placement, side));
	}

	// Lấy thông tin ref đăng ký
	@GetMapping("/affiliate/{uuid}")
	public ResponseEntity<Affiliate> getAffiliate(@PathVariable String uuid) {
		return ResponseEntity.ok().body(affService.getByUUID(uuid));
	}

	// lấy 15 tầng phía trên
	@GetMapping("/userTreeUp/{username}")
	public ResponseEntity<List<User>> getUserUp(@PathVariable String username) {
		return ResponseEntity.ok().body(userService.getTreeUp(username));
	}

	// lấy lên đến root
	@GetMapping("/userTreeUpToRoot/{username}")
	public ResponseEntity<List<User>> getUserUpToRoot(@PathVariable String username) {
		return ResponseEntity.ok().body(userService.getTreeUpToRoot(username));
	}

	// lấy 15 tầng xuống
	@GetMapping("/userMapDown/{username}")
	public ResponseEntity<HashMap<String, List<User>>> getMapDown(@PathVariable String username) {
		return ResponseEntity.ok().body(userService.getMapDown(username));
	}

	@PutMapping("/affiliate/{uuid}")
	public ResponseEntity<String> updateAffiliate(@PathVariable String uuid) {
		affService.updateRegistered(uuid);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping("/user/forgotpassword")
	public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
		User user = userService.findByEmail(email);
		String uuid = UUID.randomUUID().toString();
		if (user != null) {
			Recovery recoverPassword = new Recovery();
			recoverPassword.setUsername(user.getUsername());
			recoverPassword.setUuid(uuid);
			recoService.saveRecovery(recoverPassword);

			Thread thread = new Thread() {
				public void run() {
					String url = "The url is: https://dashboard.fxcmholdings.com/reset-password/" + uuid;
					String name = user.getName().equals("") ? "user" : user.getName();
					sendMail(email, emailBody(name, url, ""));
				}
			};
			thread.start();
			return ResponseEntity.ok().body("OK");
		} else {
			return ResponseEntity.ok().body("Email is not existed");
		}

	}

	@PostMapping("/user/resetPassword")
	public ResponseEntity<String> resetPassword(@RequestParam("uuid") String uuid,
			@RequestParam("newPassword") String newPassword) {
		Recovery recover = recoService.findByUUID(uuid);
		if (recover != null) {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			User user = userService.getUser(recover.getUsername());
			user.setPassword(encoder.encode(newPassword));
			userService.changePassword(user);
			recoService.remove(uuid);

			return ResponseEntity.ok().body("Reset password successfull");
		} else {
			return ResponseEntity.ok().body("Reset password link is invalid");
		}
	}

	@PostMapping("/admin/getPoint")
	public ResponseEntity<String> getPoint(@RequestParam("username") String username,
			@RequestParam("amount") double amount) {
		User user = userService.getUser(username);
		if (user != null) {
			if (amount < 0) {
				return ResponseEntity.ok().body("amount not valid");
			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
			LocalDateTime dateTime = LocalDateTime.now();
			String formattedDateTime = dateTime.format(formatter);
			String uuid = UUID.randomUUID().toString();

			CashWallet cw = cwService.findByUsername(username);
			cw.setBalance(cw.getBalance() + amount);
			cwService.updateBalance(cw);

			HistoryWallet history = new HistoryWallet();
			history.setAmount(amount);
			history.setCashfrom("System");
			history.setCashto(username);
			history.setFrominvestment("");
			history.setTime(formattedDateTime);
			history.setType("Transfer");
			history.setCode(uuid);
			history.setHash("");
			history.setStatus("success");
			history.setUsername(username);
			hwService.update(history);

			return ResponseEntity.ok().body("success");
		} else {
			return ResponseEntity.ok().body("not existed");
		}
	}

	@GetMapping("/admin/getTransfer")
	public ResponseEntity<List<HistoryWallet>> getTrasnfer() {
		List<HistoryWallet> result = hwService.getTransferHistory();

		return ResponseEntity.ok(result);
	}

	@PostMapping("/admin/addPack")
	public ResponseEntity<String> addPack(@RequestParam("packageName") String packageName,
			@RequestParam("daily") double daily, @RequestParam("direct") double direct,
			@RequestParam("price") long price) {
		List<Pack> packs = packService.findByName(packageName);
		if (packs.size() > 0) {
			return ResponseEntity.ok("trung ten");
		} else {
			if (price < 0 || daily < 0 || direct < 0) {
				return ResponseEntity.ok("number invalid");
			} else {
				packService.savePack(packageName, price, direct, daily);
				return ResponseEntity.ok("ok");
			}
		}
	}

	@PostMapping("/admin/resKyc")
	public ResponseEntity<String> kycres(@RequestParam("username") String username,
			@RequestParam("message") String message, @RequestParam("status") int status) {
		User user = userService.getUser(username);
		if (status == 1) {
			user.setKycStatus(1);
			user.setKycMessage("success");
			userService.saveUser(user);
			return ResponseEntity.ok("success");
		} else {
			user.setKycMessage(message);
			userService.saveUser(user);
			return ResponseEntity.ok(message);
		}
	}

	@PostMapping("/admin/togglePack")
	public ResponseEntity<String> addPack(@RequestParam("packageId") int packageId) {
		Pack pack = packService.findById(packageId);
		if (pack != null) {
			packService.toggleStatus(packageId);
			return ResponseEntity.ok("ok");
		} else {
			return ResponseEntity.ok("not existed");
		}
	}

	@PostMapping("/user/regis")
	public ResponseEntity<String> saveUser(@RequestBody User user) throws Exception {
		URI uri = URI
				.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/regis").toUriString());

		int checkPinNumber = (int) Math.floor(((Math.random() * 899999) + 100000));
		String uuid = UUID.randomUUID().toString();
		Activation acti = new Activation();
		acti.setUuid(uuid);
		acti.setUsername(user.getUsername());
		acti.setActivation(checkPinNumber);
		actiService.save(acti);

		Thread thread = new Thread() {
			public void run() {
				String url = "The url is: https://dashboard.fxcmholdings.com/active-account/" + acti.getUuid();
				String name = user.getName().equals("") ? "user" : user.getName();
				String code = "The code is: " + checkPinNumber;
				sendMail(user.getEmail(), emailBody(name, url, code));
			}
		};
		// thread.start();

		String secret = secretGenerator.generate();

		user.setSecret(secret);
		String result = userService.regis(user);

		return ResponseEntity.created(uri).body(result);
	}

	@GetMapping("/user/getRef/{username}")
	public ResponseEntity<String> getRefLinkFromUser(@PathVariable String username) {
		User user = userService.getUser(username);
		return ResponseEntity.ok(user.getRef());
	}

	@GetMapping("/user/reflink/{ref}")
	public ResponseEntity<String> reflink(@PathVariable String ref) {
		return ResponseEntity.ok(userService.refLink(ref));
	}

	@PostMapping("/user/active/{uuid}")
	public ResponseEntity<String> active(@PathVariable("uuid") String uuid,
			@RequestParam("activecode") String activecode) throws Exception {
		Activation acti = actiService.getActivation(uuid);
		if (acti.getActivation() == Integer.parseInt(activecode)) {
			User user = userService.getUser(acti.getUsername());
			userService.activated(user);

			actiService.activated(acti);

//			WalletResponse response = WalletUtils.generateBSCWallet();
//
//			userService.saveWallet(user.getId(), response);
			return ResponseEntity.ok().body("Activation success");
		} else {
			return ResponseEntity.ok().body("Wrong activation code");
		}
	}

	@GetMapping("/admin/getKycImage/{username}")
	public ResponseEntity<List<String>> getImage(@PathVariable("username") String username) {
		List<String> result = new ArrayList<>();
		User user = userService.getUser(username);
		if (user != null) {
			result.add(user.getIdFront());
			result.add(user.getIdBack());
			result.add(user.getConfirmFront());
			result.add(user.getConfirmBack());

			return ResponseEntity.ok(result);
		} else {
			return ResponseEntity.ok(null);
		}
	}

	@PostMapping("/admin/kyc")
	public ResponseEntity<String> kycprocess(@RequestBody KycRequest request) {
		if (request.getStatus() == 2) {
			if (request.getMessage().equals("")) {
				return ResponseEntity.ok("reason empty");
			} else {
				userService.processKyc(request.getUsername(), request.getMessage(), request.getStatus());
				return ResponseEntity.ok("declined");
			}
		} else if (request.getStatus() == 1) {
			userService.processKyc(request.getUsername(), "", request.getStatus());
			return ResponseEntity.ok("approved");
		} else {
			return ResponseEntity.ok("no action");
		}

	}

	@PostMapping("/user/active/resend")
	public ResponseEntity<String> resendactive(@RequestParam("username") String username) {
		Activation acti = actiService.getActivationByUsername(username);
		User user = userService.getUser(username);

		if (acti == null || user == null) {
			return ResponseEntity.ok().body("Cannot find your usename, please try again");
		} else {
			int checkPinNumber = (int) Math.floor(((Math.random() * 899999) + 100000));

			Thread thread = new Thread() {
				public void run() {
					String url = "The url is: https://dashboard.fxcmholdings.com/active-account/" + acti.getUuid();
					String name = user.getName().equals("") ? "user" : user.getName();

					String code = "The code is: " + checkPinNumber;
					sendMail(user.getEmail(), emailBody(name, url, code));
				}
			};

			thread.start();

			actiService.reGenerateActi(acti, checkPinNumber);

			return ResponseEntity.ok().body(acti.getUuid());
		}
	}

	@PutMapping("/user/updateRef")
	public ResponseEntity<User> saveUser(@RequestParam String username, @RequestParam String usernameRef,
			@RequestParam String side) {
		URI uri = URI
				.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/updateRef").toUriString());
		return ResponseEntity.created(uri).body(userService.updateRef(username, usernameRef, side));
	}

	@PostMapping("/role/save")
	public ResponseEntity<Role> saveRole(@RequestBody Role role) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
		return ResponseEntity.created(uri).body(userService.saveRole(role));
	}

	@GetMapping("/token/refresh")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String authorizationHeader = request.getHeader(AUTHORIZATION);
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			try {
				String refresh_token = authorizationHeader.substring("Bearer ".length());
				Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
				JWTVerifier verifier = JWT.require(algorithm).build();
				DecodedJWT decodedJWT = verifier.verify(refresh_token);
				String username = decodedJWT.getSubject();
				User user = userService.getUser(username);
				String access_token = JWT.create().withSubject(user.getUsername())
						.withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
						.withIssuer(request.getRequestURL().toString())
						.withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
						.sign(algorithm);
				Map<String, String> tokens = new HashMap<>();
				tokens.put("access_token", access_token);
				tokens.put("refresh_token", refresh_token);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), tokens);
			} catch (Exception exception) {
				response.setHeader("error", exception.getMessage());
				response.setStatus(FORBIDDEN.value());
				Map<String, String> error = new HashMap<>();
				error.put("error_message", exception.getMessage());
				response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), error);
			}
		} else {
			throw new RuntimeException("Refresh token is missing");
		}
	}

	@GetMapping("/admin/listAccount")
	public ResponseEntity<List<User>> getAllUserForAdmin() {
		List<User> users = userService.getUsers();
		return ResponseEntity.ok(users);
	}

	@GetMapping("/admin/listInvestment")
	public ResponseEntity<List<Investment>> getAllInvestmentForAdmin() {
		List<Investment> investments = investService.getAllInvestment();
		return ResponseEntity.ok(investments);
	}

	@GetMapping("/admin/listTransaction")
	public ResponseEntity<List<HistoryWallet>> getAllHistoryForAdmin() {
		List<HistoryWallet> histories = hwService.getAllHistory();
		return ResponseEntity.ok(histories);
	}

	@GetMapping("/admin/listDeposit")
	public ResponseEntity<List<Transaction>> getAllDepositForAdmin() {
		List<Transaction> transactions = tranService.getAllTransactionByType("Deposit");
		return ResponseEntity.ok(transactions);
	}

	@GetMapping("/admin/static")
	public ResponseEntity<StatisticDto> getStatForAdmin() throws Exception {
		StatisticDto result = new StatisticDto();
		List<Investment> newestInvestment = investService.find5Invest();
		double totalDeposit = tranService.totalAmountByType("Deposit");
		double totalWithdraw = tranService.totalAmountByType("Withdraw");
		double investment = investService.cal();
		result.setBnbWallet(
				WalletUtils.getBNBBalance("44cb641f0f3e293a46879bdbf1f3f3b0d61a834dc7423dce3f9a669290d16ec0"));
		result.setTotalInvestment(investment);
		result.setTotalDeposit(totalDeposit);
		result.setTotalWithdraw(totalWithdraw);
		result.setListInvestments(newestInvestment);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/admin/listWithdraw")
	public ResponseEntity<List<Transaction>> getAllWithdrawForAdmin() {
		List<Transaction> transactions = tranService.getAllTransactionByType("Withdraw");
		return ResponseEntity.ok(transactions);
	}

	@GetMapping("/admin/listKyc")
	public ResponseEntity<List<User>> getAllKycForAdmin() {
		List<User> user = userService.findUserKyc(1);
		return ResponseEntity.ok(user);
	}

	@GetMapping("/admin/listPackage")
	public ResponseEntity<List<Pack>> getAllPackageForAdmin() {
		List<Pack> packages = packService.getAllPackges();
		return ResponseEntity.ok(packages);
	}

	@GetMapping("/admin/fillChart")
	public ResponseEntity<Map<String, Double>> fillChart() {
		List<Investment> investments = investService.findAll();

		Map<String, Double> monthlyRevenue = calculateMonthlyRevenue(investments);

		// Sắp xếp theo tháng tăng dần
		Map<String, Double> sortedMonthlyRevenue = new TreeMap<>(monthlyRevenue);

		return ResponseEntity.ok(sortedMonthlyRevenue);
	}

	private Map<String, Double> calculateMonthlyRevenue(List<Investment> investmentList) {
		Map<String, Double> monthlyRevenue = new HashMap<>();

		for (Investment investment : investmentList) {
			String month = extractMonth(investment.getTime()); // Lấy tháng từ trường "time"
			double capital = investment.getCapital();

			// Tính tổng doanh thu cho tháng
			monthlyRevenue.merge(month, capital, Double::sum);
		}

		return monthlyRevenue;
	}

	private String extractMonth(String time) {
		// Giả sử định dạng của time là "HH:mm:ss dd/MM/yyyy"
		String[] parts = time.split(" ");
		String datePart = parts[1]; // "dd/MM/yyyy"
		String[] dateParts = datePart.split("/");
		return dateParts[1]; // Lấy tháng
	}

	public void sendMail(String emailTo, String body) {
		Email m = new Email();
		m.setFrom("Holdings");
		m.setSubject("No reply from Holdings System");
		m.setTo(emailTo);
		m.setBody(body);
		try {
			mailerServie.send(m);
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
		}
	}

	private byte[] generateQRCodeImage(String text) throws WriterException {
		BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 300, 300);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			MatrixToImageWriter.writeToStream(matrix, "PNG", byteArrayOutputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return byteArrayOutputStream.toByteArray();
	}

	private String generateJWT(String username, String password) {
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
				password);
		Authentication authentication = authenticationManager.authenticate(authenticationToken);
		long DAY_IN_MILLIS = (long) 30 * 24 * 60 * 60 * 1000;
		Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
		return JWT.create().withSubject(username).withExpiresAt(new Date(System.currentTimeMillis() + DAY_IN_MILLIS))
				.withIssuer("FXCM Holdings").withClaim("roles", authentication.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.sign(algorithm);
	}

	private String emailBody(String receiptName, String url, String code) {
		return "<body class=\"clean-body u_body\" style=\"margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #e7e7e7;color: #000000\">\n"
				+ "  <!--[if IE]><div class=\"ie-container\"><![endif]-->\n"
				+ "  <!--[if mso]><div class=\"mso-container\"><![endif]-->\n"
				+ "  <table id=\"u_body\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #e7e7e7;width:100%\" cellpadding=\"0\" cellspacing=\"0\">\n"
				+ "  <tbody>\n" + "  <tr style=\"vertical-align: top\">\n"
				+ "    <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "    <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td align=\"center\" style=\"background-color: #e7e7e7;\"><![endif]-->\n"
				+ "    \n" + "  \n" + "  \n" + "    <!--[if gte mso 9]>\n"
				+ "      <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 0 auto;min-width: 320px;max-width: 600px;\">\n"
				+ "        <tr>\n"
				+ "          <td background=\"https://cdn.templates.unlayer.com/assets/1697613131983-Layer%20bg.png\" valign=\"top\" width=\"100%\">\n"
				+ "      <v:rect xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"true\" stroke=\"false\" style=\"width: 600px;\">\n"
				+ "        <v:fill type=\"frame\" src=\"https://cdn.templates.unlayer.com/assets/1697613131983-Layer%20bg.png\" /><v:textbox style=\"mso-fit-shape-to-text:true\" inset=\"0,0,0,0\">\n"
				+ "      <![endif]-->\n" + "  \n"
				+ "<div class=\"u-row-container\" style=\"padding: 0px;background-image: url('https://firebasestorage.googleapis.com/v0/b/hedging-1d816.appspot.com/o/fxcm%2Fimage-8.png?alt=media&token=b7cca729-7427-4c46-ab64-2a80c02c6303');background-repeat: no-repeat;background-position: center top;background-color: #fdeed2\">\n"
				+ "  <div class=\"u-row\" style=\"margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">\n"
				+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">\n"
				+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-image: url('images/image-8.png');background-repeat: no-repeat;background-position: center top;background-color: #fdeed2;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: transparent;\"><![endif]-->\n"
				+ "      \n"
				+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->\n"
				+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
				+ "  <div style=\"height: 100%;width: 100% !important;\">\n"
				+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->\n"
				+ "  \n"
				+ "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:40px 10px 10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n" + "  <!--[if mso]><table width=\"100%\"><tr><td><![endif]-->\n"
				+ "    <h1 class=\"v-font-size\" style=\"margin: 0px; line-height: 140%; text-align: center; word-wrap: break-word; font-family: 'Montserrat',sans-serif; font-size: 25px; font-weight: 700;\">Introduction</h1>\n"
				+ "  <!--[if mso]></td></tr></table><![endif]-->\n" + "\n" + "      </td>\n" + "    </tr>\n"
				+ "  </tbody>\n" + "</table>\n" + "\n"
				+ "<table id=\"u_content_heading_2\" style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:0px 10px 10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n" + "  <!--[if mso]><table width=\"100%\"><tr><td><![endif]-->\n"
				+ "    <h1 class=\"v-font-size\" style=\"margin: 0px; line-height: 110%; text-align: center; word-wrap: break-word; font-family: 'Montserrat',sans-serif; font-size: 50px; font-weight: 700;\">New Team<br />Member</h1>\n"
				+ "  <!--[if mso]></td></tr></table><![endif]-->\n" + "\n" + "      </td>\n" + "    </tr>\n"
				+ "  </tbody>\n" + "</table>\n" + "\n"
				+ "<table id=\"u_content_image_1\" style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n" + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
				+ "  <tr>\n" + "    <td style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\">\n"
				+ "      \n"
				+ "      <img align=\"center\" border=\"0\" src=\"https://firebasestorage.googleapis.com/v0/b/hedging-1d816.appspot.com/o/fxcm%2Fimage-2.png?alt=media&token=b74b6913-18d5-4e14-b9a6-164763ff4679\" alt=\"image\" title=\"image\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 68%;max-width: 394.4px;\" width=\"394.4\" class=\"v-src-width v-src-max-width\"/>\n"
				+ "      \n" + "    </td>\n" + "  </tr>\n" + "</table>\n" + "\n" + "      </td>\n" + "    </tr>\n"
				+ "  </tbody>\n" + "</table>\n" + "\n" + "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n"
				+ "  </div>\n" + "</div>\n" + "<!--[if (mso)|(IE)]></td><![endif]-->\n"
				+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n" + "    </div>\n"
				+ "  </div>\n" + "  </div>\n" + "  \n" + "    <!--[if gte mso 9]>\n" + "      </v:textbox></v:rect>\n"
				+ "    </td>\n" + "    </tr>\n" + "    </table>\n" + "    <![endif]-->\n" + "    \n" + "\n" + "\n"
				+ "  \n" + "  \n" + "<div class=\"u-row-container\" style=\"padding: 0px;background-color: #fdeed2\">\n"
				+ "  <div class=\"u-row\" style=\"margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">\n"
				+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">\n"
				+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: #fdeed2;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: transparent;\"><![endif]-->\n"
				+ "      \n"
				+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"background-color: #ffffff;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->\n"
				+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
				+ "  <div style=\"background-color: #ffffff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">\n"
				+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->\n"
				+ "  \n"
				+ "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:50px 30px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n"
				+ "  <div class=\"v-font-size\" style=\"font-size: 14px; line-height: 140%; text-align: center; word-wrap: break-word;\">\n"
				+ "    <p style=\"line-height: 140%;\">Dear <b>" + receiptName + "</b>,</p>\n"
				+ "<p style=\"line-height: 140%;\">I hope this message finds you well. I am excited to introduce our newest team member, who will be joining our journey</p>\n"
				+ "<p style=\"line-height: 140%;\">" + url + "</p>\n" + "<p style=\"line-height: 140%;\">" + code
				+ "</p>\n" + "  </div>\n" + "\n" + "      </td>\n" + "    </tr>\n" + "  </tbody>\n" + "</table>\n"
				+ "\n"
				+ "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n" + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
				+ "  <tr>\n" + "    <td style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\">\n"
				+ "      \n"
				+ "      <img align=\"center\" border=\"0\" src=\"https://firebasestorage.googleapis.com/v0/b/hedging-1d816.appspot.com/o/fxcm%2Fimage-1.png?alt=media&token=39dc98d0-05a6-4b09-90ed-61c2452e9801\" alt=\"image\" title=\"image\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 100%;max-width: 580px;\" width=\"580\" class=\"v-src-width v-src-max-width\"/>\n"
				+ "      \n" + "    </td>\n" + "  </tr>\n" + "</table>\n" + "\n" + "      </td>\n" + "    </tr>\n"
				+ "  </tbody>\n" + "</table>\n" + "\n" + "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n"
				+ "  </div>\n" + "</div>\n" + "<!--[if (mso)|(IE)]></td><![endif]-->\n"
				+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n" + "    </div>\n"
				+ "  </div>\n" + "  </div>\n" + "  \n" + "\n" + "\n" + "  \n" + "  \n"
				+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: #fdeed2\">\n"
				+ "  <div class=\"u-row\" style=\"margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">\n"
				+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">\n"
				+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: #fdeed2;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: transparent;\"><![endif]-->\n"
				+ "      \n"
				+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"background-color: #ffffff;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->\n"
				+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
				+ "  <div style=\"background-color: #ffffff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">\n"
				+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->\n"
				+ "  \n"
				+ "<table id=\"u_content_social_1\" style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:60px 10px 10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n" + "<div align=\"center\">\n" + "  <div style=\"display: table; max-width:167px;\">\n"
				+ "  <!--[if (mso)|(IE)]><table width=\"167\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"border-collapse:collapse;\" align=\"center\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse; mso-table-lspace: 0pt;mso-table-rspace: 0pt; width:167px;\"><tr><![endif]-->\n"
				+ "  \n" + "    \n"
				+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 10px;\" valign=\"top\"><![endif]-->\n"
				+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 10px\">\n"
				+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "        <a href=\"https://www.facebook.com/unlayer\" title=\"Facebook\" target=\"_blank\">\n"
				+ "          <img src=\"https://firebasestorage.googleapis.com/v0/b/hedging-1d816.appspot.com/o/fxcm%2Fimage-4.png?alt=media&token=1bd88360-8dfe-474f-8b84-72909396c19a\" alt=\"Facebook\" title=\"Facebook\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "        </a>\n" + "      </td></tr>\n" + "    </tbody></table>\n"
				+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n" + "    \n"
				+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 10px;\" valign=\"top\"><![endif]-->\n"
				+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 10px\">\n"
				+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "        <a href=\"https://twitter.com/unlayerapp\" title=\"Twitter\" target=\"_blank\">\n"
				+ "          <img src=\"https://firebasestorage.googleapis.com/v0/b/hedging-1d816.appspot.com/o/fxcm%2Fimage-5.png?alt=media&token=9c7efc6d-6d6e-4ea2-b43b-ee00f207a1d6\" alt=\"Twitter\" title=\"Twitter\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "        </a>\n" + "      </td></tr>\n" + "    </tbody></table>\n"
				+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n" + "    \n"
				+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 10px;\" valign=\"top\"><![endif]-->\n"
				+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 10px\">\n"
				+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "        <a href=\"https://www.linkedin.com/company/unlayer/mycompany/\" title=\"LinkedIn\" target=\"_blank\">\n"
				+ "          <img src=\"https://firebasestorage.googleapis.com/v0/b/hedging-1d816.appspot.com/o/fxcm%2Fimage-6.png?alt=media&token=8e7b07d7-def5-4508-8f47-15b5c8ec3a8f\" alt=\"LinkedIn\" title=\"LinkedIn\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "        </a>\n" + "      </td></tr>\n" + "    </tbody></table>\n"
				+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n" + "    \n"
				+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 0px;\" valign=\"top\"><![endif]-->\n"
				+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"width: 32px !important;height: 32px !important;display: inline-block;border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 0px\">\n"
				+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
				+ "        <a href=\"https://www.instagram.com/unlayer_official/\" title=\"Instagram\" target=\"_blank\">\n"
				+ "          <img src=\"https://firebasestorage.googleapis.com/v0/b/hedging-1d816.appspot.com/o/fxcm%2Fimage-3.png?alt=media&token=a4582c4b-3d58-4dbd-9393-b50fe7fcd853\" alt=\"Instagram\" title=\"Instagram\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
				+ "        </a>\n" + "      </td></tr>\n" + "    </tbody></table>\n"
				+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n" + "    \n" + "    \n"
				+ "    <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n" + "  </div>\n" + "</div>\n"
				+ "\n" + "      </td>\n" + "    </tr>\n" + "  </tbody>\n" + "</table>\n" + "\n"
				+ "<table id=\"u_content_text_2\" style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px 100px 30px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n"
				+ "  <div class=\"v-font-size\" style=\"font-size: 14px; line-height: 170%; text-align: center; word-wrap: break-word;\">\n"
				+ "    <p style=\"font-size: 14px; line-height: 170%;\">UNSUBSCRIBE   |   PRIVACY POLICY   |   WEB</p>\n"
				+ "<p style=\"font-size: 14px; line-height: 170%;\"> </p>\n"
				+ "<p style=\"font-size: 14px; line-height: 170%;\">Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore.</p>\n"
				+ "  </div>\n" + "\n" + "      </td>\n" + "    </tr>\n" + "  </tbody>\n" + "</table>\n" + "\n"
				+ "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:0px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n"
				+ "  <table height=\"0px\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 1px solid #BBBBBB;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
				+ "    <tbody>\n" + "      <tr style=\"vertical-align: top\">\n"
				+ "        <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
				+ "          <span>&#160;</span>\n" + "        </td>\n" + "      </tr>\n" + "    </tbody>\n"
				+ "  </table>\n" + "\n" + "      </td>\n" + "    </tr>\n" + "  </tbody>\n" + "</table>\n" + "\n"
				+ "<table id=\"u_content_image_3\" style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
				+ "  <tbody>\n" + "    <tr>\n"
				+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:30px 10px 60px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
				+ "        \n" + "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n" + "  </div>\n" + "</div>\n"
				+ "<!--[if (mso)|(IE)]></td><![endif]-->\n"
				+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n" + "    </div>\n"
				+ "  </div>\n" + "  </div>\n" + "  \n" + "\n" + "\n"
				+ "    <!--[if (mso)|(IE)]></td></tr></table><![endif]-->\n" + "    </td>\n" + "  </tr>\n"
				+ "  </tbody>\n" + "  </table>\n" + "  <!--[if mso]></div><![endif]-->\n"
				+ "  <!--[if IE]></div><![endif]-->\n" + "</body>";
	}
}
