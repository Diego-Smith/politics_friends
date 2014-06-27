package models

import scala.slick.lifted.ProvenShape.proveShapeOf
import play.api.db.slick.Config.driver.simple._
import org.joda.time.DateTime
import com.github.tototoshi.slick.H2JodaSupport._
import scala.slick.lifted.TableQuery

/**
 * Created by diego on 20/06/14.
 */

case class Record(id:Option[Long], dateInsert: DateTime= DateTime.now(), friends: Long, idPolitic: Long)

class RecordTable(tag: Tag) extends Table[Record](tag, "RECORD") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def dateInsert = column[DateTime]("DATE_INSERT")
  def friends = column[Long]("FRIENDS")
  def idPolitic = column[Long]("POLITIC")
  def * = (id.?, dateInsert, friends, idPolitic) <>(Record.tupled, Record.unapply)

  def politic = foreignKey("FK_POLITIC", idPolitic, TableQuery[PoliticTable])(_.id)
  def uniqueRecord = index("INDEX_UNIQUE_RECORD", (dateInsert, idPolitic), true)

}