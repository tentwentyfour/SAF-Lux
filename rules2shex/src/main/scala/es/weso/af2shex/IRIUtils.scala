package es.weso.af2shex
import es.weso.rdf.nodes._
import com.github.vickumar1981.stringdistance.StringDistance._
import com.github.vickumar1981.stringdistance.StringSound._
import org.apache.jena.rdf.model.ResourceFactory

object IRIUtils {

  // Available algorithms at: https://github.com/vickumar1981/stringdistance/
  val SIMILARITY_ALGORITHM = Levenshtein.score _ 
  val SIMILARITY_THRESHOLD = 0.85

  def compareSimilarity(name1: String, name2: String): Double = {
    val d = SIMILARITY_ALGORITHM(name1,name2)
    // println(s"Comparing: $name1 with $name2: value = $d")
    d
  }

  def localName(iri: IRI): String = {
    ResourceFactory.createResource(iri.str).getLocalName()
  }

  def namespace(iri: IRI): IRI = {
    IRI(ResourceFactory.createResource(iri.str).getNameSpace())
  }

  def getQName(iri: IRI): (IRI, String) =
    (namespace(iri), localName(iri))


  def cleanString(str: String): String = {
    val specialCharsToIgnore = "[_ ]"
    str.toLowerCase().replaceAll(specialCharsToIgnore,"")
  }

  def similarity(name:String, iri: IRI): Double = {
   compareSimilarity(cleanString(localName(iri)), cleanString(name))
  }

  def isSimilar(name: String, iri: IRI): Boolean = {
    similarity(name,iri) >= SIMILARITY_THRESHOLD
  }

  def compareCIDOC(name: String, iri: IRI):Boolean = {
    val cidoc = "([E|P|R][0-9]{1,3})(i?)[ _](.*)".r
    (name, localName(iri)) match {
      case (cidoc(ref1,i1,rest1), cidoc(ref2,i2,rest2)) 
           if (ref1 == ref2 && 
           compareSimilarity(cleanString(rest1),cleanString(rest2)) > SIMILARITY_THRESHOLD) => true
      // Special cases
      case ("is a subclass of", "subClassOf") => true
      case ("P127 has broader type", "P127_has_broader_term") => true
      case ("P127 has narrower type of source", "P127i_has_narrower_term") => true
      case (other1,other2) => { 
        // println(s"${other1}/${other2} don't match any case")
        false
      }
    }
  }
}
    
