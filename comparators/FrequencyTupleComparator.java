package net.hyhend.spsam.comparators;

import java.util.Comparator;

import net.hyhend.spsam.Utils.Tuple;

public class FrequencyTupleComparator implements Comparator<Tuple<Integer,Double>> {


	@Override
	public int compare(Tuple<Integer,Double> lhs, Tuple<Integer,Double> rhs) {
		

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
