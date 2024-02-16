package com.something.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.something.domain.Activation;
import com.something.repo.ActivationRepo;
import com.something.service.ActivationService;

@Service
public class ActivationServiceImpl implements ActivationService{
	@Autowired
	ActivationRepo actiRepo;

	@Override
	public Activation getActivation(String uuid) {
		// TODO Auto-generated method stub
		return actiRepo.findByUUID(uuid);
	}
	
	

	@Override
	public Activation reGenerateActi(Activation acti, int newCode) {
		// TODO Auto-generated method stub
		acti.setActivation(newCode);
		return actiRepo.save(acti);
	}

	@Override
	public void activated(Activation acti) {
		// TODO Auto-generated method stub
		actiRepo.delete(acti);
	}



	@Override
	public Activation save(Activation acti) {
		// TODO Auto-generated method stub
		return actiRepo.save(acti);
	}



	@Override
	public Activation getActivationByUsername(String username) {
		// TODO Auto-generated method stub
		return actiRepo.findByUsername(username);
	}
}
