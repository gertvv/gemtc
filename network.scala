import org.drugis._

val xml = <network>
	<treatments>
		<treatment id="A"/>
		<treatment id="B"/>
		<treatment id="C"/>
		<treatment id="D"/>
	</treatments>
	<studies>
		<study id="1">
			<measurement treatment="D"/>
			<measurement treatment="B"/>
			<measurement treatment="C"/>
		</study>
		<study id="2">
			<measurement treatment="A"/>
			<measurement treatment="B"/>
		</study>
		<study id="3">
			<measurement treatment="A"/>
			<measurement treatment="C"/>
		</study>
		<study id="4">
			<measurement treatment="A"/>
			<measurement treatment="D"/>
		</study>
	</studies>
</network>

val network = Network.fromXML(xml)

val top = new Treatment("A")

for (st <- SpanningTreeEnumerator.treeEnumerator(network.treatmentGraph, top)) {
	println(st + " has ICDF = " + network.countInconsistencies(st))
}

val best = network.bestSpanningTree(top)
println("BEST: " + best + " has ICDF = " + network.countInconsistencies(best))
