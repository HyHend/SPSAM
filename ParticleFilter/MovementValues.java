package net.hyhend.spsam.ParticleFilter;

public class MovementValues {
	double distance;
	double minAzimuth;
	double maxAzimuth;
	
	/**
	 * Constructor
	 * @param distance
	 * @param azimuth
	 */
	public MovementValues (double distance, double minAzimuth, double maxAzimuth) {
		this.distance = distance;
		this.minAzimuth = minAzimuth;
		this.maxAzimuth = maxAzimuth;
	}
	
	/**
	 * @return distance
	 */
	public double getDistance () {
		return this.distance;
	}

	/**
	 * @return maxAzimuth
	 */
	public double getMinAzimuth () {
		return this.minAzimuth;
	}

	/**
	 * @return maxAzimuth
	 */
	public double getMaxAzimuth () {
		return this.maxAzimuth;
	}
}
