package net.hyhend.spsam.comparators;

import java.util.Comparator;

import net.hyhend.spsam.Localization.Location;
import net.hyhend.spsam.Utils.Tuple;

public class LocationProbabilityComparator implements Comparator<Tuple<Location,Double>>  {

	@Override
	public int compare(Tuple<Location,Double> lhs, Tuple<Location,Double> rhs) {
		

		if (lhs.value < rhs.value)
		{
			return -1;
		}
		else if (lhs.value > rhs.value)
		{
			return 1;
		}
		return 0;
	}
}

