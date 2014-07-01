import manager.Util
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import play.api.GlobalSettings
import play.api.Application
import scala.io.{BufferedSource, Source}
import java.io.File

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

  override def onStart(app: Application) {
    val dataMap: Map[String, List[(String, String, String)]] = parseFile("data/data")
    obtainDB
    if(checkDbIsEmpty) {
      fillDb(dataMap)
    }
  }

  def checkDbIsEmpty() : Boolean = {
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

        //TODO: change sql statement with a static type checking statement
        session.prepareStatement("delete from record").execute()
        session.prepareStatement("delete from politic").execute()

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
