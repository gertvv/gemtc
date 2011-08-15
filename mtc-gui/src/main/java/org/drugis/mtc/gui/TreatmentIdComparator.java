package org.drugis.mtc.gui;

import java.util.Comparator;

public class TreatmentIdComparator implements Comparator<TreatmentModel> {
	public int compare(TreatmentModel arg0, TreatmentModel arg1) {
		return arg0.getId().compareTo(arg1.getId());
	}
}
