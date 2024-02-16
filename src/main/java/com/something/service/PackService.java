package com.something.service;

import java.util.List;

import com.something.domain.Pack;

public interface PackService {
	List<Pack> getAllPackges();
	Pack findById(int id);
	void savePack(String name, long price, double direct, double daily);
	List<Pack> findByName(String name);
	void toggleStatus(int packageId);
}
