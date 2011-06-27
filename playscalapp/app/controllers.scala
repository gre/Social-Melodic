package controllers

import play._
import play.mvc._

import models._

object Application extends Controller {
    
    def index = Template
    
    def melodyComposer = Template
    def toneComposer = Template
    def random = melody(1L)
    
    def melody(id:Long) = Template( 'melody -> Melody.getFirst())
    def noMoreMelodies = Template
    def melodyNotFound = Template
    
    def vote(id:Long, vote:String) = random
    
    
}
