package org.drugis.mtc.gui;

import java.util.Comparator;

public class StudyIdComparator implements Comparator<StudyModel> {
	public int compare(StudyModel o1, StudyModel o2) {
		return o1.getId().compareTo(o2.getId());
	}
}
