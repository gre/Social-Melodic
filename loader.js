// wait everything is loaded before callback
var Loader = function(callback){
  var mainStarted = false;
  var main = function(){
    if(mainStarted) return;
    mainStarted = true;
    callback();
  }
  $(document).ready(function(){
    if(!window.Worker || melody.webWorkerReady)
      main();
    else {
      $(document).one('webWorkerReady', main);
      setTimeout(main, 800); // Max time before web worker state ignoring
    }
  })
};