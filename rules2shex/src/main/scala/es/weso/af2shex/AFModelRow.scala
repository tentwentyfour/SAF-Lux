package es.weso.af2shex
import es.weso.rdf.nodes.IRI
import cats._
import cats.implicits._

sealed abstract class AFModelRow extends Product with Serializable

case object Skipped extends AFModelRow {
  override def toString: String = "<Skipped>"
}
case class Header(values: List[String]) extends AFModelRow {
  override def toString: String = "<Header>"
}

case class Constraint(
  position: Int,
  _type: String,
  fieldAF: String,
  fieldType: Option[IRI],
  domain: IRI,
  property: IRI,
  range: IRI,
  required: Option[Boolean],
  repeated: Option[Boolean],
  dataType: String,
  description: Option[String],
  source_of_rules: Option[String]
) extends AFModelRow 

object Constraint {
  implicit val showConstraint: Show[Constraint] = 
   Show.show(c => s"Row[${c.position}]: ${c._type},${c.fieldAF},${c.fieldType},${c.domain},${c.property},${c.range},${c.required},${c.repeated},${c.dataType}")
}
