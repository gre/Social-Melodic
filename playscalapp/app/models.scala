package models

import play._
import play.mvc._

import play.db.anorm._
import play.db.anorm.SqlParser._
import play.db.anorm.defaults._

case class Melody (
  id: Pk[Long]
) {
  
  // TODO
  def getNotesArray() = "[[14, 16], [13], [23], [], [20], [], [7], [], [22], [], [23], [], [8], [], [20], [], [22], [], [22], []]"
}

object Melody extends Magic[Melody] {
  def getFirst() = {
    Melody.find().first().get
  }
}
