package com.something.service;

import com.something.domain.Activation;

public interface ActivationService {
	public Activation save(Activation acti);
	public Activation getActivation(String uuid);
	public Activation getActivationByUsername(String username);
	public Activation reGenerateActi(Activation acti, int newCode);
	public void activated(Activation acti);
}
