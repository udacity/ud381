var source = new EventSource('/stream');
var hash = {};
var width = 1200;
var height = 700;

source.onmessage = function (event) {
  word = event.data.split(":")[0];
  count = event.data.split(":")[1];
  if(!skip(word)){
    hash[word]=count;
  }
};

var updateViz = function () {
    d3.select("svg").remove();

    var svgContainer = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

    var text = svgContainer.selectAll("text")
    .data(d3.entries(hash), function(d){ return d.key; })
    .enter()
    .append("text")
    .text(function(d,i){ return d.key; });

    var textLabels = text
    .attr("x",function(d,i){ return .5*d.value; })
    .attr("y",function(d,i){ return 1.5*d.value; })
    .attr("font-family", "sans-serif")
    .attr("font-size", function(d,i){ return d.value+"px"; })
    //.attr("fill", function(d, i) { return colors(d.value); });
    //comment following lines and uncomment previous for colors function
    .attr("fill",function(d,i){return "rgb("+
    Math.round(255/(1+Math.exp(-.001*d.value)))+","+
    Math.round(255-255/(1+Math.exp(-.01*d.value)))+","+
    Math.round(130-255/(1+Math.exp(-.01*d.value)))+")";});

    console.log("Array-1" + JSON.stringify(d3.entries(hash)));
};

//update display every #1000 milliseconds
window.setInterval(updateViz, 1000);

//clean list, can be added to word skipping bolt
var skipList = ["https","follow","1","2","please","following","followers","fucking"];

var skip = function(tWord){
  for(var i=0; i<skipList.length; i++){
    if(tWord === skipList[i]){
      return true;
    }
  }
  return false;
};
