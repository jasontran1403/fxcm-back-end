package com.something.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoDto {
	private String username;
	private String fullname;
	private String contact;
	private String phone;
	private String identity;
	private String email;

}
