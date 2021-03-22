package es.weso.af2shex
import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import java.io.File
import java.nio.file._
import org.apache.poi.ss.usermodel._
import scala.jdk.CollectionConverters._
import es.weso.rdf.nodes.IRI
import es.weso.rdf.nodes.RDFNode
import es.weso.shex._
import AFModel._

case class Config(model: RDFSModel)
case class State(previousType: String, previousField: String) 

object AFModelParser {

  val START_ROW = 3
  val END_ROW   = 58

  type Comp[A] = ReaderWriterStateT[IO,Config,List[AF2ShExError],State,A]
  def getState: Comp[State] = ReaderWriterStateT.get
  def updateState(f: State => State): Comp[Unit] = ReaderWriterStateT.modify(f)
  def fromIO[A](x: IO[A]): Comp[A] = ReaderWriterStateT.liftF(x)
  def getConfig: Comp[Config] = ReaderWriterStateT.ask
  def getModel(): Comp[RDFSModel] = getConfig.map(_.model)
  def ok[A](x:A): Comp[A] = ReaderWriterStateT.pure(x)
  def addError(err: AF2ShExError): Comp[Unit] = ReaderWriterStateT.tell(List(err))

  def run_[A](cfg: Config, state: State, c: Comp[A]): IO[(List[AF2ShExError],A)] = { 
    c.run(cfg,state).map(x => (x._1, x._3))
  }

  def getPreviousType: Comp[String] = getState.map(_.previousType)
  def setPreviousType(v: String): Comp[Unit] = updateState(s => s.copy(previousType = v))
  def getPreviousField: Comp[String] = getState.map(_.previousField)
  def setPreviousField(v: String): Comp[Unit] = updateState(s => s.copy(previousField = v))

  val fileName = "examples/DMGRules_003.xlsx"
  val file = new File(fileName)
  
  def mkWorkbook(file: File): Resource[Comp, Workbook] = {
    def acquire: Comp[Workbook] = fromIO(IO(WorkbookFactory.create(file)))
    def release(wb: Workbook): Comp[Unit] = fromIO(IO(wb.close()))
    Resource.make(acquire)(release)
  }

  def parseCellAsModelIRI(r: Row, n: Int): Comp[IRI] = 
   for {
     model <- getModel()
     name <- parseCellAsString(r,n)
     val location = Location(r.getRowNum(),n)
     iri <- model.findSimilarCIDOCStyle(name).toList match {
       case Nil => 
         addError(NoMatch(name, location, model)) *> 
         ok(IRI(""))
       case pair :: Nil => 
         ok(pair._1)
       case pairs => 
         addError(MultipleMatches(name, pairs, location, model)) *> 
         ok(pairs.head._1)
      }
   } yield iri

  def parseCellAsBoolean(r: Row, n: Int): Comp[Boolean] = 
    parseCellAsString(r,n).flatMap(str => str.toUpperCase match {
      case "Y" => ok(true)
      case "N" => ok(false)
      case other   =>
        addError(ExpectedBoolean(other, Location(r.getRowNum(),n))) *>
        ok(false)
    }) 
  
  def parseCellAsString(r: Row, n: Int): Comp[String] = 
   r.getCell(n) match {
    case null    => ok("")
    case c: Cell if c.getCellType == CellType.BLANK => ok("")
    case c: Cell if c.getCellType == CellType.STRING => ok(c.getStringCellValue())
    case c: Cell if c.getCellType == CellType.NUMERIC => ok(c.getNumericCellValue().toString)
    case c: Cell =>
      addError(UnsupportedCellType(c.getCellType.toString, mkLocation(r,n))) *> 
      ok("???")
  }
  
  private def mkLocation(r: Row, n: Int): Location = Location(r.getRowNum(),n)
   
  def parseCellAsString_orPreviousField(r:Row, n: Int): Comp[String] = 
   parseOpt(parseCellAsString,r,n).flatMap(maybeString => maybeString match {
     case None => getPreviousField
     case Some(v) => setPreviousField(v) *> ok(v)
   })

  def parseCellAsString_orPreviousType(r:Row, n: Int): Comp[String] = 
   parseOpt(parseCellAsString,r,n).flatMap(maybeString => maybeString match {
     case None => getPreviousType
     case Some(v) => setPreviousType(v) *> ok(v)
   })
   
  def parseOpt[A](parser:(Row,Int) => Comp[A], r: Row, n: Int): Comp[Option[A]] = 
   r.getCell(n) match {
     case null => ok(None)
     case c: Cell if c.getCellType == CellType.BLANK => ok(None)
     case c: Cell => parser(r,n).map(Some(_))
   }

  def fieldAF2IRI(_type: String, fieldAF: String): Either[AF2ShExError,IRI] = {
    // Generate a IRI from a combination of _type/fieldAF

    // Regular expressions
    // Unanchored means that it applies to any part of the string: 
    // See: https://www.scala-lang.org/api/current/scala/util/matching/Regex.html
    
    // Note that the patterns must be lower case to avoid case sensitive mistakes
    val alternative = "alternative".r.unanchored
    val dates = "dates".r.unanchored
    val typeR = "type".r.unanchored
    val birth = "birth".r.unanchored
    val death = "death".r.unanchored
    val gender = "gender".r.unanchored
    val internal = "internal".r.unanchored
    val text = "text".r.unanchored
    val field = "field".r.unanchored
    val url = "url".r.unanchored
    val name = "name".r.unanchored
    val id = "id".r.unanchored
    val source = "source".r.unanchored
    val status = "status".r.unanchored
    val creation = "creation".r.unanchored
    val modification = "modification".r.unanchored
    
    (_type.toLowerCase(), fieldAF.toLowerCase()) match {
     case ("heading", "name") => (afl + "HeadingName").asRight
     case ("heading", "type") => (afl + "HeadingType").asRight
     case ("heading", "numeration") => (afl + "HeadingNumeration").asRight
     case ("heading", "title") => (afl + "HeadingTitle").asRight
     case (alternative(_*), "name") => (afl + "AlternativeName").asRight
     case (alternative(_*), "numeration") => (afl + "AlternativeNumeration").asRight
     case (alternative(_*), "title") => (afl + "AlternativeTitle").asRight
     case (dates(_*), birth(_*)) => (afl + "DatesBirth").asRight
     case (dates(_*), death(_*)) => (afl + "DatesDeath").asRight
     case (_, gender(_*)) => (afl + "Gender").asRight
     case ("profession", typeR(_*)) => (afl + "ProfessionType").asRight
     case ("profession", dates(_*)) => (afl + "ProfessionDate").asRight
     case ("activity", typeR(_*)) => (afl + "ActivityType").asRight
     case ("activity", dates(_*)) => (afl + "ActivityDate").asRight
     case ("notes", "notes - public") => (afl + "NotesPublic").asRight
     case ("notes", internal(_*)) => (afl + "NotesInternal").asRight
     case ("administration fields", "Status of data") => (afl + "AdministrativeStatus").asRight
     case ("sources of information", text(_*)) => (afl + "SourcesText").asRight
     case ("sources of information", field(_*)) => (afl + "SourcesField").asRight
     case ("sources of information", url(_*)) => (afl + "SourcesUrl").asRight
     case ("external standard identifier", name(_*)) => (afl + "ExternalStandardIdentifierName").asRight
     case ("external standard identifier", id(_*)) => (afl + "ExternalStandardIdentifierID").asRight
     case ("administration fields", internal(_*)) => (afl + "InternalStandardIdentifier").asRight
     case ("administration fields", source(_*)) => (afl + "SourceOfData").asRight
     case ("administration fields", status(_*)) => (afl + "StatusOfData").asRight
     case ("administration fields", creation(_*)) => (afl + "DateOfCreation").asRight
     case ("administration fields", modification(_*)) => (afl + "DateOfModification").asRight
     case ("administration fields", modification(_*)) => (afl + "AuthorOfModification").asRight
     case (_,_) => MsgError(s"Unknown field values: \'${_type}\'/\'${fieldAF}\'").asLeft
    }
  }


  def parseCIDOCRow(r: Row):Comp[AFModelRow] = {
   r.getRowNum match {
     case n if n < START_ROW => ok(Skipped)
     case n if n > END_ROW => ok(Skipped)
     case n => for {
       _type    <- parseCellAsString_orPreviousType(r,0)
       fieldAF  <- parseCellAsString_orPreviousField(r,1)
       fieldType <- fieldAF2IRI(_type, fieldAF) match {
         case Left(e) => addError(e) *> ok(None)
         case Right(v) => ok(Some(v))
       }
       domain   <- parseCellAsModelIRI(r, 2)
       property <- parseCellAsModelIRI(r,3)
       range    <- parseCellAsModelIRI(r,4)
       // Column 5 skipped (description)
       required <- parseOpt(parseCellAsBoolean,r,6)
       repeated <- parseOpt(parseCellAsBoolean,r,7)
       datatype <- parseCellAsString(r,8)
       description <- parseOpt(parseCellAsString,r,9)
       source_of_rules <- parseOpt(parseCellAsString,r,10)
     } yield Constraint(
       _type = _type, 
       fieldAF = fieldAF, 
       fieldType = fieldType,
       position = n, 
       domain = domain, 
       property = property, 
       range = range, 
       required = required,
       repeated = repeated,
       dataType = datatype,
       description = description,
       source_of_rules = source_of_rules)
   }
   
  }
    
  def parseCIDOCSheet(sheet: Sheet): Comp[List[AFModelRow]] = {
    sheet
    .rowIterator
    .asScala
    .toList
    .map(parseCIDOCRow(_))
    .sequence
    .map(
      _.dropWhile(_ == Skipped).takeWhile(_ != Skipped)
    )
  }
  
  def compXLSX(file: File): Comp[AFModel] = // IO[(List[AF2ShExError],AFModel)] = 
    mkWorkbook(file).use(wb => for {
      rs <- parseCIDOCSheet(wb.getSheet("CIDOC"))
    } yield AFModel(rs.collect { case c: Constraint => c }))

  def parseXLSX(file: File, model: RDFSModel): IO[(List[AF2ShExError],AFModel)] = 
    run_(Config(model), State("",""), compXLSX(file))
  
}