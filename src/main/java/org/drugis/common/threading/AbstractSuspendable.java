package org.drugis.common.threading;


public abstract class AbstractSuspendable implements Suspendable  {

	boolean d_threadSuspended = false;

	
	public synchronized boolean isThreadSuspended() {
		return d_threadSuspended;
	}
	
	public synchronized void suspend() {
		d_threadSuspended = true;
	}
	
	public synchronized void wakeUp() {
		d_threadSuspended = false;
		notify();
	}

	protected void waitIfSuspended() {
		while(isThreadSuspended()) {
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
