package es.weso.af2shex
import es.weso.rdf.nodes._
import es.weso.rdf.PREFIXES._
import cats._ 
import cats.implicits._
import cats.effect._
import es.weso.rdf.jena.RDFAsJenaModel
import java.io.File
import fs2._
import java.io.Reader
import es.weso.rdf.PrefixMap
import IRIUtils._

case class RDFSModel(name: String, classes: Set[IRI], properties: Set[IRI], prefixMap: PrefixMap = PrefixMap.empty) {
  def merge(other: RDFSModel): RDFSModel = 
   this.copy(name = s"$name|${other.name}", classes = classes ++ other.classes, properties = properties ++ other.properties)

  def iris: Set[IRI] = classes ++ properties

  def addPrefix(name: String, iri: IRI): RDFSModel = this.copy(prefixMap = this.prefixMap.addPrefix(name,iri))

  private def containsName(name:String, iri: IRI): Boolean = 
    localName(iri).containsSlice(name)

  def find(name: String): Option[IRI] = {
   iris.filter(node => containsName(name,node)).headOption
  }
    
  def findSimilar(name: String): Set[(IRI,Double)] = {
    iris.filter(isSimilar(name,_)).map(n => (n, similarity(name,n)))
  }

  def findSimilarCIDOCStyle(name: String): Set[(IRI,Double)] = {
    iris.filter(compareCIDOC(name,_)).map(n => (n, similarity(name,n)))
  }

  def addClass(iri: IRI): RDFSModel =
   this.copy(classes = classes + iri)

}

object RDFSModel {

  implicit val showFoo: Show[RDFSModel] = Show.show(model => model.name)


  // TODO: Move this one to srdf PREFIXES
  lazy val `rdfs:Class` = rdfs + "Class"
  lazy val `rdf:Property` = rdf + "Property"

  // Namespaces
  lazy val crm = IRI("http://www.cidoc-crm.org/cidoc-crm/")
  lazy val rdfs = IRI("http://www.w3.org/2000/01/rdf-schema#")
  lazy val frbroo = IRI("http://iflastandards.info/ns/fr/frbr/frbroo/")

  def fromReader(name: String, reader: Reader, format: String = "TURTLE"): IO[RDFSModel] = 
    RDFAsJenaModel.fromReader(reader, format).flatMap(_.use(rdf => for {
     classes <- rdf.triplesWithPredicateObject(`rdf:type`, `rdfs:Class`).map(_.subj).collect { case iri: IRI => iri }.compile.toList.map(_.toSet)
     properties <- rdf.triplesWithPredicateObject(`rdf:type`, `rdf:Property`).map(_.subj).collect {case iri: IRI => iri }.compile.toList.map(_.toSet)
     prefixMap <- rdf.getPrefixMap
    } yield RDFSModel(name,classes,properties, prefixMap = prefixMap)
  ))


//  def parseModels: IO[Model] = parseFromRDFS("src/main/resources/rdfs/cidoc_crm_v6.2.1-2018April.rdfs.xml")

}