package com.something.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Investment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@NotNull
	private String username;
	@NotNull
	private int packageId;
	@NotNull
	private String time;
	@NotNull
	private String timeEnd;
	@NotNull
	private double capital;
	@NotNull
	private String code;
	@NotNull
	private int count;
	@NotNull
	private double remain;
	@NotNull
	private double claimable;
}
