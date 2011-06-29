package models

import play._
import play.mvc._

import play.db.anorm._
import play.db.anorm.SqlParser._
import play.db.anorm.defaults._

import java.util.{Date}

object Status extends Enumeration {
    type Status = Value
    val GENERATION = Value("GENERATION")
    val MATURATION = Value("MATURATION")
    val DEAD = Value("DEAD")
}

case class Family (
    id: Pk[Long],
    name: String,
    status: String = Status.GENERATION.toString,
    root_id: Long,
    created: Date = new Date(),
    luckOfMutation : Double = 0.1,
    luckOfJustIntonation : Double = 0.7,
    luckToEscape1of2 : Double = 0.85,
    luckToEscape1of4 : Double = 0.1,
    luckToIgnoreSamePosition : Double = 0.2
) {
    def bootstrap(nbMelodies:Int):Family = bootstrap(nbMelodies, 16, 20)
    def bootstrap(nbMelodies:Int, loopLength:Int, notesLength:Int):Family = {
        // TODO
        this
    }
    def setRoot(m:Melody) = {
        //root_id = m.id
        m.setFamily(this)
        this
    }
    override def toString = name
}

object Family extends Magic[Family] {
    
}

case class Melody (
  id: Pk[Long],
  parent_id: Long,
  family_id: Long,
  loopLength: Int,
  notesLength: Int,
  likes: Int = 0,
  total: Int = 0,
  generation: Int = 0,
  mutation: Int = 0,
  sterile: Boolean = false,
  created: Date = new Date()
) {

    def setFamily(f:Family) = {
        //family_id = f.id
        this
    }

    def getNotes() = {
        SQL("""
            select * from Note n
            join Melody m on n.melody_id =m.id
            order by n.pos
        """).as( Note ~< Melody ^^ flatten * )
    }

    // TODO
    def getNotesArray() = "[[14, 16], [13], [23], [], [20], [], [7], [], [22], [], [23], [], [8], [], [20], [], [22], [], [22], []]"
}

object Melody extends Magic[Melody] {
  def getFirst() = {
    Melody.find().first().get
  }
}


case class Note (
    id: Pk[Long],
    pitch: Int,
    pos: Int,
    melody_id: Long

) {
    def sameAs(n : Note) = (pitch==n.pitch && pos==n.pos)

}

object Note extends Magic[Melody] {
    final val intonationMax = 12;
    final val justIntonations = List(1,3,5,6,8,10);
}
