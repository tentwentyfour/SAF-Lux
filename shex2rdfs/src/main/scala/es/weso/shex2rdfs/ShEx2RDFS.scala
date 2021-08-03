package es.weso.shex2rdfs

import es.weso.rdf.jena.RDFAsJenaModel
import cats.effect._
import es.weso.rdf._
import es.weso.rdf.nodes._
import es.weso.rdf.triples._
import es.weso.shex.IRILabel
import es.weso.shex._
import cats.implicits._

object ShEx2RDFS {

 val rdfs = IRI("http://www.w3.org/2000/01/rdf-schema#")
 val rdf  = IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
 val dc   = IRI("http://purl.org/dc/elements/1.1/")
 val afl  = IRI("http://data.culture.lu/ns/saf/")
 val owl  = IRI("http://www.w3.org/2002/07/owl#")

 def shex2RDFS(schema: Schema, format: String): IO[String] = 
    RDFAsJenaModel.empty.flatMap(_.use(rdf => for {
      _ <- createRDFS(schema, rdf)
      str <- rdf.serialize(format)
 } yield str))

 def createRDFS(schema: Schema, builder: RDFBuilder): IO[Unit] = for {
   _ <- builder.addPrefixMap(schema.prefixMap)
   _ <- builder.addPrefix("dc",dc)
   _ <- builder.addTriple(RDFTriple(IRI(""), dc + "title", StringLiteral("Shared Authority File Luxembourgh Vocabulary")))
   _ <- builder.addTriple(RDFTriple(IRI(""), rdf + "type", owl + "Ontology"))
   _ <- addAnnotations(schema,builder)
 } yield (())

 def annotations = IRILabel(afl + "Annotations")

 case object NotFoundAnnotations extends RuntimeException(s"Not found annotations shape: $annotations")
 case class AnnotationsNotShape(se: ShapeExpr) extends RuntimeException(s"Annotations label should be a shape, but it is: $se")
 case class NotFoundEachOf(te: TripleExpr) extends RuntimeException(s"Unexpected triple expression: $te when it was expecting an EachOf")
 case class UnexpectedTripleExpr(te: TripleExpr) extends RuntimeException(s"Unexpected TripleExpr: $te when it was expecting a triple constraint")

 def addAnnotations(schema: Schema, builder: RDFBuilder): IO[Unit] = 
   schema.getShape(annotations).fold(str => IO.raiseError(NotFoundAnnotations), _ match {
     case shape: Shape => shape.expression match {
       case None => IO.pure(())
       case Some(te) => te match {
         case eo: EachOf => eo.expressions.map(te => te match {
          case tc: TripleConstraint => tc.annotations.getOrElse(List()).map(addAnnotation(schema, builder, tc.predicate)).sequence
          case other => IO.raiseError(UnexpectedTripleExpr(other))
         }).sequence.map(_ => ())
         case other => IO.raiseError(NotFoundEachOf(other))
       }
     }
     case se => IO.raiseError(AnnotationsNotShape(se))
   }
 )

 def addAnnotation(schema: Schema, builder: RDFBuilder, subj: IRI)(a: Annotation): IO[Unit] = 
   builder.addTriple(RDFTriple(subj,a.predicate, a.obj.getNode)) *> 
   IO.pure(())
   
}