package net.hyhend.spsam.ParticleFilter;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.hyhend.spsam.Utils.Constants;

import android.util.Log;

public class ParticleFilter {
	private CollisionMap collisionMap;
	private Particles particles;
	
	/**
	 * Constructor
	 */
	public ParticleFilter () {
		// Read collisionMap
        collisionMap = new CollisionMap("EWI_HB9");
        
        // Create 1k particles
        particles = new Particles(collisionMap, Constants.numParticles);
        
	}
	
	/**
	 * Resets the particles
	 */
	public void resetParticles() {
        this.particles = new Particles(collisionMap, Constants.numParticles);
	}
	
	/**
	 * @return particles
	 */
	public Particles getParticles() {
		return this.particles;
	}
	
	/**
	 * 
	 */
	public void verifyParticles () {
		ArrayList<Particle> ps = this.particles.getParticles();
		int countWrong = 0;
		for(Particle p : ps) {
			if(collisionMap.getValueFor(p.getX(), p.getY()) == false) {
				countWrong++;
			}
		}
		
		System.out.println("AMOUNT OF WRONGLY PLACED PARTICLES: "+ countWrong);
	}
	
	/**
	 * Returns name of current UI map
	 * @return String
	 */
	public String getCurrentUIMap () {
		return collisionMap.getUIMap();
	}

	/**
	 * Moves particles in general distance, angle
	 */
	public void motion (final double distance, final double angle) {
		// Perform move
		if(!particles.getBusy()) {
			particles.moveParticles(collisionMap, distance, (distance*Constants.distanceDeviation), angle, Constants.angleDeviation);	
		}
		else {
			// Try again in 50ms
			final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
			Runnable task = new Runnable() {
				public void run() {
					// Log
					Log.v("ParticleFilter.motion", "Particles is busy, trying again in. It might be nice to use less particles.");
					motion(distance, angle);
				}
			};
			worker.schedule(task, 100, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * Moves particles in general distance, random angle
	 */
	public void motion (final double distance) {
		// Perform move
		if(!particles.getBusy()) {
			particles.moveParticles(collisionMap, distance, (distance*Constants.distanceDeviation));	

		}
		else {
			// Try again in 50ms
			final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
			Runnable task = new Runnable() {
				public void run() {
					// Log
					Log.v("ParticleFilter.motion", "Particles is busy, trying again in. It might be nice to use less particles.");
					
					motion(distance);
				}
			};
			worker.schedule(task, 100, TimeUnit.MILLISECONDS);
		}
	}
}
