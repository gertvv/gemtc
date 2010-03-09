import gov.lanl.yadas._
import org.drugis.mtc._

val xmlFile = "smoking.xml"
val xml = scala.xml.XML.loadFile(xmlFile)

val network = NetworkModel(Network.fromXML(xml))

def countArms(network: NetworkModel): Int =
	{
		for {study <- network.studyList} yield study.treatments.size
	}.reduceLeft((a, b) => a + b)

println(countArms(network))

def armDoubleArray(network: NetworkModel, x: Double): Array[Double] =
	Array.make(countArms(network), x)

def armIntArray(network: NetworkModel, x: Int): Array[Double] =
	Array.make(countArms(network), x)

def studyDoubleArray(network: NetworkModel, x: Double): Array[Double] =
	Array.make(network.studyList.size, x)

def successArray(network: NetworkModel): Array[Double] =
	network.data.map(m => m._2.responders.toDouble).toArray

def sampleSizeArray(network: NetworkModel): Array[Double] =
	network.data.map(m => m._2.sampleSize.toDouble).toArray

def mleArray(network: NetworkModel): Array[Double] = 
	network.data.map(m => logit(m._2.responders, m._2.sampleSize)).toArray

def logit(success: Int, sample: Int): Double =
	if (success > 0)
		logit(success.toDouble / sample.toDouble)
	else
		logit(0.5 / sample.toDouble)
		

def logit(p: Double): Double = Math.log(p / (1 - p))

def ilogit(q: Double): Double = {
	val exq = Math.exp(q)
	exq / (1 + exq)
}

val r = new ConstantArgument(successArray(network))
// sample-size n from data
val n = new ConstantArgument(sampleSizeArray(network))
val ni = sampleSizeArray(network).map(x => (x + 1).toInt)
// the log-odds theta, p = ilogit(theta), theta ~ N(mu_ijk, sigma)
val theta = new MCMCParameter(
	//mleArray(network),
	armDoubleArray(network, 0.0),
	armDoubleArray(network, 0.1), "./yadas-theta")
// across-study baseline 
val mu = new MCMCParameter(
	Array(0.0), Array(0.1), "./yadas-mu")
val sigma = new MCMCParameter(
	Array(0.25), Array(0.1), "./yadas-sigma")

println(successArray(network).toList)
println(sampleSizeArray(network).toList)

val params = List[MCMCParameter](theta, mu, sigma)

// r_i ~ Binom(p_i, n_i) ; p_i = ilogit(theta_i) ;
// theta_i = mu_s(i) + delta_s(i)b(i)t(i)
val databond = new BasicMCMCBond(
		Array[MCMCParameter](theta),
		Array[ArgumentMaker](
			r,
			n,
			new FunctionalArgument(
				countArms(network), // desired parameter length
				1, // number of parameters in bond
				Array.make(0, 0), // parameters that need to be expanded
				Array[Array[Int]](), // expanders
				new Function() {
					def f(args: Array[Double]): Double = {
						ilogit(args(0))
					}
				}
			)
		),
		new Binomial()
	)

val thetaprior = new BasicMCMCBond(
		Array[MCMCParameter](theta, mu, sigma),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new GroupArgument(1, Array.make(countArms(network), 0)),
			new GroupArgument(2, Array.make(countArms(network), 0))
		),
		new Gaussian()
	)

val sigmaprior = new BasicMCMCBond(
		Array[MCMCParameter](sigma),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new ConstantArgument(0.5),
			new ConstantArgument(0.5)
		),
		new Gamma()
	)

val bonds = List[MCMCBond](databond, thetaprior, sigmaprior)

val updates = List[MCMCUpdate](//new FiniteUpdate(r, ni),
	new UpdateTuner(theta), new UpdateTuner(mu), new UpdateTuner(sigma))

for (i <- 0 until 2000) {
	for (u <- updates) {
		u.update()
	}
	for (p <- params) {
		p.output()
	}
}

for (p <- params) {
	p.finish()
}

