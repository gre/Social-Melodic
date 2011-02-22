if(typeof(melody)=='undefined') melody = {};
// Melodic Player

/**
 * bpm : loop per minute
 * nbPitch : number of pitches (notes)
 * canvas : the canvas dom object
 * width and height : player dimensions
 * melody.generator : the generator of notes
 * melody.notes : an Array of notes. each index of this array represent a loop instant.
 *           each value of this array is : a number OR an array of number.
 *           a number represent the pitch of the note.
 *           pitches are values in the equal temperament (12-TET). pitch = 12*octave+tone
 *           If one loop instant has an array of pitch, pitches will be played at the same time.
 */
melody.Player = function(o) {
  var player = this;
  
  var canvas = o.canvas;
  var background = o.background ? o.background : [240,240,240];
  var width = o.width ? o.width : 450;
  var height = o.height ? o.height : 450;
  var pitches = o.melody.notes;
  var generator = o.melody.generator;
  var bpm =  o&&o.bpm? o.bpm : 280;
  var nbPitch = o&&o.nbPitch? o.nbPitch : 16;
  var nbLoop = pitches.length;
  var paused = true;
  
  player.play = function() {
    paused = false;
    return this;
  }
  player.stop = function() {
    paused = true;
    return this;
  }
  
  // Clean pitches
  for(var i=0; i<nbLoop; ++i) {
    var p = pitches[i];
    var ens = [];
    if(p!==null) {
      if(typeof(p)=="number")
        ens = [p];
      else if(p.length) {
        for(var j=0; j<p.length; ++j) {
          if(typeof(p[j])=="number" && p[j]>=0 && p[j]<nbPitch)
            ens.push(p[j]);
        }
      }
    }
    pitches[i] = ens;
  }
  
  
  var notes = [];
  
  var count = 0;
  for(var i=0; i<nbLoop; ++i) {
    (function(index){
      generator.createNote(pitches[index], function(res) {
        notes[index] = res;
        if(++count == nbLoop) {
          onLoad();
        }
      });
    }(i));
  }
  
  var triggerLoop = function(loop) {
    $(canvas).trigger('loop', [loop]);
    if(notes[loop]) notes[loop].play();
  }
  
  
  var callbacks = [];
  var loaded = false;
  player.load = function(callback){
    if(loaded) callback();
    else callbacks.push(callback);
    return this;
  }
  var onLoad = function(){
    
    player.processing = new Processing(canvas, function($p) {
      $p.externals.sketch.options.isTransparent = true;
      
      // States
      var cursor; 
      var currentLoop;
      
      // layout
      var playCenterRadius =  80;
      var ambiantSpace =  0; // Future
      var bassSpace =  0; // Future
      var borderSpace = 4;
      
      var stepHauteur = Math.floor( (Math.min(width, height) - playCenterRadius - ambiantSpace - bassSpace - borderSpace) / (nbPitch+1));
      var bass =  playCenterRadius + ambiantSpace + stepHauteur*(nbPitch+1);
      
      var startTime;
      
      $p.setup = function() {
        $p.size(width, height);
        $p.strokeWeight(0);
        $p.frameRate(30);
        cursor = 0;
        currentLoop = 0;
        startTime = new Date().getTime();
        $p.background(background[0], background[1], background[2]);
        drawEnv();
      };
      
      $p.draw = function() {
        if(!paused) {
          drawEnv();
          drawCursor();
        
          if(cursor>currentLoop) {
            triggerLoop(currentLoop % nbLoop);
            currentLoop ++;
          }
          cursor = ((new Date().getTime()-startTime)*bpm)/60000;
        }
      }
      
      $p.mousePressed = function() {
        if(playControlHover()) {
          if(paused) player.play();
          else player.stop();
          $p.setup();
        }
      }
      
      $p.mouseMoved = function() {
        drawPlayerControl();
      }
      
      var playControlHover = function() {
        var dx = $p.width/2 - $p.mouseX;
        var dy = $p.height/2 - $p.mouseY;
        return (playCenterRadius*playCenterRadius) > 4*(dx*dx+dy*dy);
      }
      
      var drawPlayerControl = function() {
        var hover = playControlHover();
        var centerX=$p.width/2, centerY=$p.height/2;
        $p.noStroke();
        $p.fill(210, 40, 40);
        $p.ellipse(centerX, centerY, playCenterRadius, playCenterRadius);
        if(hover) {
          $p.fill(255);
          $p.strokeWeight(2);
          $p.stroke(0);
        }
        else {
          $p.fill(250, 120, 120);
        }
        
        if(!paused)
          $p.rect(centerX-15, centerY-15, 30, 30);
        else
          $p.triangle( centerX-10, centerY-20, centerX-10, centerY+20, centerX+20, centerY );
      }
      
      var loopStepRad =  2*Math.PI/nbLoop;
      
      var drawCursor = function() {
        $p.strokeWeight(2);
        $p.stroke(210, 40, 40);
        var step =  cursor*loopStepRad;
        var x1 =  $p.width/2 + (playCenterRadius+ambiantSpace)*Math.cos(step) / 2;
        var y1 =  $p.height/2 + (playCenterRadius+ambiantSpace)* Math.sin(step) / 2;
        var x2 =  $p.width/2 + (bass/2)*Math.cos(step);
        var y2 =  $p.height/2 + (bass/2)* Math.sin(step);
        $p.line(x1, y1, x2, y2);
      }
      
      var drawNotes = function() {
        var centerX=$p.width/2, centerY=$p.height/2;
        $p.noStroke();
        for(var loop=0; loop<pitches.length; loop++) {
          var loopNotes = pitches[loop];
          var loopRad =  loop*loopStepRad;
          for(var i = 0; i<loopNotes.length; ++i) {
            var dist =  (playCenterRadius + ambiantSpace + stepHauteur*(1+loopNotes[i]))/2;
            var x =  centerX + dist*Math.cos(loopRad);
            var y =  centerY + dist*Math.sin(loopRad);
            var weight = 4;
            if(!paused && loop==Math.floor((cursor+0.5))%nbLoop) {
              $p.fill(240, 100, 100);
              var diff = Math.abs(Math.floor(cursor)+0.5-cursor);
              weight += diff>0.5 ? 0 : diff*20;
            }
            else {
              $p.fill(140, 100, 200);
            }
            $p.ellipse(x, y, weight, weight);
          }
        }
      }
      
      var drawEnv = function() {
        
        var centerX =  $p.width/2,centerY =  $p.height/2;
        // border
        $p.noStroke();
        $p.fill(210, 40, 40);
        $p.ellipse(centerX, centerY, bass+bassSpace+borderSpace, bass+bassSpace+borderSpace);
        // Bass area
        $p.noStroke();
        $p.fill(100);
        $p.ellipse(centerX, centerY, bass+bassSpace, bass+bassSpace);
        // Notes area
        $p.fill(255);
        $p.ellipse(centerX, centerY, bass, bass);
        // Lines
        $p.noFill();
        // Loop lines
        $p.strokeWeight(1);
        $p.stroke(240, 240, 250);
        for(var i = 0;  i<nbLoop;  ++i) {
          var step =  i*loopStepRad;
          var x =  centerX + (bass/2)*Math.cos(step);
          var y =  centerY + (bass/2)* Math.sin(step);
          $p.line(centerX, centerY, x, y);
        }
        $p.strokeWeight(1);
        $p.stroke(230, 230, 240);
        // Partition lines
        for(var i = 1;  i<=nbPitch;  ++i) {
          var dist =  playCenterRadius + ambiantSpace + stepHauteur*i;
          $p.ellipse(centerX, centerY, dist, dist);
        }
        // Ambient area
        $p.noStroke();
        $p.fill(200, 240, 200);
        $p.ellipse(centerX, centerY, playCenterRadius+ambiantSpace, playCenterRadius+ambiantSpace);
        
        drawPlayerControl();
        drawNotes();
      }
    });
    
    loaded = true;
    for(var c in callbacks) {
      callbacks[c]();
    }
    callbacks = [];
  }
}
