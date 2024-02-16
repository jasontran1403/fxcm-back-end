package com.something.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.Recovery;
import com.something.repo.RecoveryRepo;
import com.something.service.RecoveryService;

@Service
public class RecoveryServiceImpl implements RecoveryService{
	@Autowired
	RecoveryRepo recoRepo;

	@Override
	public Recovery findByUUID(String uuid) {
		// TODO Auto-generated method stub
		return recoRepo.findByUUID(uuid);
	}

	@Override
	public void remove(String uuid) {
		// TODO Auto-generated method stub
		Recovery reco = recoRepo.findByUUID(uuid);
		recoRepo.delete(reco);
	}

	@Override
	public Recovery saveRecovery(Recovery reco) {
		// TODO Auto-generated method stub
		return recoRepo.save(reco);
	}
	

}
