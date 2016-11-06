var migrate = function(json) {
  var obj = JSON.parse(json);
  obj.version=2
  return JSON.stringify(obj);
};