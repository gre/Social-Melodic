package controllers

import play._
import play.mvc._

import models._

object Application extends Controller {
    
    def index = Template
    
    def melodyComposer = Template
    def toneComposer = Template
    
    // TMP
    def random = Melody.getRandom().id.get.map( id => melody(id)).getOrElse(NotFound)
    
    def melody(id:Long) = Melody.findById(id)
                          .map( m => Template( 'melody -> m) )
                          .getOrElse(NotFound)
    def noMoreMelodies = Template
    def melodyNotFound = Template
    
    def vote(id:Long, vote:String) = random
}
