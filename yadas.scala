import gov.lanl.yadas._
import org.drugis.mtc._
import org.drugis.mtc.yadas._

val xmlFile = "smoking.xml"
val xml = scala.xml.XML.loadFile(xmlFile)


val network = Network.fromXML(xml)
val model = (new YadasModelFactory()).getConsistencyModel(network)
model.run()

for (t <- network.treatments) {
	for (i <- 1 to network.treatments.size) {
		println(t + " " + i + " " + model.rankProbability(t, i))
	}
}
