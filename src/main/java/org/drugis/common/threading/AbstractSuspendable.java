package org.drugis.common.threading;


public abstract class AbstractSuspendable implements Suspendable  {

	boolean d_threadSuspended = false;
	boolean d_threadTerminated = false;
	
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
	
	public void terminate() {
		d_threadTerminated = true;
		wakeUp();
	}

	protected boolean isTerminated() {
		return d_threadTerminated;
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
