package com.something.service;

import java.util.HashMap;
import java.util.List;

import com.something.domain.Role;
import com.something.domain.User;
import com.something.dto.InfoDto;
import com.something.dto.WalletResponse;


public interface UserService {
    User saveUser(User user);
    User findByEmail(String email);
    List<User> findUserKyc(int status);
    List<User> findByRootUsername(String rootUsername);
    String regis(User user);
    Role saveRole(Role role);
    void addRoleToUser(String username, String roleName);
    User getUser(String username);
    User updateRef(String username, String usernameRef, String side);
    HashMap<String, List<User>> getMapDown(String username);
    List<User> getTreeUp(String username);
    List<User>getUsers();
    List<User> getTreeUpToRoot(String username);
    String refLink(String ref);
    void updateSale(String username, long sale);
    void updateSaleFromWithdraw(String username, long sale);
    void updateteamSale(String username, long sale);
    void updateteamSaleWithdraw(String username, long sale);
    void calRank();
    void updateMaxOut(User user, double amount, String type);
    void enabledAuthen(User user);
    void activated(User user);
    void disabledAuthen(User user);
    void changePassword(User user);
    void updateSecrect(User user);
    void saveWallet(long userId, WalletResponse response);
    void updateInfo(InfoDto info);
    void saveKyc(String username, List<String> urls);
    void processKyc(String username, String message, int status);
}
