package net.hyhend.spsam.ParticleFilter;

public class Particle {
	private int x;
	private int y;
	private double angle;					//Radians
	private double placementAngleOffset;	//Radians
	private int attempts;
	
	/**
	 * Constructor (known x, y, angle)
	 * Chooses position randomly when given position is obstructed
	 * @param map
	 * @param x
	 * @param y
	 * @param angle
	 */
	public Particle(CollisionMap map, int x, int y, double angle, double placementOffset) {
		this.attempts = 0;
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.placementAngleOffset = placementOffset;
	}
	
	/**
	 * Constructor
	 * @param map
	 * @param width
	 * @param height
	 */
	public Particle(CollisionMap map) {
		this.attempts = 0;
		choosePositionRandomly(map);
		this.angle = Math.random() * 2 * Math.PI;
		//this.placementAngleOffset = Math.random() * 2 * Math.PI;		// In any direction (for example handbag placement is arbitrary)
		this.placementAngleOffset = ((Math.random() / 2) - 0.25) * Math.PI;		// +-45deg (Front pocket placement)
	}
	
	/**
	 * @return x
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * @return y
	 */
	public int getY() {
		return this.y;
	}
	
	/**
	 * @return angle
	 */
	public double getAngle() {
		return this.angle;
	}
	
	/**
	 * @return placementAngleOffset
	 */
	public double getPlacementOffset() {
		return this.placementAngleOffset;
	}
	
	/**
	 * Chooses random position in map. Tries again when obstructed
	 * @param map
	 */
	public void choosePositionRandomly (CollisionMap map) {
		this.x = (int) Math.round(Math.random() * map.getWidth());
		this.y = (int) Math.round(Math.random() * map.getHeight());
		
		// Check obstruction (Max 99 attempts)
		if(map.getValueFor(x, y) == false && attempts <= 99) {
			this.attempts++;
			choosePositionRandomly(map);
		}
		else {		
			this.attempts = 0;		// Reset attempts
		}
	}
	
	/**
	 * Moves particle in given direction, with given distance
	 * Return true when no collision was detected on path, false otherwise
	 * @param map
	 * @param angle (Radians)
	 * @param distance (CM)
	 * @return boolean
	 */
	public Boolean moveParticle(CollisionMap map, double angle, double distance) {
		// Initiate collision (none found yet)
		boolean collision = false;
		
		// Calculate x and y distance
		int xFinal = this.x + ((int) Math.round((distance * map.getDistancePerPixel()) * Math.cos(angle + this.placementAngleOffset)));
		int yFinal = this.y + ((int) Math.round((distance * map.getDistancePerPixel()) * Math.sin(angle + this.placementAngleOffset)));
		
		// Check if collision and set false on collision, true otherwise
		// (uses Bresemham's line algorithm)
		int x1 = this.x;	
		int y1 = this.y;	
		int x2 = xFinal;		// A bit unnecessary, more clear
		int y2 = yFinal;		// A bit unnecessary, more clear
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);

		int sx = (x1 < x2) ? 1 : -1;
		int sy = (y1 < y2) ? 1 : -1;

		int err = dx - dy;
		while (true) {
			if(map.getValueFor(x1, y1) == false) {
				collision = true;
				break;			// Don't have to check further, collision found on path
			}

		    if (x1 == x2 && y1 == y2) {
		        break;
		    }

		    int e2 = 2 * err;

		    if (e2 > -dy) {
		        err = err - dy;
		        x1 = x1 + sx;
		    }

		    if (e2 < dx) {
		        err = err + dx;
		        y1 = y1 + sy;
		    }
		}
		
		// Set new values
		this.x = xFinal;
		this.y = yFinal;
		this.angle = angle;
		
		return !collision;	// Returns true when no collision occurred
	}
}
