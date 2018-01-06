package net.hyhend.spsam.Localization;

public class RssiFingerPrint {
	private  double rssi;
	private  String identifier;
	private  Location location;
	
	public RssiFingerPrint(String identifier,double receivedSignalStrength, Location location)
	{
		rssi = receivedSignalStrength;
		this.identifier = identifier;		
		this.location = location;
	}

	/**
	 * @return location
	 */
	public Location getLocation()
	{
		return location;
	}

	/**
	 * @return rssi
	 */
	public double getRssi() {
		return rssi;
	}

	/**
	 * @return identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Compares this fingerprint to given fingerprint
	 * @param compare
	 * @return similarity
	 */
	public double compare(RssiFingerPrint compare) {
		return Math.sqrt(Math.pow((this.rssi - compare.getRssi()),2));
	}
}
