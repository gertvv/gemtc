import gov.lanl.yadas._
import org.drugis.mtc._
import org.drugis.mtc.yadas._

val xmlFile = "smoking.xml"
val xml = scala.xml.XML.loadFile(xmlFile)

val network = NetworkModel(Network.fromXML(xml))

def successArray(network: NetworkModel): Array[Double] =
	network.data.map(m => m._2.responders.toDouble).toArray

def sampleSizeArray(network: NetworkModel): Array[Double] =
	network.data.map(m => m._2.sampleSize.toDouble).toArray

// success-rate r from data
val r = new ConstantArgument(successArray(network))
// sample-size n from data
val n = new ConstantArgument(sampleSizeArray(network))
// study baselines
val mu = new MCMCParameter(
	Array.make(network.studyList.size, 0.0),
	Array.make(network.studyList.size, 0.1), "./yadas-mu")
// random effects
val delta = new MCMCParameter(
	Array.make(network.relativeEffects.size, 0.0),
	Array.make(network.relativeEffects.size, 0.1), "./yadas-delta")
// basic parameters
val basic = new MCMCParameter(
	Array.make(network.basicParameters.size, 0.0),
	Array.make(network.basicParameters.size, 0.1), "./yadas-basic")
// inconsistency parameters
val incons = new MCMCParameter(
	Array.make(network.inconsistencyParameters.size, 0.0),
	Array.make(network.inconsistencyParameters.size, 0.1), "./yadas-incons")
// variance
val sigma = new MCMCParameter(
	Array(0.25), Array(0.1), "./yadas-sigma")
// inconsistency variance
val sigmaw = new MCMCParameter(
	Array(0.25), Array(0.1), "./yadas-sigmaw")

val params = List[MCMCParameter](mu, delta, basic, incons, sigma, sigmaw)

// r_i ~ Binom(p_i, n_i) ; p_i = ilogit(theta_i) ;
// theta_i = mu_s(i) + delta_s(i)b(i)t(i)
val databond = new BasicMCMCBond(
		Array[MCMCParameter](mu, delta),
		Array[ArgumentMaker](
			r,
			n,
			new SuccessProbabilityArgumentMaker(network, 0, 1)
		),
		new Binomial()
	)

// random effects bound to basic/incons parameters
val randomeffectbond =  new BasicMCMCBond(
		Array[MCMCParameter](delta, basic, incons, sigma),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new RelativeEffectArgumentMaker(network, 1, None),
			new GroupArgument(3, Array.make(network.relativeEffects.size, 0))
		),
		new Binomial()
	)

val muprior = new BasicMCMCBond(
		Array[MCMCParameter](mu),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new ConstantArgument(0, network.studyList.size),
			new ConstantArgument(Math.sqrt(1000), network.studyList.size),
		),
		new Gaussian()
	)

val basicprior = new BasicMCMCBond(
		Array[MCMCParameter](basic),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new ConstantArgument(0, network.basicParameters.size),
			new ConstantArgument(Math.sqrt(1000), network.basicParameters.size),
		),
		new Gaussian()
	)

val inconsprior = new BasicMCMCBond(
		Array[MCMCParameter](incons, sigmaw),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new ConstantArgument(0, network.inconsistencyParameters.size),
			new GroupArgument(1, Array.make(network.inconsistencyParameters.size, 0))
		),
		new Gaussian()
	)

val sigmaprior = new BasicMCMCBond(
		Array[MCMCParameter](sigma),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new ConstantArgument(0.00001),
			new ConstantArgument(2)
		),
		new Uniform()
	)

val sigmawprior = new BasicMCMCBond(
		Array[MCMCParameter](sigmaw),
		Array[ArgumentMaker](
			new IdentityArgument(0),
			new ConstantArgument(0.00001),
			new ConstantArgument(2)
		),
		new Uniform()
	)

def tuner(param: MCMCParameter): MCMCUpdate =
	new UpdateTuner(param, 40, 50, 1, Math.exp(-1))

val updates = List(
	basic, incons, tuner(delta), tuner(mu), tuner(sigma), tuner(sigmaw))

for (i <- 0 until 10000) {
	if (i % 1000 == 0) println(i)
	for (u <- updates) {
		u.update()
	}
	for (p <- params) {
		p.output()
	}
}

for (update <- updates) {
	println("Update " + update + ": " + update.accepted())
}

for (p <- params) {
	p.finish()
}

println(network.basicParameters)
println(network.inconsistencyParameters)
