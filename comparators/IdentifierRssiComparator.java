package net.hyhend.spsam.comparators;

import java.util.Comparator;

import net.hyhend.spsam.Utils.Tuple;

public class IdentifierRssiComparator implements Comparator<Tuple<String,Integer>> {
	@Override
	public int compare(Tuple<String,Integer> lhs, Tuple<String,Integer> rhs) {
		

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

