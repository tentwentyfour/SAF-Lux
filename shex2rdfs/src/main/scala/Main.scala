import cats.effect._
import cats._
import cats.implicits._ 
import com.monovore.decline._
import com.monovore.decline.effect._
import java.io.File
import es.weso.rdf.nodes.RDFNode
import java.io.FileReader
import es.weso.rdf.nodes.IRI
import java.io._
import scala.io._
import es.weso.shex.Schema
import java.nio.file.Path
import es.weso.shex2rdfs._
import fs2._
import fs2.io._
import fs2.io.file.Files

case class Parse(path: Path)

object Main extends CommandIOApp(
  name="ShEx2RDFS", 
  header = "ShEx 2 RDFS",
  version = "0.0.1"
  ) {

  val parseOpt = Opts.option[Path]("file","ShEx file").map(Parse(_))
  val saveOpt = Opts.option[Path]("save","Save to file (default = show RDFS in console)").orNone

  override def main: Opts[IO[ExitCode]] =
   (parseOpt, saveOpt).mapN {
     case (Parse(path),maybeSave) => for {
       _ <- IO.println(s"File to load: $path")
       schema <- Schema.fromFile(path.toFile.getAbsolutePath)
       str <- ShEx2RDFS.shex2RDFS(schema, "TURTLE") 
       _ <- maybeSave match {
         case None => IO.println(str)
         case Some(outPath) => writeContents(outPath, str)
       }
    } yield ExitCode.Success
   }

  def writeContents(path: Path, cs: String): IO[Unit] = {
    Stream(cs)
    .through(text.utf8Encode)
    .through(Files[IO].writeAll(path))
    .compile
    .drain
  }
}