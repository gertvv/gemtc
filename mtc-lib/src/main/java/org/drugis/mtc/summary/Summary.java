package org.drugis.mtc.summary;

import com.jgoodies.binding.beans.Observable;

public interface Summary extends Observable {
	public static final String PROPERTY_DEFINED = "defined";
	public abstract boolean getDefined();
}