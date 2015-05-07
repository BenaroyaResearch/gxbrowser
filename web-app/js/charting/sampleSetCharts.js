function getBase() {
  var curPath = location.pathname;
  return curPath.substring(0, curPath.indexOf("/",1));
}

function drawCxBoxplot(args, chartOptions) {
  args["chartType"] = "canvasXpress";
  $.getJSON(getBase()+'/charts/getBoxplotDataAsGroups', args, function(json) {
    var chartContent = []
    $.each(json, function(groupId, val) {
      chartContent.push('<canvas id="Group_'+groupId+'"></canvas>');
    });
    $("#chart").html(chartContent.join(''));
    $.each(json, function(groupId, val) {
      $("#Group_"+groupId).chartify('drawCxBoxplot', $.extend({ data: val["data"], groupBy: ["Probe"] }, chartOptions));
    });
  });
}

function drawCxHistogram(args, chartOptions) {
  args["chartType"] = "canvasXpress";
  $.getJSON(getBase()+'/charts/getHistogramDataAsGroups', args, function(json) {
    var chartContent = []
    $.each(json, function(groupId, val) {
      chartContent.push('<canvas id="Group_'+groupId+'"></canvas>');
    });
    $("#chart").html(chartContent.join(''));
    $.each(json, function(groupId, val) {
      $("#Group_"+groupId).chartify('drawCxHistogram', $.extend({ data: val["data"], colors: val["colors"] }, chartOptions));
    });
  });
}

function drawCxLineChart(args, chartOptions) {
  args["chartType"] = "canvasXpress";
  if (!$("#chart").is("canvas")) {
    $("#chart").html('<canvas id="cxLineChart"></canvas>');
  }
  $.getJSON(getBase()+'/charts/getLineChartData', args, function(json) {
    $("#chart").chartify('drawCxLineChart', $.extend({ renderTo: "cxLineChart", data: json[0] }, chartOptions));
  });
}

function drawHistogram(args, chartOptions) {
  args["chartType"] = "highcharts";
  $.getJSON(getBase()+'/charts/getHistogramDataAsGroups', args, function(json) {
    var chartContent = []
    $.each(json, function(groupId, val) {
      chartContent.push('<div id="Group_'+groupId+'"></div>');
    });
    $("#chart").html(chartContent.join(''));
    $.each(json, function(groupId, val) {
      $("#Group_"+groupId).chartify('drawHistogram', $.extend({ data: val["data"], colors: val["colors"] }, chartOptions));
    });
  });
}

function drawLineChart(args, chartOptions) {
  args["chartType"] = "highcharts";
  $.getJSON(getBase()+'/charts/getLineChartData', args, function(json) {
    $("#chart").chartify('drawLineChart', $.extend({ data: json }, chartOptions));
  });
}

function drawBipolarChart(args, chartOptions) {
  args["chartType"] = "highcharts";
  $("#chart").chartify('drawBipolarChart', { data: [ { name: "Negative", data: [-1,-2,-3,-4,-5] }, { name: "Positive", data: [1,2,3,4,5] }], showLegend: true });
}

function drawD3Boxplot(args, chartOptions) {
  args["chartType"] = "d3";
  $.getJSON(getBase()+'/charts/getBoxplotDataAsGroups', args, function(json) {
    var chartContent = []
    $.each(json, function(groupId, val) {
      chartContent.push('<div id="Group_'+groupId+'"></div>');
    });
    $("#chart").html(chartContent.join(''));
    $.each(json, function(groupId, val) {
      $("#Group_"+groupId).chartify('drawD3Boxplot', $.extend({ data: val["data"], n: val["minMax"]["n"], min: val["minMax"]["min"], max: val["minMax"]["max"] }, chartOptions));
    });
  });
}