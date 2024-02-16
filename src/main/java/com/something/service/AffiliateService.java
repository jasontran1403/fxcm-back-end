package com.something.service;

import java.util.List;

import com.something.domain.Affiliate;

public interface AffiliateService {
	public Affiliate getByUUID(String uuid);
	public Affiliate getByPlacementAndSide(String placement, String side);
	public List<Affiliate> getByRoot(String root);
	public List<Affiliate> getByPlacement(String placement);
	public void updateRegistered(String uuid);
	public Affiliate addRegisURL(String root, String placement, String side);
}
