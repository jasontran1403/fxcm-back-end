package com.something.service.serviceimpl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.Affiliate;
import com.something.repo.AffiliateRepo;
import com.something.service.AffiliateService;

@Service
public class AffiliateServiceImpl implements AffiliateService {
	@Autowired
	AffiliateRepo affRepo;

	@Override
	public Affiliate getByUUID(String uuid) {
		// TODO Auto-generated method stub
		Affiliate aff = affRepo.findByUUID(uuid);
		if (aff == null) {
			throw new RuntimeException("This uuid is not existed");
		}
		return aff;
	}

	@Override
	public void updateRegistered(String uuid) {
		// TODO Auto-generated method stub
		Affiliate aff = affRepo.findByUUID(uuid);
		affRepo.delete(aff);
	}

	@Override
	public Affiliate addRegisURL(String root, String placement, String side) {
		// TODO Auto-generated method stub
		String uuid = UUID.randomUUID().toString();
		Affiliate newAff = new Affiliate();
		newAff.setUuid(uuid);
		newAff.setRoot(root);
		newAff.setPlacement(placement);
		newAff.setSide(side);
		return affRepo.save(newAff);
	}

	@Override
	public List<Affiliate> getByRoot(String root) {
		// TODO Auto-generated method stub
		return affRepo.findByRoot(root);
	}

	@Override
	public List<Affiliate> getByPlacement(String placement) {
		// TODO Auto-generated method stub
		return affRepo.findByPlacement(placement);
	}

	@Override
	public Affiliate getByPlacementAndSide(String placement, String side) {
		// TODO Auto-generated method stub
		return affRepo.findByPlacementAndSide(placement, side);
	}

}
