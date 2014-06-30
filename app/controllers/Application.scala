package controllers

import play.api.mvc.{Action, Controller}
import play.api.db.slick.Session
import scala.slick.lifted.TableQuery
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import models.{Record, RecordTable, PoliticTable, Politic}
import org.joda.time.DateTime
import scala.collection.immutable.ListMap
import play.api.Routes
import org.joda.time.format.{DateTimeFormatterBuilder, DateTimeFormatter}
import manager.Util


object Application extends Controller with Util {

  val politicTable = TableQuery[PoliticTable]
  val recordTable = TableQuery[RecordTable]

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def index = Action {
    val x:Option[Long] = Some(1L)
    val records: List[(String, List[(String, Long)])] = play.api.db.slick.DB.withSession {
      implicit session: Session => {
        val queryExtractPoliticRecords = for {
          (politic, record) <- politicTable innerJoin recordTable on (_.id === _.idPolitic)
        } yield (politic.name, record.friends, record.dateInsert)
        val listOfRecords: List[(String, Long, DateTime)] = queryExtractPoliticRecords.list()

        val values: Map[String, List[(String, Long)]] = listOfRecords.groupBy(_._3.toString("dd/MM/YYYY")).mapValues(x => x.map(value => (value._1, value._2)))
        values.toList.sortBy(_._1)
      }
    }
    Ok(views.html.index(records))
  }

  def addRecord(date:String, politicName: String, friends: Long) = Action {
    play.api.db.slick.DB.withSession {
      implicit session: Session => {
        val politicOpt: Option[Politic] = politicTable.filter(_.name === politicName).firstOption

        val record: Option[Record] = for {
          politic <- politicOpt
        } yield Record(None, DateTime.parse(date, formatter), friends, politic.id.get)

        record match {
          case Some(r) => recordTable += r; Ok("true")
          case None => Ok("false")
        }
      }
    }
  }

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.Application.addRecord
        )
      )
  }
}