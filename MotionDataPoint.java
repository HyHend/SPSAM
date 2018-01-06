package net.hyhend.spsam;

public class MotionDataPoint {
	private long time;
	private double value;
	private double x;
	private double y;
	private double z;
	private double azimuth;
	
	/**
	 * Constructor with direction data
	 * @param time
	 * @param value
	 * @param x
	 * @param y
	 * @param z
	 * @param azimuth
	 */
	public MotionDataPoint (long time, double value, double x, double y, double z, double azimuth) {
		this.time = time;
		this.value = value;
		this.x = x;
		this.y = y;
		this.z = z;
		this.azimuth = azimuth;
	}
	
	/**
	 * Constructor
	 * @param time
	 * @param value
	 */
	public MotionDataPoint (long time, double value) {
		this.time = time;
		this.value = value;
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.azimuth = 0;
	}
	
	/**
	 * @return time
	 */
	public long getTime () {
		return this.time;
	}
	
	/**
	 * @return value
	 */
	public double getValue () {
		return this.value;
	}
	
	/**
	 * @return azimuth
	 */
	public double getAzimuth () {
		return this.azimuth;
	}
	
	/**
	 * @return x
	 */
	public double getX () {
		return this.x;
	}
	
	/**
	 * @return y
	 */
	public double getY () {
		return this.y;
	}
	
	/**
	 * @return z
	 */
	public double getZ () {
		return this.z;
	}
}
