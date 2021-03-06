package controllers

import play.api._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import play.api.mvc._
import play.api.Play.current
import models.{Record, Politic, RecordTable, PoliticTable}
import org.joda.time.DateTime
import manager.Util
import scalax.io.{Output, Resource}
import play.libs.Json
import com.github.tototoshi.slick.H2JodaSupport._


object Application extends Controller {

  val politicTable = TableQuery[PoliticTable]
  val recordTable = TableQuery[RecordTable]

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def index = DBAction { implicit rs =>
    val x: Option[Long] = Some(1L)
    val records: List[(String, List[(String, Long)])] = {
      val queryExtractPoliticRecords = for {
        (politic, record) <- politicTable innerJoin recordTable on (_.id === _.idPolitic)
      } yield (politic.name, record.friends, record.dateInsert)
      val listOfRecords: List[(String, Long, DateTime)] = queryExtractPoliticRecords.list()

      val values: Map[DateTime, List[(String, Long)]] = listOfRecords.groupBy(_._3)
        .mapValues(x => x.map(value => (value._1, value._2)))
      val listDateTime = values.toList.sortBy(_._1)
      listDateTime.map((x: (DateTime, List[(String, Long)])) => (x._1.toString("dd/MM/YYYY"), x._2))
    }
    Ok(views.html.index(records))
  }

  def addRecord(date: String, politicName: String, friends: Long) = DBAction { implicit rs =>
    val politicOpt: Option[Politic] = politicTable.filter(_.name === politicName).firstOption

    val record: Option[Record] = for {
      politic <- politicOpt
    } yield Record(None, DateTime.parse(date, Util.formatter), friends, politic.id.get)

    record match {
      case Some(r) => recordTable += r; Ok("true")
      case None => Ok("false")
    }
  }

  def updateDate(beforeDate: String, newDate: String) = DBAction { implicit rs =>
    val beforeDateParsed: DateTime = DateTime.parse(beforeDate, Util.formatter)
    val newDateParsed: DateTime = DateTime.parse(newDate, Util.formatter)

    val recordsAlreadyIn: List[RecordTable#TableElementType] = recordTable.filter(_.dateInsert === newDateParsed).list()
    recordsAlreadyIn match {
      case List() => {
        recordTable.filter(_.dateInsert === beforeDateParsed).map(_.dateInsert).update(newDateParsed)
        Ok("true")
      }
      case head :: tail => Ok("overwrite")
    }
  }

  def updateRecord(date: String, politicName: String, friends: String) = DBAction { implicit rs =>
    val politic: Politic = politicTable.filter(_.name === politicName).first
    val dateParsed: DateTime = DateTime.parse(date, Util.formatter)

    val record = for {
      (p, r) <- politicTable innerJoin recordTable on (_.id === _.idPolitic)
      if ((r.dateInsert === dateParsed) && (p.name === politic.name))
    } yield (r)


    val listRecords: List[Record] = record.list()

    listRecords match {
      case List() => recordTable += Record(None, dateParsed, friends.toLong, politic.id.get)
      case head :: tail => recordTable.filter(_.id === head.id).map(_.friends).update(friends.toLong)
    }
    Ok("true")
  }

  def removeRecords(date: String) = DBAction {  implicit rs =>
    val dateParsed: DateTime = DateTime.parse(date, Util.formatter)
    val delete: Int = recordTable.filter(_.dateInsert === dateParsed).delete
    if (delete > 0) {
      Ok("true")
    } else {
      Ok("false")
    }
  }

  def saveRecords(data: String) = Action {
    println(data)
    println(Json.parse(data))
    Ok("true")
  }

  def saveRecordsToFile() = DBAction { implicit rs =>
    val innerJoin = for {
      (politic, record) <- politicTable innerJoin recordTable on (_.id === _.idPolitic)
    } yield (politic.name, record.friends, record.dateInsert)

    val listRecords: List[(String, Long, DateTime)] = innerJoin.list()

    val filePath = s"data/${new DateTime().toString("yyyyMMdd")}_data"

    val output: Output = Resource.fromOutputStream(new java.io.FileOutputStream(filePath))
    val line: String = listRecords.map(el => {
      s"${el._1};${el._2};${el._3.toString("dd/MM/yyyy")}"
    }).mkString("\n")

    output.write(s"$line")
    Ok("true")
  }

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.Application.addRecord,
          routes.javascript.Application.updateDate,
          routes.javascript.Application.saveRecords,
          routes.javascript.Application.saveRecordsToFile,
          routes.javascript.Application.removeRecords,
          routes.javascript.ConfigurationController.loadLastConfiguration,
          routes.javascript.Application.updateRecord
        )
      )
  }
}