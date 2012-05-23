package org.drugis.mtc.parameterization;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.drugis.mtc.Parameter;

public class ParameterComparator implements Comparator<Parameter> {
	@SuppressWarnings("unchecked")
	private static final List<Class<? extends Parameter>> classes = Arrays.asList(
		BasicParameter.class,
		SplitParameter.class,
		InconsistencyParameter.class,
		RandomEffectsVariance.class,
		InconsistencyVariance.class
		);
	
	@Override
	public int compare(Parameter o1, Parameter o2) {
		int par1Type = classes.indexOf(o1.getClass());
		int par2Type = classes.indexOf(o2.getClass());

		if (par1Type < 0 | par2Type < 0) {
			throw new IllegalArgumentException("Attempt to compare unknown Parameter class : " + o1.getClass());
		}
		
		if (par1Type != par2Type) {
			return par1Type - par2Type;
		}
		
		if (o1 instanceof BasicParameter) {
			return ((BasicParameter) o1).compareTo((BasicParameter) o2);
		} else if (o1 instanceof SplitParameter) {
			return ((SplitParameter) o1).compareTo((SplitParameter) o2);
		} else if (o1 instanceof InconsistencyParameter) {
			return ((InconsistencyParameter) o1).compareTo((InconsistencyParameter) o2);
		}
		
		return 0;
	}

}
