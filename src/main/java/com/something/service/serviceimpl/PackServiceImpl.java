package com.something.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.Pack;
import com.something.repo.PackRepo;
import com.something.service.PackService;

@Service
public class PackServiceImpl implements PackService{
	@Autowired
	PackRepo packRepo;

	@Override
	public List<Pack> getAllPackges() {
		// TODO Auto-generated method stub
		return packRepo.findAll();
	}

	@Override
	public Pack findById(int id) {
		// TODO Auto-generated method stub
		return packRepo.getById(id);
	}

	@Override
	public void savePack(String name, long price, double direct, double daily) {
		// TODO Auto-generated method stub
		Pack pack = new Pack();
		pack.setDaily(daily);
		pack.setName(name);
		pack.setDirectCommission(direct);
		pack.setPrice(price);
		pack.setStatus(0);
		
		packRepo.save(pack);
	}

	@Override
	public List<Pack> findByName(String name) {
		// TODO Auto-generated method stub
		return packRepo.findByName(name);
	}

	@Override
	public void toggleStatus(int packageId) {
		// TODO Auto-generated method stub
		Pack pack = packRepo.getById(packageId);
		int newStatus;
		if (pack.getStatus() == 1) {
			newStatus = 0;
		} else {
			newStatus = 1;
		}
		pack.setStatus(newStatus);
		
		packRepo.save(pack);
	}

}
