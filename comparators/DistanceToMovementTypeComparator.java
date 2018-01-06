package net.hyhend.spsam.comparators;

import java.util.Comparator;

import net.hyhend.spsam.Utils.MovementType;
import net.hyhend.spsam.Utils.Tuple;

public class DistanceToMovementTypeComparator implements Comparator<Tuple<Double,MovementType>> {

	

	@Override
	public int compare(Tuple<Double,MovementType> lhs, Tuple<Double,MovementType> rhs) {
		

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

