package net.hyhend.spsam.UI;

import java.util.ArrayList;
import java.util.Locale;
import net.hyhend.spsam.MotionDataAnalyzer;
import net.hyhend.spsam.MotionHandler;
import net.hyhend.spsam.R;
import net.hyhend.spsam.ParticleFilter.ParticleFilter;
import net.hyhend.spsam.ParticleFilter.RealTimeMotionDataAnalyzer;
import net.hyhend.spsam.TrainingDataFileIO;
import net.hyhend.spsam.Localization.RSSIHandler;
import net.hyhend.spsam.Localization.RssiAnalyzer;
import net.hyhend.spsam.Localization.RssiFingerPrint;
import net.hyhend.spsam.Utils.Constants;
import net.hyhend.spsam.cloud.CloudManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	 
	/**
	 * Main Activity context
	 */
	public static Context context;
	
	/**
	 * Fragments
	 */
	private static Fragment mainFragment;
	private static Fragment murkFragment;
	private static Fragment settingsFragment;
	private static Fragment fingerPrintFragment;
	private static Fragment locationSensingFragment;
	private static Fragment particleFragment;
	
	/**
	 * Motion Handler
	 */
	private MotionHandler motionHandler;

	/**
	 * Motion Handler
	 */
	private RSSIHandler rssiHandler;

	/**
	 * Motion Data Analyzer (owns Feature Vectors)
	 */
	private MotionDataAnalyzer motionAnalyzer;

	/**
	 * RealTime Motion Data Analyzer
	 */
	private RealTimeMotionDataAnalyzer realTimeMotionAnalyzer;

	/**
	 * RSSI Analyzer
	 */
	private RssiAnalyzer rssiAnalyzer;
	
	/**
	 * ParticleFilter
	 */
	private ParticleFilter particleFilter;
	
	/**
	 * Cloud Communication channel
	 */
	private CloudManager cloudManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
		setContentView(R.layout.activity_main);
		
		// Set up the action bar.
		ActionBar actionBar = initActionBar();

		// Set up viewPager
		initViewPager(actionBar);

		// Empty gauss file
		TrainingDataFileIO.destroyGaussDataFiles();
		
		// Create motion data analyzer (in new thread)
		new Thread(new Runnable() {
            @Override
            public void run() {
            	motionAnalyzer = new MotionDataAnalyzer();
            }
        }).start();
		
		// Create realtime motion data analyzer 
		realTimeMotionAnalyzer = new RealTimeMotionDataAnalyzer();
				
		// Create/init motion handler
        motionHandler = new MotionHandler(Constants.sample_rate);
    	
    	// Create rssi handler. Init with known fingerprints
    	ArrayList<RssiFingerPrint> fingerprints = TrainingDataFileIO.getFingerPrintDataFromFile();
    	rssiHandler = new RSSIHandler(fingerprints);

        // Create rssi analyzer
        rssiAnalyzer = new RssiAnalyzer();
        rssiAnalyzer.setFingerPrints(fingerprints);			// Set inital fingerprints. (from text file)
        
        // Cloudmanager
        cloudManager = new CloudManager();
        
        // Particle filter
        particleFilter = new ParticleFilter();
	}
	
	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// Do nothing
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return mainFragment;
			case 1:
				return locationSensingFragment;
			case 2:
				return particleFragment;
			case 3:
				return murkFragment;
			case 4:
				return fingerPrintFragment;
			case 5:
				return settingsFragment;

			}
			return null;
		}


		@Override
		public int getCount() {
			// Show 6 total pages.
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section_activity).toUpperCase(l);
			case 1:
				return getString(R.string.title_section_bayes).toUpperCase(l);
			case 2:
				return getString(R.string.title_section_particle).toUpperCase(l);
			case 3:
				return getString(R.string.title_section_activity_training).toUpperCase(l);
			case 4:
				return getString(R.string.title_section_bayes_training).toUpperCase(l);
			case 5:
				return getString(R.string.title_section_settings).toUpperCase(l);
		
			}
			return null;
		}
	}
    
    /**
     * Initializes viewPager with given actionBar
     * @param actionBar
     */
    private void initViewPager(final ActionBar actionBar) {
    	// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// Create Fragments
		mainFragment = new MainFragment();
		murkFragment = new MurkFragment();
		settingsFragment = new SettingsFragment();
		fingerPrintFragment = new FingerPrintFragment();
		locationSensingFragment = new LocationSensingFragment();
		particleFragment = new ParticleFragment();
		
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
    }
    
    /**
     * Set up and return actionBar
     * @return ActionBar
     */
    private android.app.ActionBar initActionBar() {
    	// Set up the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		return actionBar;
    }
    
	/**
	 * Returns Main Activity Context
	 * @return context
	 */
    public static Context getAppContext() {
        return MainActivity.context;
    }
	
	/**
	 * Returns mainFragment
	 * @return Fragment
	 */
    public static Fragment getMainFragment() {
        return mainFragment;
    }
	
	/**
	 * Returns murkFragment
	 * @return Fragment
	 */
    public static Fragment getMurkFragment() {
        return murkFragment;
    }

	/**
	 * Returns settingsFragment
	 * @return Fragment
	 */
    public static Fragment getSettingsFragment() {
        return settingsFragment;
    }
    
	/**
	 * Returns locationSensingFragment
	 * @return Fragment
	 */
    public static Fragment getLocationSensingFragment() {
        return locationSensingFragment;
    }
    
	/**
	 * Returns fingerPrintFragment
	 * @return Fragment
	 */
    public static Fragment getFingerPrintingFragment() {
        return fingerPrintFragment;
    }
    
	/**
	 * Returns particleFragment
	 * @return Fragment
	 */
    public static Fragment getParticleFragment() {
        return particleFragment;
    }
    
    /**
     * Returns the MotionHandler
     * @return MotionHandler
     */
    public MotionHandler getMotionHandler() {
    	return motionHandler;
    }
    
    /**
     * Returns the RssiAnalyzer
     * @return RssiAnalyzer
     */
    public RssiAnalyzer getRSSIAnalyzer() {
    	return rssiAnalyzer;
    }
    
    /**
     * Returns the RSSIHandler
     * @return RSSIHandler
     */
    public RSSIHandler getRSSIHandler() {
    	return rssiHandler;
    }
    
    /**
     * Returns the Cloud manager
     * @return CloudManager
     */
    public CloudManager getCloudManager()
    {
    	return cloudManager;
    }
    
    /**
     * Returns the MotionDataAnalyzer
     * @return MotionDataAnalyzer
     */
    public MotionDataAnalyzer getMotionDataAnalyzer() {
    	return motionAnalyzer;
    }
    
    /**
     * Returns the RealTimeMotionDataAnalyzer
     * @return RealTimeMotionDataAnalyzer
     */
    public RealTimeMotionDataAnalyzer getRealTimeMotionDataAnalyzer() {
    	return realTimeMotionAnalyzer;
    }
    
    /**
     * Returns the ParticleFilter
     * @return ParticleFilter
     */
    public ParticleFilter getParticleFilter() {
    	return particleFilter;
    }
    
    /**
     * Sets realtime in mergedMotionHistory
     * @param realTime
     */
    public void setRealTime(boolean realTime) {
    	
    }
}
