package controllers

import play._
import play.mvc._

import models._

object Application extends Controller {
    
    def index = {
        Family.bootstrap("test", 3, 16, 20)
        Template
    }
    
    def melodyComposer = Template
    def toneComposer = Template
    
    // TMP
    def random = melody( Melody.find().list().map( (m:Melody) => m.id.apply() ).reduceLeft( (a,b) => if(Math.random>0.5) a else b ))
    
    def melody(id:Long) = Melody.findById(id)
                          .map( m => Template( 'melody -> m) )
                          .getOrElse(NotFound)
    def noMoreMelodies = Template
    def melodyNotFound = Template
    
    def vote(id:Long, vote:String) = random
}
