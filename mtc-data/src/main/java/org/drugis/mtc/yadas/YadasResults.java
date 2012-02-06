package org.drugis.mtc.yadas;

import gov.lanl.yadas.MCMCParameter;

import java.util.List;

import org.drugis.mtc.MCMCResults;
import org.drugis.mtc.MCMCResultsListener;
import org.drugis.mtc.Parameter;

public class YadasResults implements MCMCResults {

	@Override
	public void addResultsListener(MCMCResultsListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int findParameter(Parameter p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfChains() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Parameter[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSample(int p, int c, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeResultsListener(MCMCResultsListener l) {
		// TODO Auto-generated method stub
		
	}

	public void setDirectParameters(List<Parameter> parameters) {
		// TODO Auto-generated method stub
		
	}

	public void setNumberOfChains(int nChains) {
		// TODO Auto-generated method stub
		
	}

	public void setNumberOfIterations(int simulationIter) {
		// TODO Auto-generated method stub
		
	}

	public void simulationFinished() {
		// TODO Auto-generated method stub
		
	}

	public ParameterWriter getParameterWriter(
			Parameter randomEffectVar, int chain, MCMCParameter basic,
			int i) {
		// TODO Auto-generated method stub
		return null;
	}

}
