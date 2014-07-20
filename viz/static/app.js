var source = new EventSource('/stream');
var hash = {};
var width = 1200;
var height = 700;
//var words = [];
//var counts = [];
// hack count to update top words
var updates = 0;
var UPDATE_RESET = 10;

source.onmessage = function (event) {
  updates += 1;
  word = event.data.split(":")[0];
  count = event.data.split(":")[1];
  hash[word]=count;

  if(updates > UPDATE_RESET){
    // let's just remove svg since each svg element is changing...
    //d3.select("svg").remove();

    console.log("transition-non1");

    //var svgContainer = d3.select("body").append("svg")
    //.attr("width", width)
    //.attr("height", height);

    var text = svgContainer.selectAll("text")
    .data(Object.keys(hash), function(d){ return d; }) // data(values, key)
    .enter()
    .append("text")
    .text(function(d,i){ return d; });

    var textLabels = text
    .attr("x",function(d,i){ return hash[d]; })
    .attr("y",function(d,i){ return hash[d]; })
    .attr("font-family", "sans-serif")
    .attr("font-size", function(d,i){ return hash[d]+"px"; })
    .attr("fill",function(d,i){return "rgb("+
    Math.round(255/(1+Math.exp(-.001*hash[d])))+","+
    Math.round(255-255/(1+Math.exp(-.01*hash[d])))+","+
    Math.round(130-255/(1+Math.exp(-.01*hash[d])))+")";});

    //hash={};
    updates=0;
  }

};
