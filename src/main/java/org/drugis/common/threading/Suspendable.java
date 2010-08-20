package org.drugis.common.threading;

public interface Suspendable extends Runnable{

	public boolean isThreadSuspended();
	public void suspend();
	public void wakeUp();
	
}