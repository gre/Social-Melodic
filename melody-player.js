if(typeof(melody)=='undefined') melody = {};
// Melodic Player

/**
 * canvas : the canvas dom object
 * melody.generator : the generator of notes
 * melody.notes : an Array of notes. each index of this array represent a loop instant.
 *           each value of this array is : a number OR an array of number.
 *           a number represent the pitch of the note.
 *           If one loop instant has an array of pitch, two pitches will be played at the same time.
 *          
 */
melody.Player = function(o) {
  var player = this;
  
  var canvas = o.canvas;
  var pitches = o.melody.notes;
  var generator = o.melody.generator;
  var bpm =  o&&o.bpm? o.bpm : 280;
  var nbPitch = o&&o.nbPitch? o.nbPitch : 16;
  var nbLoop = pitches.length;
  var paused = true;
  
  player.play = function() {
    paused = false;
  }
  player.stop = function() {
    paused = true;
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
  
  for(var i=0; i<nbLoop; ++i) {
    (function(index){
      generator.createNote(pitches[index], function(res) {
        notes[index] = res;
      });
    }(i));
  }
  
  var triggerLoop = function(loop) {
    if(notes[loop]) notes[loop].play();
  }
  
  player.processing = new Processing(canvas, function($p) {
    $p.externals.sketch.options.isTransparent = true;
    
    // States
    var cursor; 
    var currentLoop;
    
    // layout
    var width = 500;
    var height = 500;
    var playCenterRadius =  100;
    var ambiantSpace =  0; // Future
    var stepHauteur =  18;
    var bassSpace =  10; // Future
    var bass =  playCenterRadius + ambiantSpace + stepHauteur*(nbPitch+1);
    
    var startTime;
    
    $p.setup = function() {
      $p.size(width, height);
      $p.strokeWeight(0);
      $p.frameRate(30);
      cursor = 0;
      currentLoop = 0;
      startTime = new Date().getTime();
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
      var dx = $p.width/2-$p.mouseX;
      var dy = $p.height/2-$p.mouseY;
      return (playCenterRadius*playCenterRadius) > 4*(dx*dx+dy*dy);
    }
    
    var drawPlayerControl = function() {
      var hover = playControlHover();
      var centerX=$p.width/2, centerY=$p.height/2;
      $p.noStroke();
      if(hover)
        $p.fill(150, 150, 200);
      else
        $p.fill(200, 200, 250);
      $p.ellipse(centerX, centerY, playCenterRadius, playCenterRadius);
      $p.strokeWeight(2);
      if(hover) {
        $p.fill(190, 190, 250);
        $p.stroke(200, 200, 255);
      }
      else {
        $p.fill(170, 170, 230);
        $p.stroke(150, 150, 200);
      }
      
      if(!paused)
        $p.rect(centerX-20, centerY-20, 40, 40);
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
      $p.background(240);
      
      var centerX =  $p.width/2,centerY =  $p.height/2;
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
      $p.stroke(240);
      for(var i = 0;  i<nbLoop;  ++i) {
        var step =  i*loopStepRad;
        var x =  centerX + (bass/2)*Math.cos(step);
        var y =  centerY + (bass/2)* Math.sin(step);
        $p.line(centerX, centerY, x, y);
      }
      $p.strokeWeight(1);
      $p.stroke(210);
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
}