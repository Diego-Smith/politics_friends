import manager.Util
import org.joda.time.DateTime
import play.api.GlobalSettings
import play.api.Application
import scala.io.{BufferedSource, Source}
import java.io.{FilenameFilter, File}

import play.api.db.slick.Session
import scala.slick.lifted.TableQuery
import play.api.Play.current
import models.{Record, RecordTable, Politic, PoliticTable}
import play.api.db.DB
import scala.slick.driver.H2Driver.simple._

/**
 * Created by diego on 26/06/14.
 */
object Global extends GlobalSettings {

  def main(args: Array[String]) {
    println(pathDataFile)
  }

  override def onStart(app: Application) {
    obtainDB
    if (checkDbIsEmpty) {
      val dataMap: Map[String, List[(String, String, String)]] = parseFile(pathDataFile)
      fillDb(dataMap)
    }
  }

  implicit def fileNameOrder: Ordering[String] = Ordering.fromLessThan(_ > _)
  lazy val pathDataFile: String = {
    val file: File = new File("data/")
    val list = file.list(new FilenameFilter {
      override def accept(p1: File, p2: String): Boolean = {
        p2.contains("data") && p2.split("_").size == 2
      }
    })
    val listSorted: Array[String] = list.sortBy(identity)
    s"data/${listSorted(0)}"
  }

  def checkDbIsEmpty(): Boolean = {
    val politicTable = TableQuery[PoliticTable]
    play.api.db.slick.DB.withSession {
      implicit session: Session => {
        val firstOption: Option[PoliticTable#TableElementType] = politicTable.firstOption
        firstOption.map(_ => false).getOrElse(true)
      }
    }
  }


  def parseFile(filePath: String) = {
    val file: BufferedSource = Source.fromFile(new File(filePath))

    val list = file.getLines().map(s => {
      s.split(";") match {
        case Array(x, y, z) => (x, y, z)
        case t => println("WARNING DATA:" + t.toString); ("", "", "")
      }
    }).toList

    val cleanList = list.filter(_._1 != "")
    cleanList.groupBy(_._1)
  }

  def fillDb(dataMap: Map[String, List[(String, String, String)]]) = {
    val politicTable = TableQuery[PoliticTable]
    val recordTable = TableQuery[RecordTable]
    play.api.db.slick.DB.withSession {
      implicit session: Session => {

        //        session.prepareStatement("delete from record").execute()
        //        session.prepareStatement("delete from politic").execute()

        val mapRecordsWithUserId: Map[String, List[(Long, String, String)]] = dataMap.map(record => {
          val id: Long = politicTable.filter(_.name === record._1).firstOption match {
            case Some(p) => p.id.get
            case None => politicTable.returning(politicTable.map(_.id)) += Politic(None, record._1, DateTime.now())
          }
          (record._1, record._2.map(t => (id, t._2, t._3)))
        })


        for {
          politicRecords <- mapRecordsWithUserId
          tuple <- politicRecords._2
        } recordTable += Record(None, DateTime.parse(tuple._3, Util.formatter), tuple._2.toLong, tuple._1)
      }
    }
  }

  import slick.driver.H2Driver.backend.DatabaseDef

  def obtainDB: DatabaseDef = Database.forDataSource(DB.getDataSource())
}
