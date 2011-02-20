importScripts('jsfx/audio.js', 'jsfx/jsfx.js', 'melody-utils.js')

onmessage = function(e) {
  var arr = e.data;
  if(typeof(arr[0].length)=="undefined")
    arr = [arr];
  melody.utils.mixPrimitives(arr, function(data){
    postMessage(audio.makeData64(data));
  })
}
