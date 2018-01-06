package net.hyhend.spsam.comparators;

import java.util.Comparator;

import net.hyhend.spsam.Localization.Location;
import net.hyhend.spsam.Utils.Tuple;

public class DistanceToLocationComparator implements Comparator<Tuple<Double,Location>> {

	

	@Override
	public int compare(Tuple<Double,Location> lhs, Tuple<Double,Location> rhs) {
		

		if (lhs.key < rhs.key)
		{
			return -1;
		}
		else if (lhs.key > rhs.key)
		{
			return 1;
		}
		return 0;
	}
}

