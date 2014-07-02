package controllers

/**
 * Created by diego on 02/07/14.
 */

import scala.io.{BufferedSource, Source}
import java.io.{FilenameFilter, File}

import scala.slick.lifted.TableQuery

import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import play.api.mvc._
import play.api.Play.current
import models.{Record, Politic, RecordTable, PoliticTable}
import org.joda.time.DateTime
import manager.Util


/**
 * Created by diego on 26/06/14.
 */
object ConfigurationController extends Controller {

  val politicTable = TableQuery[PoliticTable]
  val recordTable = TableQuery[RecordTable]

  def loadLastConfiguration() = DBAction {
    implicit rs =>
      recordTable.delete
      politicTable.delete
      val dataMap: Map[String, List[(String, String, String)]] = parseFile(pathDataFile)
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
      Ok("true")
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
}
