if(typeof(melody)=='undefined') melody = {};
if(!melody.utils) melody.utils = {};
(function(melodyutils){
  
  var arrayToParams = function(pararr){
    var params = {};
    var len = jsfx.Parameters.length;
    for(var i = 0; i < len; i++){
        params[jsfx.Parameters[i].id] = pararr[i];
    }
    return params;
  }
  
  var generate = melodyutils.generate = function(params, callback) {
    callback(jsfx.generate(arrayToParams(params)));
  }
  
  var mixArrays = melodyutils.mixArrays = this.mixArrays = function() {
    if(arguments.length==0) return;
    if(arguments.length==1) return arguments[0];
    var max = 0;
    for(var a=0; a<arguments.length; ++a)
      max = Math.max(max, arguments[a].length);
    var data = new Array(max);
    for(var d=0; d<max; ++d) {
      var sum = 0;  
      for(var a=0; a<arguments.length; ++a) {
        var dataArg = arguments[a];
        sum += (d<dataArg.length ? dataArg[d] : 0);
      }
      data[d] = sum/arguments.length;
    }
    return data;
  }
  
  var mixPrimitives = melodyutils.mixPrimitives = function(all, callback) {
    var arr = [];
    var nb = 0;
    var length = all.length;
    for(var i=0; i<length; ++i) {
      generate(all[i], function(result){
        ++nb;
        arr.push(result);
        if(nb==length) {
          callback(mixArrays.apply(this, arr));
        }
      });
    }
  }
}(melody.utils));