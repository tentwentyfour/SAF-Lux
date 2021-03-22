package es.weso.af2shex
import cats._
import cats.implicits._

case class Location(row: Int, cell: Int)

object Location {
  implicit val showLocation: Show[Location] = Show.show(loc => s"(${loc.row},${loc.cell})")
}