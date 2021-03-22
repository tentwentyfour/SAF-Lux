package es.weso.af2shex
import AFModel._
import es.weso.shex._
import es.weso.rdf.nodes.IRI
import es.weso.rdf.PREFIXES._
import es.weso.rdf._
import IRIUtils._
import cats._
import cats.data._
import cats.implicits._
import es.weso.rdf.nodes.StringLiteral

case class AFModel(constraints: List[Constraint]) {

  def checkMainShape(c: Constraint): Boolean = {
    c.required == None
  }

  def toShEx(addRowNumbers: Boolean): Schema = {

    case class State(smap: Map[ShapeLabel,ShapeExpr])
    case class Config(addRowNumbers: Boolean)
    type Comp[A] = ReaderWriterStateT[Id,Config, List[AF2ShExError], State,A]
    def ok[A](x:A): Comp[A] = x.pure[Comp]
    def addError(e: AF2ShExError): Comp[Unit] = IndexedReaderWriterStateT.tell(List(e))
    def modify(f: State => State): Comp[Unit] = IndexedReaderWriterStateT.modify(f)
    def run[A](config: Config, state: State, comp: Comp[A]): (List[AF2ShExError], State, A) = comp.run(config, state)
    def getAddRowNumbers: Comp[Boolean] = { 
      val cfg: Comp[Config] = IndexedReaderWriterStateT.ask
      cfg.map(_.addRowNumbers)
    }
    
    def mkAnnotation(c: Constraint, addRowNumbers: Boolean): Option[List[Annotation]] = 
      if (addRowNumbers) 
       Some(List(Annotation(rdfs + "comment", StringValue(s"Row ${c.position + 1}"))))
      else 
       None

    def addMainShape(c: Constraint): Comp[Unit] = {
     for {
      lbl <- generateLabelShape(c.domain) 
      rangeLabel <- generateLabelShapeAux(c.range, c.fieldType, c)
      addRowNumbers <- getAddRowNumbers
      _ <- modify(s => s.copy(smap = s.smap.updatedWith(lbl){
        case None => Some(Shape.expr(
           EachOf.fromExpressions(
            TripleConstraint.emptyPred(`rdf:type`).copy(
              valueExpr = Some(NodeConstraint.valueSet(List(IRIValue(c.domain)), List())),
              annotations = mkAnnotation(c, addRowNumbers)
            ),
            TripleConstraint.emptyPred(c.property).copy(
              valueExpr = Some(ShapeRef(rangeLabel, None,None)),
              annotations = mkAnnotation(c,addRowNumbers)
            )
           )
          )
          .addId(lbl))
        case Some(s: Shape) => 
          Some(addTripleConstraint(s,c.property,rangeLabel,c,addRowNumbers))
        case Some(s) => {
          println(s"addMainShape/warning: ${s}")
          Some(s)
        }
      }))
     } yield () 
    }

   def generateLabelShape(iri: IRI): Comp[ShapeLabel] = getQName(iri) match {
     case (`crm`,localName) => ok(IRILabel(afl + (localName + "Shape")))
     case (_,_) => addError(NonCRMLabel(iri)) *> ok(IRILabel(iri))
   }

   def mkShapeAux(lbl: ShapeLabel, domain: IRI, pred: IRI, rangeLabel: ShapeLabel, c: Constraint, addRowNumbers: Boolean): Shape = 
    Shape.expr(
           EachOf.fromExpressions(
             tripleConstraint_type(domain, c, addRowNumbers)
            /*TripleConstraint.emptyPred(`rdf:type`).copy(
              valueExpr = Some(NodeConstraint.valueSet(List(IRIValue(domain)), List())),
              annotations = mkAnnotation(c,addRowNumbers)
            ) */,
            TripleConstraint.emptyPred(pred).copy(
              valueExpr = Some(ShapeRef(rangeLabel, None,None)),
              annotations = mkAnnotation(c,addRowNumbers)
            )
           )
     ).addId(lbl)

   def addTripleConstraint(s: Shape, pred: IRI, rangeLabel: ShapeLabel, c: Constraint, addRowNumbers: Boolean): Shape = {
    val newTc = TripleConstraint.emptyPred(pred).copy(
                valueExpr = Some(ShapeRef(rangeLabel,None,None)),
                annotations = mkAnnotation(c,addRowNumbers),
                optMin = c.required.map(b => if (b) 1 else 0),
                optMax = c.repeated.map(b => if (b) Star else IntMax(1)) 
    )
    s.expression match {
         case Some(tc: TripleConstraint) => 
          s.copy(expression = Some(EachOf.fromExpressions(tc, newTc)))
         case Some(eo: EachOf) => 
          s.copy(expression = Some(
            eo.copy(expressions = 
             eo.expressions 
             ++ List(newTc))))
         case _ => { 
           println(s"addTripleConstraint/Warning s.expression= ${s.expression}")
           s
         }
        }
      }

   def tripleConstraint_type(rangeType: IRI, c: Constraint, addRowNumbers: Boolean): TripleExpr = 
     TripleConstraint.emptyPred(`rdf:type`).copy(
       valueExpr = Some(NodeConstraint.valueSet(List(IRIValue(rangeType)), List())),
       annotations = mkAnnotation(c,addRowNumbers)
      )

   def addAuxShapeOrTripleConstraint(c: Constraint, addRowNumbers: Boolean): Comp[Unit] = for {
     lbl <- generateLabelShapeAux(c.domain,c.fieldType,c)
     rangeLabel <- generateLabelShapeAux(c.range,c.fieldType,c)
     addRowNumbers <- getAddRowNumbers
     _ <- modify(s => s.copy(
       smap = s.smap.updatedWith(lbl) {
        case None => Some(mkShapeAux(lbl,c.domain,c.property,rangeLabel,c,addRowNumbers))
        case Some(s: Shape) => Some(addTripleConstraint(s,c.property,rangeLabel,c,addRowNumbers))
        case Some(se) => {
          println(s"addAuxShapeOrTripleConstraint/Warning: ${se}")
          Some(se)  
        }
      }.updatedWith(rangeLabel) {
        case None => Some(Shape.expr(tripleConstraint_type(c.range, c, addRowNumbers)))
        case Some(s) => Some(s) // TODO: Not sure if we should add something here...
      }))
   } yield ()

   def generateLabelShapeAux(iri: IRI, fieldType: Option[IRI], c: Constraint): Comp[ShapeLabel] = 
    getQName(iri) match {
     case (`crm`,name) => {
      fieldType match {
        case None => addError(NoFieldType(iri, c)) *> ok(IRILabel(iri))
        case Some(iriFieldType) => ok(IRILabel(afl + (name + "_" + localName(iriFieldType))))
      }
     }
     case (_,_) => addError(NonCRMLabel(iri)) *> ok(IRILabel(iri))
    }

    def addConstraint(c: Constraint): Comp[Unit] = c match {
     case _ if c.property == `rdfs:subClassOf` => ok(())  // Skip rdfs:subClassOf declaration
     case _ if checkMainShape(c) => addMainShape(c)
     case _ => for {
       addRowNumbers <- getAddRowNumbers
       _ <- addAuxShapeOrTripleConstraint(c,addRowNumbers)
      } yield ()
    }

    def addShapes(cs: List[Constraint]): Comp[Unit] = 
      cs.map(addConstraint(_)).sequence.map(_ => ())
    
    val comp = for {
      _ <- addShapes(constraints)
    } yield ()

    val (ls, state, _) = run(Config(addRowNumbers), State(Map()), comp)

    val schema = state.smap.toList.foldRight(Schema.empty){
      case (pair, acc) => {
        val (lbl,se) = pair
        acc.addShape(se.addId(lbl))
      }
    }.copy(prefixes = Some(aflPrefixMap))

    schema
  }  

  def mkShapeExpr(lbl: ShapeLabel, cs: List[Constraint]): ShapeExpr = 
    Shape.expr(constraints2TripleExpr(cs)).addId(lbl)

  def constraints2TripleExpr(cs: List[Constraint]): TripleExpr = 
    EachOf.fromExpressions(cs.map(constraint2TripleExpr): _*)

  def constraint2TripleExpr(c: Constraint): TripleExpr = {
    TripleConstraint
    .emptyPred(c.property)
    .copy(valueExpr = crm2ShapeLabel(c.range)
                      .map(ShapeRef(_,None,None))
         )
  }

  def crm2ShapeLabel(iri: IRI): Option[ShapeLabel] = getQName(iri) match {
    case (`crm`,localName) => Some(IRILabel(afl + localName))
    case (_,_) => None
  }

}

object AFModel {
    val crm    = IRI("http://www.cidoc-crm.org/cidoc-crm/")
    val afl    = IRI("http://afl.org/")
    val frbroo = IRI("http://iflastandards.info/ns/fr/frbr/frbroo/")
    def aflPrefixMap = PrefixMap.fromMap(Map(
      "crm" -> crm, 
      "afl" -> afl, 
      "rdfs" -> rdfs, 
      "frbroo" -> frbroo,
      "rdf" -> rdf
    ))

}