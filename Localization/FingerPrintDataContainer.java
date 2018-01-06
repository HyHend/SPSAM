package net.hyhend.spsam.Localization;

import java.util.ArrayList;

public class FingerPrintDataContainer {

	
	private ArrayList<RssiFingerPrint> fingerPrints;
	
	
	public FingerPrintDataContainer()
	{
		fingerPrints = new ArrayList<RssiFingerPrint>();	
	}
	
	public void addFingerPrint(RssiFingerPrint fingerPrint)
	{
		fingerPrints.add(fingerPrint);
	}
	
	public ArrayList<RssiFingerPrint> getFingerPrints()
	{
		return fingerPrints;
	}
	
	public ArrayList<RssiFingerPrint> getFingerPrintsForLocation(Location location)
	{
		ArrayList<RssiFingerPrint> fingerPrintsForLocation = new ArrayList<RssiFingerPrint>();
		
		for(RssiFingerPrint fingerPrint : fingerPrints)
		{
			if (fingerPrint.getLocation() == location)
			{
				fingerPrintsForLocation.add(fingerPrint);
			}
		}			
		return fingerPrintsForLocation;			
	}
}
