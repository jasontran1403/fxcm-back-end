package com.something.service.serviceimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.something.domain.CashWallet;
import com.something.domain.CommissionWallet;
import com.something.domain.Role;
import com.something.domain.User;
import com.something.dto.InfoDto;
import com.something.dto.WalletResponse;
import com.something.repo.RoleRepo;
import com.something.repo.UserRepo;
import com.something.service.CashWalletService;
import com.something.service.CommissionWalletService;
import com.something.service.UserService;
import com.something.utils.WalletUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
	@Autowired
	UserRepo userRepo;

	@Autowired
	RoleRepo roleRepo;

	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	CommissionWalletService cmwService;
	
	@Autowired
	CashWalletService cwService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepo.findByUsername(username);
		if (user == null || user.isActived() == false) {
			throw new UsernameNotFoundException("User is invalid, or is not active");
		} else {
			Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
			user.getRoles().forEach(role -> {
				authorities.add(new SimpleGrantedAuthority(role.getName()));
			});
			return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
					authorities);
		}
	}

	@Override
	public User saveUser(User user) {
		log.info("Saving new user {} to the database", user.getName());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role role = roleRepo.findByName("ROLE_ADMIN");
		user.getRoles().add(role);

		User existedUserByUsername = userRepo.findByUsername(user.getUsername());
		if (existedUserByUsername != null) {
			throw new RuntimeException("This username already existed");
		}

		User existedUserByEmail = userRepo.findByEmail(user.getEmail());
		if (existedUserByEmail != null) {
			throw new RuntimeException("This email address already existed");
		}
		return userRepo.save(user);
	}

	@Override
	public String regis(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role role = roleRepo.findByName("ROLE_USER");
		user.getRoles().add(role);

		User existedUserByUsername = userRepo.findByUsername(user.getUsername());
		if (existedUserByUsername != null) {
			return ("This username already existed");
		}

		User existedUserByEmail = userRepo.findByEmail(user.getEmail());
		if (existedUserByEmail != null) {
			return ("This email address already existed");
		}
		User sponsor = userRepo.findByUsername(user.getRootUsername());
		if (sponsor == null) {
			return ("This sponsor is not existed");
		}
		log.info("Saving new user {} to the database", user.getName());
		CommissionWallet cmw = new CommissionWallet();
		cmw.setBalance(0);
		cmw.setUsername(user.getUsername());
		cmwService.createCommissionWallet(cmw);

		CashWallet cw = new CashWallet();
		cw.setBalance(0);
		cw.setUsername(user.getUsername());
		cwService.createCashWallet(cw);
		
		user.setActived(true);
		
		String uuid = UUID.randomUUID().toString();
		String ref = uuid.split("-")[0];
		user.setRef(ref);
		
		User result = userRepo.save(user);
		
		WalletResponse response;
		try {
			response = WalletUtils.generateBSCWallet();
			user.setAddress(response.getAddress());
			user.setPkey(response.getPublicKey());
			user.setSkey(response.getSecretKey());
			
			userRepo.save(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return "success";
	}

	@Override
	public Role saveRole(Role role) {
		log.info("Saving new role {} to the database", role.getName());
		return roleRepo.save(role);
	}

	@Override
	public void addRoleToUser(String username, String roleName) {
		log.info("Adding role {} to user {}", roleName, username);
		User user = userRepo.findByUsername(username);
		Role role = roleRepo.findByName(roleName);
		user.getRoles().add(role);
	}

	@Override
	public User getUser(String username) {
		log.info("Fetching user {}", username);
		return userRepo.findByUsername(username);
	}

	@Override
	public List<User> getUsers() {
		log.info("Fetching all users");
		return userRepo.findAll();
	}

	@Override
	public User updateRef(String username, String usernameRef, String side) {
		User user = userRepo.findByUsername(username);
		// TODO Auto-generated method stub
		if (user == null) {
			throw new RuntimeException("This user doesnt exists");
		}
		
		return user;
	}

	@Override
	public HashMap<String, List<User>> getMapDown(String username) {
		// TODO Auto-generated method stub
		HashMap<String, List<User>> userTree = new HashMap<>();
		userTree = findMapDown(username);
		return userTree;
	}

	public HashMap<String, List<User>> findMapDown(String username) {
		HashMap<String, List<User>> userTreeMap = new HashMap<>();
		List<User> listUser = new ArrayList<>();
		User user = userRepo.findByUsername(username);
		listUser.add(user);
		userTreeMap.put("Root", listUser);

		List<User> userTreeL1 = userRepo.findByRoot(username);
		userTreeMap.put("L1", userTreeL1);

		for (int i = 1; i < 15; i++) {
			List<User> userTree = userTreeMap.get("L" + i);
			if (userTree == null) {
				break;
			}
			List<User> nextUserTree = new ArrayList<>();
			for (User item : userTree) {
				List<User> next = new ArrayList<>();
				next = userRepo.findByRoot(item.getUsername());
				if (next == null) {
					continue;
				}
				nextUserTree.addAll(next);
			}
			userTreeMap.put("L" + (i + 1), nextUserTree);
		}

		return userTreeMap;
	}

	@Override
	public List<User> getTreeUp(String username) {
		// TODO Auto-generated method stub
		List<User> listUser = new ArrayList<>();
		User user = userRepo.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("This username is not exists");
		}
		listUser = findTreeUp(username);
		return listUser;
	}

	public List<User> findTreeUp(String username) {
		List<User> listUser = new ArrayList<>();
		User user = userRepo.findByUsername(username);
		listUser.add(user);
		for (int i = 1; i < 4; i++) {
			User nextUser = userRepo.findByUsername(listUser.get(i - 1).getRootUsername());
			if (nextUser == null || nextUser.getUsername().equalsIgnoreCase("super")) {
				break;
			}
			listUser.add(nextUser);
		}

		return listUser;
	}

	@Override
	public List<User> getTreeUpToRoot(String username) {
		// TODO Auto-generated method stub
		List<User> listUser = new ArrayList<>();
		User user = userRepo.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("This username is not exists");
		}
		listUser = findTreeUpToRoot(username);
		return listUser;
	}

	public List<User> findTreeUpToRoot(String username) {
		List<User> listUser = new ArrayList<>();
		List<User> listAllUser = userRepo.findAll();
		User user = userRepo.findByUsername(username);
		listUser.add(user);
		for (int i = 1; i < listAllUser.size(); i++) {
			User nextUser = userRepo.findByUsername(listUser.get(i - 1).getRootUsername());
			if (nextUser == null || nextUser.getUsername().equalsIgnoreCase("super")) {
				break;
			}
			listUser.add(nextUser);
		}

		return listUser;
	}

	@Override
	public void updateSale(String username, long sale) {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(username);
		user.setSales(user.getSales() + sale);
		userRepo.save(user);
	}

	@Override
	public void updateteamSale(String username, long sale) {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(username);
		user.setTeamsales(user.getTeamsales() + sale);
		userRepo.save(user);
	}
	
	@Override
	public void updateteamSaleWithdraw(String username, long sale) {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(username);
		user.setTeamsales(user.getTeamsales() - sale);
		userRepo.save(user);
	}

	@Override
	public void calRank() {
		// TODO Auto-generated method stub
		List<User> allUsers = userRepo.findAll();
		for (User user : allUsers) {
			if (user.isRanked()) {
				continue;
			}
			
		}
	}

	public void updateRank(User user, int rank) {
		// TODO Auto-generated method stub
		user.setLevel(rank);
		userRepo.save(user);
	}

	@Override
	public void updateMaxOut(User user, double amount, String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enabledAuthen(User user) {
		// TODO Auto-generated method stub
		user.setMfaEnabled(true);
		userRepo.save(user);
		
	}

	@Override
	public void disabledAuthen(User user) {
		// TODO Auto-generated method stub
		user.setMfaEnabled(false);
		userRepo.save(user);
	}

	@Override
	public void changePassword(User user) {
		// TODO Auto-generated method stub
		userRepo.save(user);
	}

	@Override
	public void activated(User user) {
		// TODO Auto-generated method stub
		user.setActived(true);
		userRepo.save(user);
	}

	@Override
	public User findByEmail(String email) {
		// TODO Auto-generated method stub
		return userRepo.findByEmail(email);
	}

	@Override
	public void updateSaleFromWithdraw(String username, long sale) {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(username);
		user.setSales(sale);
		userRepo.save(user);
	}

	@Override
	public void updateSecrect(User user) {
		// TODO Auto-generated method stub
		userRepo.save(user);
	}

	@Override
	public void saveWallet(long userId, WalletResponse response) {
		// TODO Auto-generated method stub
		User user = userRepo.findById(userId).get();
		user.setAddress(response.getAddress());
		user.setPkey(response.getPublicKey());
		user.setSkey(response.getSecretKey());
		
		userRepo.save(user);
		
	}

	@Override
	public List<User> findUserKyc(int status) {
		// TODO Auto-generated method stub
		return userRepo.findByKycStatus(status);
	}

	@Override
	public void updateInfo(InfoDto info) {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(info.getUsername());
		user.setEmail(info.getEmail());
		user.setPhoneNumber(info.getPhone());
		user.setIdentity(info.getIdentity());
		user.setContact(info.getContact());
		user.setName(info.getFullname());
		
		userRepo.save(user);
	}

	@Override
	public void saveKyc(String username, List<String> urls) {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(username);
		user.setIdBack(urls.get(0));
		user.setIdFront(urls.get(1));
		user.setConfirmBack(urls.get(2));
		user.setConfirmFront(urls.get(3));
		user.setKycStatus(1);
		
		userRepo.save(user);
	}

	@Override
	public String refLink(String ref) {
		// TODO Auto-generated method stub
		User user = userRepo.findByRefLink(ref);
		if (user == null) {
			return "Not existed";
		} else {
			return user.getUsername();
		}
	}

	@Override
	public void processKyc(String username, String message, int status) {
		// TODO Auto-generated method stub
		User user = userRepo.findByUsername(username);
		if (status == 1) {
			user.setKycStatus(1);
			userRepo.save(user);
		} else if (status == 2 && !message.equals("")) {
			user.setKycStatus(2);
			user.setKycMessage(message);
			userRepo.save(user);
		}
	}

	@Override
	public List<User> findByRootUsername(String rootUsername) {
		// TODO Auto-generated method stub
		return userRepo.findListByRootUsername(rootUsername);
	}
}
