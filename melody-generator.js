if(typeof(melody)=='undefined') melody = {};

// Test workers
(function(){
  melody.webWorkerReady = false;
  if(!!window.Worker) {
    var worker = new Worker("worker.js");
    if(worker) {
      // small test
      worker.onmessage = function(a){
        if(a.data && a.data.indexOf('data:audio')==0) {
          melody.webWorkerReady = true;
          $(document).trigger('webWorkerReady');
        }
        worker.terminate();
      };
      worker.onerror = function(arguments){
        melody.webWorkerReady = false;
        worker.terminate();
      };
      worker.postMessage(["sine",0,0,0,0,0,0,20,20,20,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]);
    }
  }
}());

// Melody generator
melody.Generator = function(primitives) {
  
  var freqScale = function(refFreq, octave, pitch) {
    return refFreq * Math.pow(2, octave-3+(pitch-10)/12);
  }
  var pitchToFreq = function(pitch) {
      var octave = 4;
      while(pitch>11) {
        pitch -= 12;
        octave ++;
      }
      return freqScale(440, octave, pitch);
  }
  
  
  var applyFreqToPrimitives = function(freq) {
    for(var p=0; p<primitives.length; ++p) {
      primitives[p][8] = freq;
    }
  }
  
  var mixPrimitives = function(all, callback) {
    if(melody.webWorkerReady) {
      var worker = new Worker("worker.js");
      worker.onmessage = function(e) {
        callback(audio.data64ToAudio(e.data));
      }
      worker.postMessage(all);
    }
    else {
      melody.utils.mixPrimitives(all, function(res){
        callback(audio.make(res));
      });
    }
  }

  // number all notes from 0 to n : 
  // do=0 re=1 mi=2 fa sol la si do re mi fa sol la si ...
  this.createNote = function(pitches, callback) {
    if(typeof(pitches)=="number") {
      pitches = [pitches];
    }
    if(pitches.length==0) {
      return callback(null);
    }
    var all = [];
    for(var i=0; i<pitches.length; ++i) {
      var pitch = pitches[i];
      applyFreqToPrimitives(pitchToFreq(pitch));
      for(var p=0; p<primitives.length; ++p) {
        var copy = [];
        var primitive = primitives[p];
        for(var j=0; j<primitive.length; ++j)
          copy[j] = primitive[j];
        all.push(copy);
      }
    }
    mixPrimitives(all, callback);
  }
  
}