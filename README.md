# SPSAM

Student project (course: __smart phone sensing__) _from 2014_. Built together with Mike, a fellow CS student of mine. Added here for future reference :)

The project consists of three parts: Activity monitoring, Particle system localization and WIFI signal strength/fingerprinting localization.

### 1. Activity monitoring
Based purely on accelerometer data. Uses a fast fourier transform to retrieve the normalyzed frequency from the sensors x, y and z measurements.
This normalized frequency turned out to be good at differentiating between different activities such as walking, running, cycling and sitting/resting.

Example showing differences between activities. This knowledge was used to classify the actual activity:
![Activity feature plot](../img/activities_fft_example.png)

### 2. Particle system localization
Based on article 4. The idea behind the particle system localization is:
- Given a floorplan of a building
- You walk a path, for example 10 metres north and then 5 metres west.
- At this moment, there are only so many possibilities on the floorplan where you could've walked this path (not walking through walls, desks etc.)

This is where the particle system comes in. 
- When starting, the probability of you being on any point on the map is equal.
- To approximate this, we uniformly distribute x "particles" over the floorplan.
- When you move, each particle is moved with the amount of metres in the direction you walk.
- Some particles will hit an object and "die". They will be added in the neighborhood of existing particles.
- After moving, it is to be noticed that groups of particles are formed at possible locations on the floorplan.
- When the moved pattern is unique enough, there will only be one group. At your actual position.

Because measurements are not perfect
- The particles are moved +/-x% of the actual distance (random, per particle).
- The direction of the particles is, per particle, also randomly altered by +/-y%.
- This decreases the accuracy, but will handle real-world imperfections. The outcome is similar.

Example within the app:
- todo

### 3. WIFI signal strength/fingerprinting localization
Based on WIFI signal strength (RSSI). The idea behind this is from article 3, explaining Bayesian Indoor Positioning filters. The idea from this article is basically implemented as part of this application.

#### Articles used
1. G. F. R. B. Eladio Martin, Oriol Vinyals. Precise indoor localization using smart phones. 2010.
2. A. Junior. Fast fourier transfer result computing. http://stackoverflow.com/questions/12007071/ fft-and-accelerometer-data-why-am-i-getting-this-output August 2012.
3. D. Madigan, E. Elnahrawy, R. P. Martin, Wen-Hua, P. Krishnan, and A. Krishnakumar. Bayesian indoor positioning systems, July 2005.
4. A. Rai, K. K. Chintalapudi, V. N. Padbanabhan, and R. Sen. Zee: Zero-effort crowdsourcing for indoor localization. august 2012.
5. N. Roy, H. Wang, and R. R. Choudhury. ’i am a smartphone and i can tell my user’s walking direction’.
6. B. P. M. m. Sauvik Das, LaToya Green. Detecting user activities using the accelerometer on android smartphones, July 2010.
7. C. University. Fast fourier transfer. https://www.ee.columbia.edu/~ronw/code/ MEAPsoft/doc/html/FFT_8java-source.html, 2006.