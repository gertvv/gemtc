package org.drugis.mtc

import org.drugis.mtc.{DichotomousMeasurement => M}

trait NetworkModelParameter {

}

final class BasicParameter(val base: Treatment, val subject: Treatment)
extends NetworkModelParameter {
	override def toString() = "d." + base.id + "." + subject.id

	override def equals(other: Any): Boolean = other match {
		case p: BasicParameter => (p.base == base && p.subject == subject)
		case _ => false
	}

	override def hashCode: Int = 31 * base.hashCode + subject.hashCode
}

final class InconsistencyParameter(val cycle: List[Treatment])
extends NetworkModelParameter {
	private val cycleStr =
		cycle.reverse.tail.reverse.map(t => t.id).mkString(".")

	override def toString() = "w." + cycleStr

	override def equals(other: Any): Boolean = other match {
		case p: InconsistencyParameter => (p.cycle == cycle)
		case _ => false
	}

	override def hashCode: Int = cycle.hashCode

	def treatmentList: java.util.List[Treatment] = {
		val list = new java.util.ArrayList[Treatment]()
		for (t <- cycle) {
			list.add(t)
		}
		list
	}
}

/**
 * Class representing a Bayes model for a treatment network
 */
class NetworkModel(
	val network: Network, 
	val basis: FundamentalGraphBasis[Treatment],
	val studyBaseline: Map[Study[M], Treatment],
	val treatmentList: List[Treatment],
	val studyList: List[Study[M]]) {

	require(Set[Study[M]]() ++ studyList == network.studies)
	require(Set[Treatment]() ++ treatmentList == network.treatments)
	require(studyBaseline.keySet == network.studies)
	require(basis.tree.vertexSet == network.treatments)

	val studyMap = NetworkModel.indexMap(studyList)
	val treatmentMap = NetworkModel.indexMap(treatmentList)

	val data = studyList.flatMap(
		study => NetworkModel.treatmentList(study.treatments).map(
				t => (study, study.measurements(t))))

	private val basicParameterEdges = sort(basis.treeEdges)
	private val inconsistencyParameterEdges = sort(inconsistencyEdges)

	private val parameterMap = 
		Map[(Treatment, Treatment), NetworkModelParameter]() ++
		basicParameterEdges.map(e => (e, new BasicParameter(e._1, e._2))) ++
		inconsistencyParameterEdges.map(e => (e, new InconsistencyParameter(
			basis.tree.cycle(e._1, e._2))))

	private val parameterEdges =
		basicParameterEdges ++ inconsistencyParameterEdges

	/**
	 * Gives a list of edges (in the tree) that result directly in parameters
	 * in an ordered manner.
	 */
	val parameterVector: List[NetworkModelParameter] = 
		parameterEdges.map(e => parameterMap(e))

	/**
	 * Gives the list of Inconsistency parameters
	 */
	val inconsistencyParameters: List[InconsistencyParameter] =
		for {param <- parameterVector;
			if (param.isInstanceOf[InconsistencyParameter])
		} yield param.asInstanceOf[InconsistencyParameter];

	/**
	 * Gives the list of Basic parameters
	 */
	val basicParameters: List[BasicParameter] =
		for {param <- parameterVector;
			if (param.isInstanceOf[BasicParameter])
		} yield param.asInstanceOf[BasicParameter];

	/**
	 * List of relative effects in order
	 */
	val relativeEffects: List[(Treatment, Treatment)] =
		studyList.flatMap(study => studyRelativeEffects(study))

	val relativeEffectIndex: Map[Study[M], Int] =
		reIndexMap(studyList, 0)

	private def reIndexMap(l: List[Study[M]], i: Int)
	: Map[Study[M], Int] = l match {
		case Nil => Map[Study[M], Int]()
		case s :: l0 =>
			reIndexMap(l0, i + s.treatments.size - 1) + ((s, i))
	}

	def studyRelativeEffects(study: Study[M])
	: List[(Treatment, Treatment)] =
		for {t <- treatmentList; if (study.treatments.contains(t)
				&& !(studyBaseline(study) == t))
			} yield (studyBaseline(study), t)

	def parameterization(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = param(a, b)

	private def param(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		if (basicParameterEdges contains (a, b)) basicParam(a, b)
		else if (basicParameterEdges contains (b, a)) negate(basicParam(b, a))
		else functionalParam(a, b)
	}

	private def functionalParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val r = basis.tree.commonAncestor(a, b)
		add(add(negate(pathParam(basis.tree.path(r, a))),
			pathParam(basis.tree.path(r, b))),
			inconsParam(a, b))
	}

	private def inconsParam(a: Treatment, b: Treatment)
	: Map[NetworkModelParameter, Int] = {
		val edges = inconsistencyParameterEdges
		if (edges contains (a, b)) basicParam(a, b)
		else if (edges contains (b, a)) negate(basicParam(b, a))
		else emptyParam()
	}

	private def pathParam(path: List[Treatment])
	: Map[NetworkModelParameter, Int] = {
		if (path.size < 2) emptyParam()
		else add(param(path(0), path(1)), pathParam(path.tail))
	}

	private def basicParam(a: Treatment, b: Treatment) =
		Map[NetworkModelParameter, Int]((parameterMap((a, b)), 1))

	private def negate(p: Map[NetworkModelParameter, Int]) =
		p.transform((a, b) => -b)

	private def add(p: Map[NetworkModelParameter, Int],
			q: Map[NetworkModelParameter, Int])
	: Map[NetworkModelParameter, Int] = {
		emptyParam() ++
		(for {x <- (p.keySet ++ q.keySet)
		} yield (x, getOrZero(p, x) + getOrZero(q, x)))
	}

	private def emptyParam() = Map[NetworkModelParameter, Int]()

	private def getOrZero(p: Map[NetworkModelParameter, Int],
			x: NetworkModelParameter): Int =
	p.get(x) match {
		case None => 0
		case Some(d) => d
	}

	private def inconsistencyEdges: Set[(Treatment, Treatment)] = 
		basis.backEdges.filter(
			e => network.isInconsistency(basis.tree.createCycle(e)))

	private def sort(edges: Set[(Treatment, Treatment)])
	: List[(Treatment, Treatment)] = {
		edges.toList.sort((a, b) => if (a._1 == b._1) a._2 < b._2 else a._1 < b._1)
	}
}

class BaselineSearchState(
	val toCover: Set[(Treatment, Treatment)],
	val studies: List[Study[M]],
	val assignment: Map[Study[M], Treatment]) { }

class BaselineSearchProblem(
	toCover: Set[(Treatment, Treatment)],
	studies: Set[Study[M]]) extends SearchProblem[BaselineSearchState] {

	val initialState = new BaselineSearchState(toCover, studies.toList,
		Map[Study[M], Treatment]())

	def isGoal(s: BaselineSearchState): Boolean = {
		s.toCover.isEmpty && s.studies.isEmpty
	}

	def successors(s: BaselineSearchState): List[BaselineSearchState] = {
		if (s.studies.isEmpty) Nil
		else {
			val study = s.studies.head
			(for {t <- study.treatments.toList.sort((a, b) => a < b)
				val toCover = s.toCover -- (
					study.treatmentGraph.edgeSet.filter(e => (e._1 == t || e._2 == t)))
				val assignment = s.assignment + ((study, t))
			} yield new BaselineSearchState(toCover, s.studies.tail, assignment)
			).toList
		}
	}
}

object NetworkModel {
	def apply(network: Network, tree: Tree[Treatment]): NetworkModel = {
		new NetworkModel(network,
			new FundamentalGraphBasis(network.treatmentGraph, tree),
			assignBaselines(network, tree),
			treatmentList(network.treatments),
			studyList(network.studies))
	}

	def apply(network: Network, base: Treatment): NetworkModel = {
		apply(network, network.bestSpanningTree(base))
	}

	def apply(network: Network): NetworkModel = {
		apply(network, treatmentList(network.treatments).first)
	}

	private def assignMultiArm(
		toCover: Set[(Treatment, Treatment)], studies: Set[Study[M]])
	: Map[Study[M], Treatment] = {
		val problem = new BaselineSearchProblem(toCover, studies)
		val alg = new DFS()
		alg.search(problem) match {
			case None => throw new Exception("No Assignment Found!")
			case Some(x) => x.assignment
		}
	}

	def assignBaselines(network: Network, st: Tree[Treatment])
	: Map[Study[M], Treatment] = {
		val toCover = network.inconsistencies(st).flatMap(a => a.edgeSet)
		val twoArm = network.studies.filter(study => study.treatments.size == 2)
		val multiArm = network.studies -- twoArm
		val covered = twoArm.flatMap(study => study.treatmentGraph.edgeSet)

		val twoArmMap = Map[Study[M], Treatment]() ++ twoArm.map(study => (study, study.treatments.toList.sort((a, b) => a < b).head))

		val leftToCover = toCover -- covered
		twoArmMap ++ assignMultiArm(leftToCover, multiArm)
	}

	def studyList(studies: Set[Study[M]]) = {
		studies.toList.sort((a, b) => a.id < b.id)
	}

	def treatmentList(treatments: Set[Treatment]) = {
		treatments.toList.sort((a, b) => a < b)
	}

	def indexMap[T](l: List[T]): Map[T, Int] =
		Map[T, Int]() ++ l.map(a => (a, l.indexOf(a) + 1))
}
