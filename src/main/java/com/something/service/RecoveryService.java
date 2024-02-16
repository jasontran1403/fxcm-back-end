package com.something.service;

import com.something.domain.Recovery;

public interface RecoveryService {
	Recovery findByUUID(String uuid);
	Recovery saveRecovery(Recovery reco);
	void remove(String uuid);
}
