package net.hyhend.spsam.ParticleFilter;

import java.util.ArrayList;

import net.hyhend.spsam.Utils.Constants;

public class Particles {
	private ArrayList<Particle> particles;
	private boolean listBusy;

	/**
	 * Constructor
	 */
	public Particles () {
		particles = new ArrayList<Particle>();
		listBusy = false;
	}
	
	/**
	 * Constructor with initial particles
	 */
	public Particles (CollisionMap map, int amount) {
		particles = new ArrayList<Particle>();
		
		// Create #amount randomly placed particles
		for(int i=0; i<amount; i++) {
			createParticle(map);
		}
	}
	
	/**
	 * @return particles
	 */
	public ArrayList<Particle> getParticles() {
		return this.particles;
	}
	
	/**
	 * Returns particles busy flag
	 * @return busy
	 */
	public boolean getBusy () {
		return listBusy;
	}
	
	/**
	 * Adds particle
	 * @param p
	 */
	public void addParticle(Particle p) {
		particles.add(p);
	}
	
	/**
	 * Creates particle somewhere within map
	 * @param map
	 */
	public void createParticle(CollisionMap map) {
		Particle p = new Particle(map);
		particles.add(p);
	}
	
	/**
	 * Moves particles in given direction with given distance and given deviations
	 * @param distance
	 * @param distanceDeviation
	 * @param angle (Radians)
	 * @param angleDeviation (Radians)
	 */
	public void moveParticles(CollisionMap map, double distance, double distanceDeviation, double angle, double angleDeviation) {
		// For each particle
		// 	- Random distance between distance+distanceDeviation and distance-distanceDeviation
		//  - Random angle between angle-angleDeviation and angle+angleDeviation
		ArrayList<Particle> removeParticles = new ArrayList<Particle>();

		listBusy = true;
		for(Particle particle : particles) {

			// Angle + or - random between angle and angleDeviation
			double useAngle = Constants.collision_map_north_offset + angle+(((Math.random()*2)-1)*angleDeviation);		
			
			// Distance + or - random between distance and distanceDeviation
			double useDistance = distance+(((Math.random()*2)-1)*distanceDeviation);	

			// Actually move the particle
			boolean result = particle.moveParticle(map, useAngle, useDistance);
			if(!result) {
				removeParticles.add(particle);
			}
		}
		listBusy = false;
		
		// Removes particles which have crossed walls in their paths
		// Also creates new particle for every removed particle
		removeAndRepositionParticles(map, removeParticles);
	}
	
	/**
	 * Moves particles in given distance with given deviation
	 * @param distance
	 * @param distanceDeviation
	 */
	public void moveParticles(CollisionMap map, double distance, double distanceDeviation) {
		// For each particle
		// 	- Random distance between distance+distanceDeviation and distance-distanceDeviation
		//  - For 35% of the particles: Angle random between currentangle+someSmallDeviation and currentangle-someSmallDeviation
		//  - For 35% of the particles: Angle random between currentangle+someLargerDeviation and currentangle-someLargerDeviation
		//  - For 30% of the particles: Angle random between 0 and 2PI
		double smallAngleDeviation = 0.25;	// Deviation of 0.5PI
		double mediumAngleDeviation = 0.5;	// Deviation of 1.0PI
		ArrayList<Particle> removeParticles = new ArrayList<Particle>();

		listBusy = true;
		for(Particle particle : particles) {
			double useDistance = distance+(((Math.random()*2)-1)*distanceDeviation);		// Distance + or - random between 0 and distanceDeviation
			
			// Calculate angle, P=0.35 for smallDeviation, 0.35 for mediumDeviation, 0.3 for 0-2PI
			double useAngle = Math.random()*2*Math.PI;								// Angle random between 0 and 2PI (default)
			double randomBlock = Math.random();
			if(randomBlock < 0.35) {
				useAngle = particle.getAngle() + (((Math.random()*2)-1)*smallAngleDeviation);	// Angle + or - random between 0 and smallAngleDeviation
			}
			else if(randomBlock < 0.7) {
				useAngle = particle.getAngle() + (((Math.random()*2)-1)*mediumAngleDeviation);	// Angle + or - random between 0 and mediumAngleDeviation
			}
			
			// Actually move the particle
			boolean result = particle.moveParticle(map, useAngle, useDistance);
			if(!result) {
				removeParticles.add(particle);
			}
		}
		listBusy = false;
		
		// Removes particles which have crossed walls in their paths
		// Also creates new particle for every removed particle
		removeAndRepositionParticles(map, removeParticles);
	}
	
	/**
	 * Removes given particles and adds one random other particle
	 * @param toRemove
	 */
	private void removeAndRepositionParticles (CollisionMap map, ArrayList<Particle> toRemove) {
		//System.out.println("REMOVEANDREPOSITION: TOTAL: "+particles.size()+" REMOVED: "+toRemove.size());
		
		// Remove given particles
		listBusy = true;
		for(Particle particle : toRemove) {
			this.particles.remove(particle);
		}
		
		// Clone random old particle for every removed particle
		/*for(int i=0; i<toRemove.size(); i++) {
			// 99% chance that a random other will be cloned. Otherwise choose random position 
			// (also when there are no particles to be cloned)
			if(particles.size() > 0
					&& Math.random() < 0.99) {
				Particle randomParticle = this.particles.get((int) Math.round(Math.random()*(this.particles.size()-1)));
				
				// Copy given random particle
				// Position within 3px square
				// Copy angle and placementOffset
				int newX = randomParticle.getX() + Double.valueOf((Math.random() - 0.5) * 6).intValue();	
				int newY = randomParticle.getY() + Double.valueOf((Math.random() - 0.5) * 6).intValue();
				Particle newParticle = new Particle(map, newX, newY, randomParticle.getAngle(), randomParticle.getPlacementOffset());
				particles.add(newParticle);
			}
			else {
				// There were no particles to copy, add random new one
				Particle newParticle = new Particle(map);
				particles.add(newParticle);
			}
		}*/
		
		if(particles.size() > 0) {
			for(int i=0; i<toRemove.size(); i++) {
				Particle randomParticle = this.particles.get((int) Math.round(Math.random()*(this.particles.size()-1)));
				
				// Copy given random particle
				// Position within 3px square
				// Copy angle and placementOffset
				int newX = randomParticle.getX() + Double.valueOf((Math.random() - 0.5) * 6).intValue();	
				int newY = randomParticle.getY() + Double.valueOf((Math.random() - 0.5) * 6).intValue();
				Particle newParticle = new Particle(map, newX, newY, randomParticle.getAngle(), randomParticle.getPlacementOffset());
				particles.add(newParticle);
			}
		}
		else {
			// There were no particles to copy, add all new randomly
			// Create #amount randomly placed particles
			for(int i=0; i<toRemove.size(); i++) {
				createParticle(map);
			}
		}
		listBusy = false;
	}
}
