package es.weso.af2shex

import cats._
import cats.implicits._
import scala.util.control.NoStackTrace
import es.weso.rdf.nodes.IRI
import IRIUtils._

sealed  abstract class AF2ShExError protected (val msg: String) extends Exception(msg) with NoStackTrace with Product with Serializable 

case class MsgError(override val msg: String) extends AF2ShExError(msg)
case class UnsupportedCellType(cell: String, location: Location) extends 
  AF2ShExError(s"""|Unsupported cell: ${cell}
                   |Location: ${location.show}""".stripMargin)
case class NoMatch(name: String, location: Location, model: RDFSModel) extends 
  AF2ShExError(s"""|No match for string: ${name.show}
                   |Location: ${location.show}
                   |Model: ${model.show}""".stripMargin)

case class MultipleMatches(name: String, nodes: List[(IRI,Double)], location: Location, model: RDFSModel) extends 
  AF2ShExError(s"""|Multiples matches for string: ${name.show}
                   |Location: ${location.show}
                   |Matches: ${nodes.map{ case (iri,d) => f"${localName(iri)}%s/$d%2.2f} "}.mkString(",")}
                   |Model: ${model.show}
                   |----------------
                   """.stripMargin)

case class ExpectedBoolean(obtained: String, location: Location) extends
  AF2ShExError(s"""|Expected boolean represented as Y/N. Obtained: ${obtained}
                   |Location: ${location.show}
                   """.stripMargin)

case class NonCRMLabel(iri: IRI) extends
  AF2ShExError(s"""|NonCRMLabel: ${iri}""".stripMargin)
                   
case class NoFieldType(iri: IRI, c: Constraint) extends
  AF2ShExError(s"""|NoFieldType: ${iri}
                   |Constraint: ${c.show}
                   |""".stripMargin)

object AF2ShExError {
  def msgErr(msg: String): AF2ShExError = MsgError(msg)

  implicit val show: Show[AF2ShExError] = Show.show(_.msg)

}
