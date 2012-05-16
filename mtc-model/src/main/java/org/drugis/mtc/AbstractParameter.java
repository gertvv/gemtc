package org.drugis.mtc;

public abstract class AbstractParameter implements Parameter {

	@Override
	public int compareTo(Parameter o) {
		return getName().compareTo(o.getName());
	}

	
	abstract public String getName();

}
