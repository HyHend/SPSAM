package net.hyhend.spsam.comparators;

import java.util.Comparator;

import net.hyhend.spsam.Localization.RssiFingerPrint;

public class FingerPrintComparator implements Comparator<RssiFingerPrint> {

	

	@Override
	public int compare(RssiFingerPrint lhs, RssiFingerPrint rhs) {
		

		if (lhs.getRssi() < rhs.getRssi())
		{
			return 1;
		}
		else if (lhs.getRssi() > rhs.getRssi())
		{
			return -1;
		}
		return 0;
	}
}

