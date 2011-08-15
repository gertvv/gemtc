package org.drugis.mtc.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drugis.mtc.ContinuousMeasurement;
import org.drugis.mtc.DichotomousMeasurement;
import org.drugis.mtc.Network;
import org.drugis.mtc.NoneMeasurement;
import org.drugis.mtc.Study;
import org.drugis.mtc.Treatment;

import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.ObservableList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;

/**
 * Editable model for an MTC data set.
 */
public class DataSetModel {
	private ValueModel d_description = new ValueHolder("???");
	private ValueModel d_type = new ValueHolder(MeasurementType.DICHOTOMOUS);
	private ObservableList<TreatmentModel> d_treatments = new ArrayListModel<TreatmentModel>();
	private ObservableList<StudyModel> d_studies = new ArrayListModel<StudyModel>();
	
	public ValueModel getDescription() {
		return d_description;
	}

	public ValueModel getMeasurementType() {
		return d_type;
	}
	
	public ObservableList<TreatmentModel> getTreatments() {
		return d_treatments;
	}
	
	public ObservableList<StudyModel> getStudies() {
		return d_studies;
	}
	
	public Network<?> build() {
		List<Treatment> ts = new ArrayList<Treatment>();
		for (TreatmentModel tm : d_treatments) {
			ts.add(tm.build());
		}
		switch ((MeasurementType)d_type.getValue()) {
		case NONE:
			return buildNone(ts);
		case CONTINUOUS:
			return buildContinuous(ts);
		case DICHOTOMOUS:
			return buildDichotomous(ts);
		}
		throw new IllegalStateException();
	}

	private Network<?> buildDichotomous(List<Treatment> ts) {
		List<Study<DichotomousMeasurement>> ss = new ArrayList<Study<DichotomousMeasurement>>();
		for (StudyModel sm : d_studies) {
			ss.add(sm.buildDichotomous(ts));
		}
		return new Network<DichotomousMeasurement>(ScalaUtil.toScalaSet(ts), ScalaUtil.toScalaSet(ss));
	}

	private Network<?> buildContinuous(List<Treatment> ts) {
		List<Study<ContinuousMeasurement>> ss = new ArrayList<Study<ContinuousMeasurement>>();
		for (StudyModel sm : d_studies) {
			ss.add(sm.buildContinuous(ts));
		}
		return new Network<ContinuousMeasurement>(ScalaUtil.toScalaSet(ts), ScalaUtil.toScalaSet(ss));
	}

	private Network<?> buildNone(List<Treatment> ts) {
		List<Study<NoneMeasurement>> ss = new ArrayList<Study<NoneMeasurement>>();
		for (StudyModel sm : d_studies) {
			ss.add(sm.buildNone(ts));
		}
		return new Network<NoneMeasurement>(ScalaUtil.toScalaSet(ts), ScalaUtil.toScalaSet(ss));
	}
	
	@SuppressWarnings("unchecked")
	public static DataSetModel build(Network<?> network) {
		DataSetModel model = new DataSetModel();
		
		// populate treatments
		Map<Treatment, TreatmentModel> tMap = new HashMap<Treatment, TreatmentModel>();
		for (Treatment t : ScalaUtil.toJavaSet(network.treatments())) {
			TreatmentModel tm = new TreatmentModel();
			tm.setId(t.id());
			tm.setDescription(t.description());
			tMap.put(t, tm);
		}
		model.getTreatments().addAll(tMap.values());
		Collections.sort(model.getTreatments(), new TreatmentIdComparator());
		
		convertStudies(network, model, tMap);
		if (network.measurementType().equals(NoneMeasurement.class)) {
			model.getMeasurementType().setValue(MeasurementType.NONE);
		} else if (network.measurementType().equals(ContinuousMeasurement.class)) {
			model.getMeasurementType().setValue(MeasurementType.CONTINUOUS);
			convertContinuousMeasurements((Network<ContinuousMeasurement>)network, model);
		} else if (network.measurementType().equals(DichotomousMeasurement.class)) {
			model.getMeasurementType().setValue(MeasurementType.DICHOTOMOUS);
			convertDichotomousMeasurements((Network<DichotomousMeasurement>)network, model);
		}
		
		return model;
	}

	private static void convertDichotomousMeasurements(
			Network<DichotomousMeasurement> network, DataSetModel model) {
		for (StudyModel sm : model.getStudies()) {
			Study<DichotomousMeasurement> study = network.study(sm.getId());
			for (TreatmentModel tm : sm.getTreatments()) {
				DichotomousMeasurement m = study.measurements().apply(network.treatment(tm.getId()));
				sm.setResponders(tm, m.responders());
				sm.setSampleSize(tm, m.sampleSize());
			}
		}
	}

	private static void convertContinuousMeasurements(
			Network<ContinuousMeasurement> network, DataSetModel model) {
		for (StudyModel sm : model.getStudies()) {
			Study<ContinuousMeasurement> study = network.study(sm.getId());
			for (TreatmentModel tm : sm.getTreatments()) {
				ContinuousMeasurement m = study.measurements().apply(network.treatment(tm.getId()));
				sm.setMean(tm, m.mean());
				sm.setStdDev(tm, m.stdDev());
				sm.setSampleSize(tm, m.sampleSize());
			}
		}
	}

	private static void convertStudies(Network<?> network,
			DataSetModel model, Map<Treatment, TreatmentModel> tMap) {
		for (Study<?> s : ScalaUtil.toJavaSet(network.studies())) {
			StudyModel sm = new StudyModel();
			sm.setId(s.id());
			List<TreatmentModel> ts = new ArrayList<TreatmentModel>();
			for (Treatment t : ScalaUtil.toJavaSet(s.treatments())) {
				ts.add(tMap.get(t));
			}
			Collections.sort(ts, new TreatmentIdComparator());
			sm.getTreatments().addAll(ts);
			model.getStudies().add(sm);
		}
		Collections.sort(model.getStudies(), new StudyIdComparator());
	}
}
