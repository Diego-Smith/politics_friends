package models

import scala.slick.lifted.ProvenShape.proveShapeOf
import play.api.db.slick.Config.driver.simple._
import org.joda.time.DateTime
import com.github.tototoshi.slick.H2JodaSupport._

/**
 * Created by diego on 20/06/14.
 */

case class Politic(id:Option[Long], name: String, dateInsert: DateTime= DateTime.now())

class PoliticTable(tag: Tag) extends Table[Politic](tag, "POLITIC") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.NotNull)
  def dateInsert = column[DateTime]("DATE_INSERT")
  def * = (id.?, name, dateInsert) <>(Politic.tupled, Politic.unapply)
}