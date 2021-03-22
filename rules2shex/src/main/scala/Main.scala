import cats.effect._
import es.weso.af2shex._
import cats._
import cats.implicits._ 
import com.monovore.decline._
import com.monovore.decline.effect._
import java.io.File
import es.weso.rdf.nodes.RDFNode
import java.io.FileReader
import es.weso.rdf.nodes.IRI
import es.weso.shex.implicits.showShEx._
import java.nio.file.{Files => _, _}
import fs2.io._
import java.io._
// import cats.data.EitherT
import cats.effect._
import scala.io._
import fs2._
// import fs2.io.file.Files

case class Parse(path: Path)

object Main extends CommandIOApp(
  name="AF2ShEx", 
  header = "Authority files 2 ShEx",
  version = "0.0.1"
  ) {

  val parseOpt = Opts.option[Path]("file","Excel file").map(Parse)
  val showAFModel = Opts.flag("showAFModel", help = "Show internal AF model (default=false)").orFalse
  val showErrors = Opts.flag("noErrors", help = "Don't show errors after processing (default=true)").orTrue
  val addRowNumbers = Opts.flag("addRowNumbers", help = "Add row numbers to ShEx").orFalse
  val saveOpt = Opts.option[Path]("save","Save to file (default = show ShEx in console)").orNone

  val crm = AFModel.crm 
  val frbroo = AFModel.frbroo 
  val rdfs = IRI("http://www.w3.org/2000/01/rdf-schema#")

 
  override def main: Opts[IO[ExitCode]] =
   (parseOpt, saveOpt, showAFModel,showErrors, addRowNumbers).mapN {
     case (Parse(path),maybeSave, showAFModel, showErrors, addRowNumbers) => {
       val cmp = for {
        cidoc <- RDFSModel.fromReader("CIDOC", new FileReader("src/main/resources/rdfs/cidoc_crm_v6.2.1-2018April.rdfs.xml"),"RDF/XML")
        frbrooModel <- RDFSModel.fromReader("FRBROO", new FileReader("src/main/resources/rdfs/FRBR2.4-draft.rdfs.xml"),"RDF/XML")
        rdfsModel <- RDFSModel.fromReader("RDFS",new FileReader("src/main/resources/rdfs/rdfs.ttl"),"Turtle")
        model = cidoc
         .merge(frbrooModel)
         .merge(rdfsModel)
         .addPrefix("crm",crm)
         .addPrefix("frbroo",frbroo)
         .addPrefix("rdfs",rdfs)
         .addClass(crm + "E62_String")
         parsed <- AFModelParser.parseXLSX(path.toFile, model)
         (errs, afmodel) = parsed
         schema = afmodel.toShEx(addRowNumbers)
         _ <- maybeSave match {
           case None => IO(println(s"${schema.show}"))
           case Some(path) => writeContents(path,schema.show)
         }
         _ <- if (showAFModel) IO { 
          println(s"${afmodel.constraints.map(_.show).mkString("\n")}") 
         } else IO()
         _ <- if (showErrors) IO {
              errs.map(e => println(s"Error: ${e.show}"))
              println(s"${errs.size} errors") 
              println(schema.wellFormed.fold(err => s"Not well formed\nErrors: ${err}", _ => "Well formed"))
         } else IO(
         )
       } yield (errs, afmodel, schema)
       cmp.attempt.flatMap(_.fold(
         err => IO {
          println(s"Error: ${err}")
          ExitCode.Error
        },
        _ => IO(ExitCode.Success)
       ))
      } 

   }

   def writeContents(path: Path, cs: String): IO[Unit] = {
    import scala.concurrent.ExecutionContext
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    Stream.resource(Blocker[IO]).flatMap(blocker =>
     Stream(cs).through(text.utf8Encode).through(io.file.writeAll[IO](path,blocker))
    ).compile.drain
   }

}