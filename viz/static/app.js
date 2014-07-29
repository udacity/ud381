var source = new EventSource('/stream');
var hash = {};
var width = 1200;
var height = 700;

source.onmessage = function (event) {
  word = event.data.split(":")[0];
  count = event.data.split(":")[1];
  hash[word]=count;
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
    .attr("x",function(d,i){ return d.value; })
    .attr("y",function(d,i){ return d.value; })
    .attr("font-family", "sans-serif")
    .attr("font-size", function(d,i){ return d.value+"px"; })
    .attr("fill", function(d, i) { return colors(d.value); });
    //or control color directly, remove previous line and uncomment
    //.attr("fill",function(d,i){return "rgb("+
    //Math.round(255/(1+Math.exp(-.001*d.value)))+","+
    //Math.round(255-255/(1+Math.exp(-.01*d.value)))+","+
    //Math.round(130-255/(1+Math.exp(-.01*d.value)))+")";});

    console.log("Array-1" + JSON.stringify(d3.entries(hash)));
};

//update display every #1000 milliseconds
window.setInterval(updateViz, 1000);
