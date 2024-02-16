package com.something.domain;

import javax.persistence.Id;

import lombok.Data;

@Data
public class RoleToUser {
	
	@Id
    private String username;
    private String roleName;
}

