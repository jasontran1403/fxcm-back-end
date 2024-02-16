package com.something.dto;

import com.something.domain.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
	private User user;
	private double leftrefsale;
	private double rightrefsale;
	private double cashbalance;
	private double commissionbalance;
}
