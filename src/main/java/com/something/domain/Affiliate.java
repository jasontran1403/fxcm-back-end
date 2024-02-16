package com.something.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Affiliate implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	private String uuid;
	private String root;
	
	private String placement;
	private String side;
	private boolean status = false;
}
