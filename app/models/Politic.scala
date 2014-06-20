package models

import scala.slick.lifted.ProvenShape.proveShapeOf
import play.api.db.slick.Config.driver.simple._

/**
 * Created by diego on 20/06/14.
 */

case class Politic(id:Option[Long], name: String)

class PoliticTable(tag: Tag) extends Table[Politic](tag, "POLITIC") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.NotNull)

  def * = (id.?, name) <>(Politic.tupled, Politic.unapply)
}