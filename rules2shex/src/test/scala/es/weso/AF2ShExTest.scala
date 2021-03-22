package es.weso.af2shex
import munit._
import AFModel._
import AFModelParser._

class AF2ShExSuite extends FunSuite {

  test("fieldAF2IRI") {
    assertEquals(fieldAF2IRI("Heading","Name"), Right(afl + "HeadingName"))
    assertEquals(fieldAF2IRI("anything","Gender"), Right(afl + "Gender"))
    assertEquals(fieldAF2IRI("anything","Gender "), Right(afl + "Gender"))
    assertEquals(fieldAF2IRI("Profession","Profession  - type"), Right(afl + "ProfessionType"))
    assertEquals(fieldAF2IRI("Notes","Notes - internal/non public"), Right(afl + "NotesInternal"))
  }
}