var source = new EventSource('/stream');
var hash = {};
var width = 1200;
var height = 700;

//update hash (associative array) with incoming word and count
source.onmessage = function (event) {
  county_id = event.data.split(":")[0];
  sentiment = event.data.split(":")[1]; 
  URLInfo = event.data.split(":")[2];
  hash[county_id]=sentiment;
  

};

var rateById = d3.map();
var us = d3.json("/static/us.json", function(error, us) {
  if (error) return console.error(error);
});

d3.tsv("/static/unemployment.tsv", function(error, d) {
  rateById.set(d.id, +d.rate);
});


//update function for visualization
var updateViz =  function(){
  //print console message
  console.log("Map-1" + JSON.stringify(d3.entries(hash)));

  var sentimentCountyData = d3.entries(hash);
  
  var quantize = d3.scale.quantize()
        .domain([0, .15])
        .range(d3.range(9).map(function(i) { return "q" + i + "-9"; }));

  var projection = d3.geo.albersUsa()
        .scale(1280)
        .translate([width / 2, height / 2]);

  var path = d3.geo.path()
        .projection(projection);

  var svg = d3.select("body").append("svg")
        .attr("width", width)
        .attr("height", height);


  svg.append("g")
      .attr("class", "counties")
    .selectAll("path")
      .data(topojson.feature(us, us.objects.counties).features)
    .enter().append("path")
      .attr("class", function(d) { return quantize(rateById.get(d.id)); })
      .attr("d", path);

  svg.append("path")
      .datum(topojson.mesh(us, us.objects.states, function(a, b) { return a !== b; }))
      .attr("class", "states")
      .attr("d", path);

};

// run updateViz at #7000 milliseconds, or 7 second
window.setInterval(updateViz, 7000);
