package com.something.domain;

import static javax.persistence.FetchType.EAGER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String name;
	@NotNull
	private String username;
	@NotNull
	private String password;
	@NotNull
	private String email;
	@NotNull
	private String rootUsername;
	@Column(columnDefinition="TEXT")
	private String idBack;
	@Column(columnDefinition="TEXT")
	private String idFront;
	@Column(columnDefinition="TEXT")
	private String confirmBack;
	@Column(columnDefinition="TEXT")
	private String confirmFront;
	private String ref;
	private boolean mfaEnabled;
	@Column(columnDefinition="TEXT")
	private String contact;
	
	@Column(columnDefinition="TEXT")
	private String kycMessage;
	private String address;
	private String pkey;
	private String skey;
    private String secret;
	private int level;
	private long sales;
	private long teamsales;
	private boolean isLocked;
	private boolean isActived;
	private boolean isRanked;
	private String identity;
	private int kycStatus;
	private String phoneNumber;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonIgnore
    private List<Transaction> transactions;

	@ManyToMany(fetch = EAGER)
    private Collection<Role> roles = new ArrayList<>();
}
