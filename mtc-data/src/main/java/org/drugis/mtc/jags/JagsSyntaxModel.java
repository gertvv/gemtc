package org.drugis.mtc.jags;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.drugis.common.CollectionHelper;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.parameterization.BasicParameter;
import org.drugis.mtc.parameterization.InconsistencyParameter;
import org.drugis.mtc.parameterization.InconsistencyParameterization;
import org.drugis.mtc.parameterization.NetworkModel;
import org.drugis.mtc.parameterization.NetworkParameter;
import org.drugis.mtc.parameterization.NetworkParameterComparator;
import org.drugis.mtc.parameterization.Parameterization;
import org.drugis.mtc.parameterization.PriorGenerator;
import org.drugis.mtc.parameterization.StartingValueGenerator;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import com.jgoodies.binding.list.ObservableList;

import edu.uci.ics.jung.graph.util.Pair;

public class JagsSyntaxModel {
	private static final Format s_format = new DecimalFormat("0.0##E0");
	private static final Transformer<NetworkParameter, String> s_idTrans = new Transformer<NetworkParameter, String>() {
		public String transform(NetworkParameter input) {
			return input.getName();
		}
	};
	private static final Transformer<NetworkParameter, String> s_rTransform = new Transformer<NetworkParameter, String>() {
		public String transform(NetworkParameter input) {
			return "x[, \"" + input.getName() + "\"]";
		}
	};
	
	private final Parameterization d_pmtz;
	private final boolean d_isJags;
	private final boolean d_inconsistency;
	private final Network d_network;
	private final PriorGenerator d_priorGen;

	public JagsSyntaxModel(Network network, Parameterization pmtz, boolean isJags) {
		d_network = network;
		d_pmtz = pmtz;
		d_isJags = isJags;
		d_inconsistency = pmtz instanceof InconsistencyParameterization;
		d_priorGen = new PriorGenerator(network);
	}

	/**
	 * Rewrite a number in scientific E-notation to the format appropriate for BUGS or JAGS.
	 */
	private String rewriteNumber(String s) { 
		return d_isJags ? s.replaceFirst("E", "*10^") : s;
	}
	
	private String generateDataFile(List<Pair<String>> assignments) {
		final String assign = d_isJags ? " <- " : " = ";
		final String sep = d_isJags ?  "\n" : ",\n";
		final String head = d_isJags ?  "" : "list(\n";
		final String foot = d_isJags ? "\n" : "\n)\n";

		Collection<String> lines = CollectionHelper.transform(assignments, new Transformer<Pair<String>, String>() {
			public String transform(Pair<String> input) {
				return input.getFirst() + assign + input.getSecond();
			}});

		return head + StringUtils.join(lines, sep) + foot;
	}

	public String initialValuesText(StartingValueGenerator generator) {
		List<Pair<String>> list = new ArrayList<Pair<String>>();
		list.addAll(initMetaParameters(generator));
		list.addAll(initBaselineEffects(generator));
		list.addAll(initRelativeEffects(generator));
		list.addAll(initVarianceParameters(generator));

		return generateDataFile(list);
	}


	public String analysisText(String prefix) {
		List<String> list = new ArrayList<String>();
		list.add("deriv <- list(");
		list.add(getDerivations());
		list.add("\t)");
		list.add("# source('mtc.R')");
		list.add("# data <- append.derived(read.mtc('" + prefix + "'), deriv)\n");
		
		return StringUtils.join(list, "\n");
	}

	/*
	private def initMetaParameters(g: StartingValueGenerator[M]): List[(String, String)] = {
		val basic = {
			for {basicParam <- model.basicParameters}
			yield g.getRelativeEffect(asBasic(basicParam))
		}
		
		model.parameterVector.map(param => init(param, g, basic))
	}

	private def init(p: NetworkModelParameter, g: StartingValueGenerator[M],
			bl: List[Double])
	: (String, String) = (p.toString, (p match {
		case b: BasicParameter => bl(model.basicParameters.findIndexOf(_ == b))
		case s: SplitParameter => bl(model.basicParameters.findIndexOf(_ == s))
		case i: InconsistencyParameter =>
			InconsistencyStartingValueGenerator(i, model, g, bl)
		case _ => throw new IllegalStateException("Unsupported parameter " + p)
	}).toString)

*/
	private List<Pair<String>> initMetaParameters(StartingValueGenerator generator) {
		List<Pair<String>> list = new ArrayList<Pair<String>>();
		for (NetworkParameter p : d_pmtz.getParameters()) {
			if (p instanceof BasicParameter) {
				final double relativeEffect = generator.getRelativeEffect((BasicParameter) p);
				list.add(new Pair<String>(p.getName(), String.valueOf(relativeEffect)));
			}
		}
		return list;
	}

	private List<Pair<String>> initBaselineEffects(StartingValueGenerator generator) {
		Double[] baselineEffects = new Double[d_network.getStudies().size()];
		for (int i = 0; i < d_network.getStudies().size(); ++i) {
			final Study study = d_network.getStudies().get(i);
			final Treatment baseline = d_pmtz.getStudyBaseline(study);
			baselineEffects[i] = generator.getTreatmentEffect(study, baseline);
		}
		return Collections.singletonList(new Pair<String>("mu", writeVector(baselineEffects, d_isJags)));
	}

	private List<Pair<String>> initRelativeEffects(final StartingValueGenerator generator) {
		Double[][] relativeEffects = getMatrix(new StudyTreatmentTransformer<Double>() {
			public Double transform(final Study s, final Treatment t) {
				final Treatment b = d_pmtz.getStudyBaseline(s);
				if (b.equals(t)) {
					return null;
				}
				return generator.getRelativeEffect(s, new BasicParameter(b, t));
			}});
		return Collections.singletonList(new Pair<String>("delta", writeMatrix(relativeEffects, d_isJags)));
	}
	
	private List<Pair<String>> initVarianceParameters(StartingValueGenerator generator) {
		List<Pair<String>> list = new ArrayList<Pair<String>>();
		list.add(new Pair<String>("sd.d", String.valueOf(generator.getStandardDeviation())));
		if (d_inconsistency) {
			list.add(new Pair<String>("sd.w", String.valueOf(generator.getStandardDeviation())));
		}
		return list;
	}

	
	private String getDerivations() {
		Collection<String> lines = NetworkModel.transformTreatmentPairs(d_network, new Transformer<Pair<Treatment>, String>() {
			public String transform(Pair<Treatment> input) {
				Treatment ti = input.getFirst();
				Treatment tj = input.getSecond();
				BasicParameter p = new BasicParameter(ti, tj);
				BasicParameter q = new BasicParameter(tj, ti);
				if (!d_pmtz.getParameters().contains(p) && !d_pmtz.getParameters().contains(q)) {
					String e = expressRelativeEffect(ti, tj, s_rTransform);
					return "\t`" + p + "` = function(x) { " + e + " }";
				}
				return null;
			}
		});
		CollectionUtils.filter(lines, PredicateUtils.notNullPredicate());
		return StringUtils.join(lines, ",\n");
	}
	
	public CompiledTemplate readTemplate(String path) {
		return TemplateCompiler.compileTemplate(getClass().getResourceAsStream(path));
	}

	public String modelText() {
		CompiledTemplate template = readTemplate("modelTemplate.txt");
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("dichotomous", d_network.getType().equals(DataType.RATE));
		map.put("inconsistency", d_inconsistency);
		map.put("relativeEffectMatrix", getRelativeEffectMatrix());
		double sd = d_priorGen.getVagueNormalSigma();
		map.put("priorPrecision", rewriteNumber(s_format.format(1 / (sd * sd))));
		map.put("stdDevUpperLimit", rewriteNumber(s_format.format(d_priorGen.getRandomEffectsSigma())));
		map.put("parameters", d_pmtz.getParameters());
		map.put("inconsClass", InconsistencyParameter.class);
		return String.valueOf(TemplateRuntime.execute(template, map));
	}

	private String expressRelativeEffect(Treatment t1, Treatment t2, Transformer<NetworkParameter, String> transform) {
		if (t1.equals(t2)) {
			return "0";
		}
		return writeExpression(d_pmtz.parameterize(t1, t2), transform);
	}

	private String getRelativeEffectMatrix() {
		List<String> lines = new ArrayList<String>();
		final ObservableList<Treatment> treatments = d_network.getTreatments();
		for (int i = 0; i < treatments.size(); ++i) {
			for (int j = 0; j < treatments.size(); ++j) {
				lines.add("\td[" + (i + 1) + "," + (j + 1) + "] <- " + expressRelativeEffect(treatments.get(i), treatments.get(j), s_idTrans));
			}
		}
		return StringUtils.join(lines, "\n");
	}
	

	public String scriptText(String prefix, int nchains, int tuning, int simulation) {
		CompiledTemplate template = d_isJags ? readTemplate("jagsScriptTemplate.txt") : readTemplate("bugsScriptTemplate.txt");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("prefix", prefix);
		map.put("nchains", nchains);
		List<Integer> chains = new ArrayList<Integer>();
		for (int i = 0; i < nchains; ++i) {
			chains.add(i + 1);
		}
		map.put("chains", chains);
		map.put("tuning", tuning);
		map.put("simulation", simulation);
		map.put("inconsistency", d_inconsistency);
		map.put("parameters", d_pmtz.getParameters());
		return String.valueOf(TemplateRuntime.execute(template, map));
	}
	
	public String dataText() {
		List<Pair<String>> list = new ArrayList<Pair<String>>();
		list.add(new Pair<String>("ns", writeNumber(d_network.getStudies().size(), d_isJags)));
		list.add(new Pair<String>("na", writeVector(getArmCounts(), d_isJags)));
		list.add(new Pair<String>("t", writeMatrix(getTreatmentMatrix(), d_isJags)));
		
		switch (d_network.getType()) {
		case RATE:
			list.add(new Pair<String>("r", writeMatrix(getResponderMatrix(), d_isJags)));
			list.add(new Pair<String>("n", writeMatrix(getSampleSizeMatrix(), d_isJags)));
			break;
		case CONTINUOUS:
			list.add(new Pair<String>("m", writeMatrix(getMeanMatrix(), d_isJags)));
			list.add(new Pair<String>("e", writeMatrix(getStdErrMatrix(), d_isJags)));
			break;
		default:
			throw new IllegalArgumentException("Don't know how to generate starting values for " + d_network.getType() + " data");					
		}
		
		return generateDataFile(list);
	}

	private Integer[] getArmCounts() {
		Integer count[] = new Integer[d_network.getStudies().size()];
		for (int i = 0; i < count.length; ++i) {
			count[i] = d_network.getStudies().get(i).getMeasurements().size();
		}
		return count;	
	}
	
	public int getMaxArmCount() {
		int max = 0;
		for (int i = 0; i < d_network.getStudies().size(); ++i) {
			max = Math.max(max, d_network.getStudies().get(i).getMeasurements().size());
		}
		return max;
	}
	
	public Integer[][] getTreatmentMatrix() {
		return getMatrix(new StudyTreatmentTransformer<Integer>() {
			public Integer transform(Study s, Treatment t) {
				return d_network.getTreatments().indexOf(t) + 1;
			}});
	}
	
	public Integer[][] getResponderMatrix() {
		return getMatrix(new StudyTreatmentTransformer<Integer>() {
			public Integer transform(Study s, Treatment t) {
				return NetworkModel.findMeasurement(s, t).getResponders();
			}});
	}

	public Double[][] getMeanMatrix() {
		return getMatrix(new StudyTreatmentTransformer<Double>() {
			public Double transform(Study s, Treatment t) {
				return NetworkModel.findMeasurement(s, t).getMean();
			}});
	}
	
	public Double[][] getStdErrMatrix() {
		return getMatrix(new StudyTreatmentTransformer<Double>() {
			public Double transform(Study s, Treatment t) {
				final Measurement m = NetworkModel.findMeasurement(s, t);
				return m.getStdDev() / Math.sqrt(m.getSampleSize());
			}});
	}
	
	public Integer[][] getSampleSizeMatrix() {
		return getMatrix(new StudyTreatmentTransformer<Integer>() {
			public Integer transform(Study s, Treatment t) {
				return NetworkModel.findMeasurement(s, t).getSampleSize();
			}});
	}
	
	private interface StudyTreatmentTransformer<O> {
		public O transform(Study s, Treatment t);
	};
	
	/**
	 * Fill a matrix with Doubles that correspond to each (study, arm) combination.
	 * @see getTreatments() for the order in which arms are listed. 
	 * @param transformer Takes a pair of (Study, Treatment) and gives the corresponsing number.
	 * @return The generated matrix, containing "null" for missing combinations.
	 */
	public Double[][] getMatrix(StudyTreatmentTransformer<Double> transformer) {
		Double[][] m = new Double[d_network.getStudies().size()][getMaxArmCount()];
		getMatrix(m, transformer);
		return m;
	}
	
	/**
	 * Fill a matrix with Integers that correspond to each (study, arm) combination.
	 * @see getTreatments() for the order in which arms are listed.
	 * @param transformer Takes a pair of (Study, Treatment) and gives the corresponsing number.
	 * @return The generated matrix, containing "null" for missing combinations.
	 */
	public Integer[][] getMatrix(StudyTreatmentTransformer<Integer> transformer) {
		Integer[][] m = new Integer[d_network.getStudies().size()][getMaxArmCount()];
		getMatrix(m, transformer);
		return m;
	}
	
	/**
	 * Fill a matrix with numbers that correspond to each (study, arm) combination.
	 * @see getTreatments() for the order in which arms are listed.
	 * @param <N> The type of number.
	 * @param m The array to write results in.
	 * @param transformer Takes a pair of (Study, Treatment) and gives the corresponding number.
	 */
	public <N extends Number> void getMatrix(N[][] m, StudyTreatmentTransformer<N> transformer) {
		final ObservableList<Study> studies = d_network.getStudies();
		for (int i = 0; i < studies.size(); ++i) {
			final List<Treatment> treatments = getTreatments(studies.get(i));
			for (int j = 0; j < treatments.size(); ++j) {
				m[i][j] = transformer.transform(studies.get(i), treatments.get(j));
			}
		}
	}
	
	/**
	 * Get the treatments of the study, giving the baseline first, then the rest according to the natural ordering.
	 */
	private List<Treatment> getTreatments(Study study) {
		List<Treatment> treatments = NetworkModel.getTreatments(study);
		Treatment baseline = d_pmtz.getStudyBaseline(study);
		treatments.remove(baseline);
		treatments.add(0, baseline);
		return treatments;
	}

	/**
	 * Convert a number to a String so that it can be read by S-Plus/R
	 * @param x the number to convert to a string.
	 * @param jags true to write for JAGS/R, false to write for BUGS.
	 */
	public static String writeNumber(Number x, boolean jags) {
		if (x == null) {
			return "NA";
		}
		String suffix = jags && isInteger(x) ? "L" : "";
		return String.valueOf(x) + suffix;
	}
	
	private static boolean isInteger(Number x) {
		return x instanceof Integer || x instanceof Long || x instanceof Short || x instanceof Byte;
	}
	
	/**
	 * Convert a matrix m -- where m(i)(j) is the number in the i-th row and j-th column -- to S-Plus/R format.
	 * @param jags true for column-major format (R/S-Plus/JAGS), false for row-major (BUGS).
	 */
	public static String writeMatrix(Number[][] m, boolean jags) {
		int rows = m.length;
		int cols = m[0].length;
		String cells[] = new String[rows * cols];
		for (int i = 0; i < cells.length; ++i) {
			cells[i] = writeNumber(jags ? m[i % rows][i / rows] : m[i / cols][i % cols], jags);
		}
		
		return "structure(" + (jags ? "" : ".Data = ") + "c(" + StringUtils.join(cells, ", ") + "), .Dim = c(" +
			writeNumber(rows, jags) + ", " + writeNumber(cols, jags) + "))";
	}
	
	/**
	 * Convert a vector v to S-Plus/R format.
	 * @param jags true for R/S-Plus/JAGS format, false for BUGS.
	 */
	public static String writeVector(Number[] v, boolean jags) {
		String[] cells = new String[v.length];
		for (int i = 0; i < cells.length; ++i) {
			cells[i] = writeNumber(v[i], jags);
		}
		
		return "c(" + StringUtils.join(cells, ", ") + ")";
	}
	
	/**
	 * Transform the given map from parameters to -1 or +1 to a sum expression.
	 */
	public static String writeExpression(Map<NetworkParameter, Integer> pmtz, Transformer<NetworkParameter, String> transform) {
		List<String> terms = new ArrayList<String>();
		final Set<NetworkParameter> keys = new TreeSet<NetworkParameter>(NetworkParameterComparator.INSTANCE);
		keys.addAll(pmtz.keySet());
		for (NetworkParameter key : keys) {
			terms.add((pmtz.get(key) == -1 ? "-" : "") + transform.transform(key));
		}
		return StringUtils.join(terms, " + ");
	}
}