/**
 * Chartify
 *
 */
(function($) {

  var charts = {};

  var copyButton = '<button class="copy-chart generic-button generic-button-left ui-button ui-corner-all ui-button-icon-primary">' +
    '<span class="ui-button-icon-primary ui-icon ui-icon-copy"></span>' +
    '</button>';
  var saveButton = '<button class="save-chart generic-button generic-button-left ui-button ui-corner-all ui-button-icon-primary">' +
    '<span class="ui-button-icon-primary ui-icon ui-icon-disk"></span>' +
    '</button>';

  function getBase() {
    var curPath = location.pathname;
    return curPath.substring(0, curPath.indexOf("/",1));
  }

  $('.save-chart').live('click', function() {
    var chartId = $(this).parent().attr("id").substring(8);
    $("#"+chartId).chartify('saveChart');
  });

  $('.copy-chart').live('click', function() {
    var chartId = $(this).parent().attr("id").substring(8);
    $("#"+chartId).chartify('copyToClipboard');
  });

  var defaultSaveSVGHandler = function(svg, filename) {
    $.post(getBase()+'/charts/saveImg', { svg: svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      window.location.href = location.concat("?filename=").concat(filename);
    });
  };

  var defaultCopyHandler = function(id) {
    var html = "<div id='clipboard-dialog' class='modal dialog' style='width: 300px; z-index: 15001;'>" +
      "<div class='modal-body'><p>Highlight the image and copy using Ctrl+C. Press Esc to cancel.</p><p style='text-align:center;'><img src='"+getBase()+"/tempImages/temp"+id+".png'/></p></div></div>";
    $("body").append(html);
    var ctrlDown = false;
    var copy = false;
    var ctrlKey = 91, cKey = 67, escKey = 27;
    $(document).keydown(function(e) {
      var code = (e.keyCode ? e.keyCode : e.which);
      if (code == ctrlKey)
      {
        ctrlDown = true;
      }
      else if (ctrlDown && (code == cKey))
      {
        copy = true;
      }
    }).keyup(function(e) {
        var code = (e.keyCode ? e.keyCode : e.which);
        if (code == ctrlKey)
        {
          ctrlDown = false;
        }
        if (copy || code == escKey)
        {
          $("div#clipboard-dialog").detach();
          $.post(getBase()+'/charts/deleteImg', { id: id });
          copy = false;
        }
    });
  };

  var defaultCopySVGHandler = function(svg) {
    $.post(getBase()+'/charts/saveImg', { svg: svg }, function(id) {
      defaultCopyHandler(id);
    });
  };

  var defaultSaveCanvasHandler = function(imgData) {
    $.post(getBase()+'/charts/saveImg', { img: imgData }, function(id) {
      window.location.href = getBase()+'/charts/downloadImg/'+id;
    });
  };

  var defaultCopyCanvasHandler = function(imgData) {
    $.post(getBase()+'/charts/saveImg', { img: imgData }, function(id) {
      defaultCopyHandler(id);
    });
  };

  var defaultCxMouseover = function(data, event) {
    var elt = event.currentTarget;
    var sampleId = data["y"]["smps"][0];
    var value = data["y"]["data"][0];
    var text = sampleId + ": " + value;
    $(elt).qtip({
      content: {
        text: text
      },
      position: {
        viewport: $(document),
        my: 'bottom right',
        at: 'top center',
        adjust: {
          method: 'flip flip'
        }
      },
      show: {
        solo: true,
        ready: true
      },
      hide: {
        event: 'click mouseleave'
      },
      style: {
        classes: 'ui-tooltip-tipsy'
      }
    });
  };

  var defaultCxBoxplotMouseover = function(data, event) {
    var elt = event.currentTarget;
    var groupId = data["w"]["vars"][0];
    var text = groupId + ":" +
      "<br/>N: " + data["w"]["n"] +
      "<br/>Median: " + data["w"]["median"] +
      "<br/>Quartile 1: " + data["w"]["qtl1"] +
      "<br/>Quartile 3: " + data["w"]["qtl3"] +
      "<br/>Inter-quartile 1: " + data["w"]["iqr1"] +
      "<br/>Inter-quartile 3: " + data["w"]["iqr3"];
    $(elt).qtip({
      content: {
        text: text
      },
      position: {
        viewport: $(document),
        my: 'bottom right',
        at: 'top center',
        adjust: {
          method: 'flip flip'
        }
      },
      show: {
        solo: true,
        ready: true
      },
      hide: {
        event: 'click mouseleave'
      },
      style: {
        classes: 'ui-tooltip-tipsy'
      }
    });
  };

  var defaultHighchartsTooltip = function() {
    if (this.point.name)
      return "<strong>"+this.point.name+"</strong>: "+this.y;
    else
      return this.y;
  };

  // defaults
  var highchartsDefaults = {
    chartWidth: null,
    chartHeight: null,
    chartBorderWidth: 0,
    data: [],
    colors: ['#359636','#359636','#359636','#359636','#359636',
      '#359636','#359636','#359,636','#359636','#359636'],
    title: null,
    yTitle: null,
    xTitle: null,
    titleStyle: null,
    axisTitleStyle: null,
    animation: false,
    showExport: true,
    showLegend: false,
    xCategories: [],
    yCategories: [],
    xTickWidth: 1,
    yTickWidth: 1,
    showXLabels: true,
    showYLabels: true,
    showYFirstLabel: true,
    showYLastLabel: true,
    showXFirstLabel: true,
    showXLastLabel: true,
    xLabelStyle: null,
    yLabelStyle: null,
    xLabelRotation: 0,
    yLabelRotation: 0,
    xLabelAlign: "center",
    yLabelAlign: "center",
    xTickInterval: null,
    yTickInterval: null,
    yStartOnTick: true,
    xLineWidth: 1,
    yLineWidth: 0,
    xGridLineWidth: 1,
    yGridLineWidth: 1,
    minX: null,
    maxX: null,
    minY: null,
    maxY: null,
    minPointLength: 5,
    pointWidth: null,
    lineWidth: 2,
    markerRadius: 1,
    colorByPoint: true,
    shadow: true,
    columnPadding: 1,
    borderWidth: 1,
    tooltip: { enabled: true, formatter: defaultHighchartsTooltip },
    mouseover: function(event) { },
    mouseout: function(event) { },
    click: function(event) { }
  };

  var histogramDefaults = {
    xTickWidth: 0,
    yTickWidth: 0,
    showXLabels: false,
    showYLabels: false,
    xGridLineWidth: 0,
    yGridLineWidth: 0
  };

  var lineChartDefaults = {
    xGridLineWidth: 0,
    yGridLineWidth: 0,
    showYLabels: false,
    minY: null
  };

  var bipolarChartDefaults = {
    colors: ["#E4317F", "#488AC7"],
    showXLabels: false,
    showYLabels: false,
    xTickWidth: 0,
    xGridLineWidth: 0
  };

  var canvasXpressDefaults = {
    data: { y: { vars: [], smps: [], data: [] } },
    colors: ['rgb(53,150,54)','rgb(53,150,54)','rgb(53,150,54)','rgb(53,150,54)','rgb(53,150,54)',
      'rgb(53,150,54)','rgb(53,150,54)','rgb(53,150,54)','rgb(53,150,54)','rgb(53,150,54)'],
    showExport: true,
    showLegend: false,
    showSampleNames: true,
    showVariableNames: true,
    mouseover: defaultCxMouseover,
    mouseout: function(data, event) { },
    click: function(data, event) { }
  };

  var d3Defaults = {
    chartHeight: 300,
    chartWidth: 500,
    mouseover: function() {},
    mouseout: function() {},
    boxPlotOptions: { pointWidth: 3, strokeWidth: 1 }
  };

  // Returns a function to compute the interquartile range.
  var iqrFunc = function iqr(k) {
    return function(d, i) {
      var q1 = d.quartiles[0],
        q3 = d.quartiles[2],
        iqr = (q3 - q1) * k,
        i = -1,
        j = d.length;
      while (d[++i] < q1 - iqr);
      while (d[--j] > q3 + iqr);
      return [i, j];
    };
  };

  var initHighchartsOptions = function(options, id) {
    if (!options["renderTo"])
    {
      options["renderTo"] = id;
    }
    var settings = $.extend({}, highchartsDefaults, options);
    Highcharts.setOptions({
      chart: { width: settings["chartWidth"], height: settings["chartHeight"], borderWidth: settings["chartBorderWidth"] },
      title: { text: settings["title"], style: settings["titleStyle"] },
      credits: { enabled: false },
      exporting: { enabled: false },
      legend: { enabled: settings["showLegend"] },
      colors: settings["colors"],
      tooltip: settings["tooltip"],
      xAxis: {
        categories: settings["xCategories"],
        title: { text: settings["xTitle"], style: settings["axisTitleStyle"] },
        showFirstLabel: settings["showXFirstLabel"],
        showLastLabel: settings["showXLastLabel"],
        labels: {
          enabled: settings["showXLabels"],
          style: settings["xLabelStyle"],
          align: settings["xLabelAlign"],
          rotation: settings["xLabelRotation"] },
        tickInterval: settings["xTickInterval"],
        tickWidth: settings["xTickWidth"],
        gridLineWidth: settings["xGridLineWidth"]
      },
      yAxis: {
        categories: settings["yCategories"],
        title: { text: settings["yTitle"], style: settings["axisTitleStyle"] },
        startOnTick: settings["yStartOnTick"],
        showFirstLabel: settings["showYFirstLabel"],
        showLastLabel: settings["showYLastLabel"],
        labels: {
          enabled: settings["showYLabels"],
          style: settings["yLabelStyle"],
          align: settings["yLabelAlign"],
          rotation: settings["yLabelRotation"] },
        tickInterval: settings["yTickInterval"],
        tickWidth: settings["yTickWidth"],
        gridLineWidth: settings["yGridLineWidth"]
      },
      plotOptions: {
        column: { columnPadding: settings["columnPadding"] },
        series: {
          animation: settings["animation"],
          colorByPoint: settings["colorByPoint"],
          lineWidth: settings["lineWidth"],
          marker: { radius: settings["markerRadius"] }
        }
      }
    });

    return settings;
  };

  var initCanvasXpressOptions = function(options, id) {
    if (!options["renderTo"])
    {
      options["renderTo"] = id;
    }
    return $.extend({}, canvasXpressDefaults, options);
  };

  var initD3Options = function(options, id) {
    if (!options["renderTo"])
    {
      options["renderTo"] = "#"+id;
    }
    return $.extend({}, d3Defaults, options);
  };

  var getCanvasParent = function(currentNode) {
    var canvasParent = currentNode;
    if (currentNode.is("canvas")) {
      canvasParent = currentNode.parent();
    }
    return canvasParent;
  };

  var methods = {
    drawHistogram: function(options) {
      var settings = initHighchartsOptions($.extend({}, histogramDefaults, options), this.attr("id"));
      var chart = new Highcharts.Chart({
        chart: {
          renderTo: settings["renderTo"],
          defaultSeriesType: 'column'
        },
        colors: settings["colors"],
        plotOptions: {
          column: {
            minPointLength: settings["minPointLength"]
          },
          series: {
            borderWidth: settings["borderWidth"],
            shadow: settings["shadow"],
            borderColor: '#ffffff',
            colorByPoint: settings["colorByPoint"],
            pointPadding: settings["pointPadding"],
            groupPadding: settings["groupPadding"],
            pointWidth: settings["pointWidth"],
            pointInterval: 1,
            point: {
              events: {
                mouseOver: settings["mouseover"],
                mouseOut: settings["mouseout"],
                click: settings["click"]
              }
            }
          }
        },
        xAxis: {
          lineWidth: settings["xLineWidth"],
          labels: {
            align: settings["xLabelAlign"]
          }
        },
        yAxis: {
          lineWidth: settings["yLineWidth"],
//          offset: 5,
          labels: {
            align: settings["yLabelAlign"]
          },
          min: settings["minY"],
          max: settings["maxY"],
          endOnTick: false
        },
        series: settings["data"]
      });
      if (settings["showExport"])
      {
        $('<div id="buttons-'+settings["renderTo"]+'" class="button-set">'+saveButton+copyButton+'</div>').prependTo($('#'+settings["renderTo"]));
      }
      charts[settings["renderTo"]] = chart;
    },
    drawLineChart: function(options) {
      var settings = initHighchartsOptions($.extend({}, lineChartDefaults, options), this.attr("id"));
      var chart = new Highcharts.Chart({
        chart: {
          renderTo: settings["renderTo"],
          defaultSeriesType: 'line'
        },
        colors: settings["colors"],
        series: settings["data"],
        yAxis: {
          tickWidth: settings["yTickWidth"],
          min: settings["minY"],
          max: settings["maxY"],
          endOnTick: false,
          showLastLabel: settings["showYLastLabel"]
        }
      });
      if (settings["showExport"])
      {
        $('<div id="buttons-'+settings["renderTo"]+'" class="button-set">'+saveButton+copyButton+'</div>').prependTo($('#'+settings["renderTo"]));
      }
      charts[settings["renderTo"]] = chart;
    },
    drawBipolarChart: function(options) {
      var settings = initHighchartsOptions($.extend({}, bipolarChartDefaults, options), this.attr("id"));
      var chart = new Highcharts.Chart({
        chart: {
          renderTo: settings["renderTo"],
          defaultSeriesType: 'bar'
        },
        xAxis: [{
          reversed: false
        }, {
          opposite: true,
          reversed: false,
          linkedTo: 0
        }],
        yAxis: {
          gridLineDashStyle: 'Dash',
          gridLineColor: '#DDDDDD'
        },
        plotOptions: {
          series: {
            stacking: 'normal'
          }
        },
        series: settings["data"]
      });
      if (settings["showExport"])
      {
        $('<div id="buttons-'+settings["renderTo"]+'" class="button-set">'+saveButton+copyButton+'</div>').prependTo($('#'+settings["renderTo"]));
      }
      charts[settings["renderTo"]] = chart;
    },
    drawCxHistogram: function(options) {
      var settings = initCanvasXpressOptions(options, this.attr("id"));
      var chart = new CanvasXpress({
        renderTo: settings["renderTo"],
        data: settings["data"],
        config: {
          graphType: 'Bar',
          graphOrientation: 'vertical',
          smpHairline: false,
          autoExtend: true,
          foreground: 'rgb(255,255,255)',
          showSampleNames: settings["showSampleNames"],
          showVariableNames: settings["showVariableNames"],
          showLegend: settings["showLegend"],
          transparency: 0.8,
          colorBy: 'variable',
          axisTickColor: 'rgb(255,255,255)',
          scaleTickFontFactor: 0.2,
          xAxisTickColor: 'rgb(255,255,255)',
          colorScheme: 'user',
          colors: settings["colors"],
          blockSeparationFactor: 0.1,
          disableDragEvents: true,
          disableResizerEvents: true
        },
        events: {
          mouseover: settings["mouseover"],
          click: settings["click"]
        }
      });
      if (settings["showExport"])
      {
        $('<div id="buttons-'+settings["renderTo"]+'" class="button-set">'+saveButton+copyButton+'</div>').insertBefore($('#'+settings["renderTo"]).parent());
      }
      charts[settings["renderTo"]] = chart;
    },
    drawCxLineChart: function(options) {
      var settings = initCanvasXpressOptions(options, this.attr("id"));
      var chart = new CanvasXpress({
        renderTo: settings["renderTo"],
        data: settings["data"],
        config: {
          graphType: 'Line',
          smpHairline: false,
          graphOrientation: 'vertical',
          transparency: 0.8,
          showLegend: settings["showLegend"],
          autoExtend: true,
          showSampleNames: settings["showSampleNames"],
          showVariableNames: settings["showVariableNames"],
          colorBy: 'user',
          colors: settings["colors"],
          disableDragEvents: true
        }
      });
      if (settings["showExport"])
      {
        $('<div id="buttons-'+settings["renderTo"]+'" class="button-set">'+saveButton+copyButton+'</div>').insertBefore($('#'+settings["renderTo"]).parent());
      }
      charts[settings["renderTo"]] = chart;
    },
    drawCxBoxplot: function(options) {
      if (!options["mouseover"]) {
        options["mouseover"] = defaultCxBoxplotMouseover;
      }
      var settings = initCanvasXpressOptions(options, this.attr("id"));
      var chart = new CanvasXpress({
        renderTo: settings["renderTo"],
        data: settings["data"],
        config: {
          graphType: 'Boxplot',
          smpHairline: false,
          graphOrientation: 'vertical',
          transparency: 0.8,
          colorScheme: 'user',
          colors: settings["colors"],
          autoExtend: true,
          showSampleNames: settings["showSampleNames"],
          showVariableNames: settings["showVariableNames"],
          showLegend: settings["showLegend"],
          disableDragEvents: true
        },
        events: {
          mouseover: settings["mouseover"],
          click: settings["click"]
        }
      });
      chart.groupSamples(settings["groupBy"], 'iqr');
      chart.draw();
      if (settings["showExport"])
      {
        $('<div id="buttons-'+settings["renderTo"]+'" class="button-set">'+saveButton+copyButton+'</div>').insertBefore($('#'+settings["renderTo"]).parent());
      }
      charts[settings["renderTo"]] = chart;
    },
    drawD3Boxplot: function(options) {
      var maxBoxWidth = 150;
      var browser = $.browser;
      var settings = initD3Options(options, this.attr("id"));
      var elt = settings.renderTo;
      var labels = settings.labels;
      var colors = settings.colors;
      var data = settings.data;

      var chartNode = $(elt);
      var oldHtml = chartNode.html();
      var labelWidths = [], maxWidth = 0;
      d3.max(labels, function(d, i) {
        labelWidths[i] = chartNode.html(d).textWidth();
        maxWidth = Math.max(maxWidth, labelWidths[i]);
      });
      chartNode.html(oldHtml);

      var axisM = 80;
      var chartWidth = settings.chartWidth - 60;
      var plotWidth = settings.showLegend ? chartWidth - maxWidth - 25 : chartWidth;
      var chartHeight = settings.chartHeight;
      var axisHeight = chartHeight - 250;
      var w = (plotWidth - axisM - 20) / data.length, // box width
        h = chartHeight - 350, // box height
        m = [50, 20, 10, 20]; // top right bottom left

      d3.max(data, function(d, i) {
        d.x = i;
      });

      var boxWidth = Math.min(maxBoxWidth, w - m[1]);
      var x = function(d) {
        return (d.x === 0 ? axisM + m[3]/2 : axisM + m[3]/2 + (d.x * (boxWidth + m[3])));
      };

      var svg = d3.select(elt)
        .append("svg:svg")
          .attr("width", chartWidth)
          .attr("height", chartHeight - 190);

      // add a background
      svg.append("svg:rect")
          .attr("x", 0)
          .attr("width", chartWidth)
          .attr("height", chartHeight)
          .style("fill", "#FFFFFF");

      if (settings.chartBorderWidth > 0)
      {
        svg.append("svg:rect")
          .attr("x", 2)
          .attr("width", plotWidth - 4)
          .attr("height", chartHeight - 192)
          .style("fill-opacity", 0)
          .style("stroke", "#333333")
          .style("stroke-width", settings.chartBorderWidth);
      }

      // draw the title
      if (settings.title !== null && settings.title.trim() !== "")
      {
        svg.append("svg:text")
          .attr("x", plotWidth / 2 + m[3])
          .attr("y", 25)
          .style("font-size", settings.titleStyle.fontSize)
          .style("font-weight", "bold")
          .attr("text-anchor", "middle")
          .attr("fill", "#333333")
          .text(settings.title);
      }

      if (settings.yTitle !== null && settings.yTitle.trim() !== "")
      {
        svg.append("svg:text")
          .attr("x", -((chartHeight - 200)/2))
          .attr("y", 30)
          .attr("transform", "rotate(-90)")
          .style("font-size", settings.axisTitleStyle.fontSize)
          .style("font-weight", "bold")
          .attr("text-anchor", "middle")
          .attr("fill", "#333333")
          .text(settings.yTitle);
      }

      if (settings.xTitle !== null && settings.xTitle.trim() !== "")
      {
        svg.append("svg:text")
          .attr("x", plotWidth / 2 + m[3] + 10)
          .attr("y", axisHeight + 50)
          .style("font-size", settings.axisTitleStyle.fontSize)
          .style("font-weight", "bold")
          .attr("text-anchor", "middle")
          .attr("fill", "#333333")
          .text(settings.xTitle);
      }

      var chart = d3.chart.box()
        .whiskers(iqrFunc(1.5))
        .width(boxWidth)
        .height(h - m[0] - m[2])
        .labels(labels)
        .colors(colors)
        .showPoints(true)
        .funcMouseover(settings.mouseover)
        .funcMouseout(settings.mouseout)
        .options(options);

      chart.domain([settings["min"],settings["max"]]);

      var vis = svg.selectAll("g.boxplot")
        .data(data)
        .enter().append("svg:g")
          .attr("transform", function(d) { return "translate(" + x(d) + "," + m[0] + ")" });

      vis.append("svg:g")
          .attr("class", "box")
          .attr("width", w)
          .attr("height", h)
        .call(chart);

      chart.duration(1000);

      var halfBox = browser.mozilla ? boxWidth : boxWidth / 2;
      svg.selectAll("text.label")
        .data(data)
        .enter().append("svg:text")
          .attr("x", function(d) { return x(d) })
          .attr("y", axisHeight + 20)
          .attr("dx", halfBox)
          .attr("text-anchor", "middle")
          .attr("fill", function(d,i) { return colors[i] })
          .style("font-size", settings.xLabelStyle.fontSize)
          .text(function(d,i) { return labels[i] });
      svg.append("svg:text")
          .attr("x", axisM - 10)
          .attr("y", m[0])
          .attr("text-anchor", "end")
          .style("font-size", settings.yLabelStyle.fontSize)
          .text(Math.ceil(settings.max));
      svg.append("svg:text")
          .attr("x", axisM - 10)
          .attr("y", axisHeight + 5)
          .attr("text-anchor", "end")
          .style("font-size", settings.yLabelStyle.fontSize)
          .text(settings.min);


      if (settings.xLineWidth > 0 || settings.yLineWidth > 0)
      {
        var axisGroup = svg.append("svg:g")
          .attr("transform", "translate(" + axisM + ",0)")
        if (settings.xLineWidth > 0)
        {
          var axisWidth = plotWidth - 100;
          axisGroup.append("svg:line") // x-axis
            .attr("x1", 0)
            .attr("x2", axisWidth)
            .attr("y1", axisHeight)
            .attr("y2", axisHeight)
            .style("stroke-width", settings.xLineWidth)
            .style("stroke", "black");
        }
        if (settings.yLineWidth > 0)
        {
          axisGroup.append("svg:line") // y-axis
            .attr("x1", 0)
            .attr("x2", 0)
            .attr("y1", m[0]-5)
            .attr("y2", axisHeight)
            .style("stroke-width", settings.yLineWidth)
            .style("stroke", "black");
        }
      }

      if (settings.showLegend)
      {
        var legendX = plotWidth + 10;
        var legend = svg.append("svg:g");
        legend.selectAll("text.label")
          .data(labels)
          .enter().append("svg:rect")
          .attr("x", legendX)
          .attr("y", function(d,i) { return i * 20 + 10; })
          .attr("width", 10)
          .attr("height", 10)
          .style("fill", function(d,i) { return colors[i]; });
        legend.selectAll("text.label")
          .data(labels)
          .enter().append("svg:text")
          .attr("x", legendX + 15)
          .attr("y", function(d,i) { return i * 20 + 19; })
          .attr("text-anchor", "left")
          .attr("fill", "#333333")
          .text(function(d) { return d; });
      }

      charts[elt.substr(1)] = chartNode;
    },
    redraw: function(options) {
      try
      {
        var chart = charts[this.attr("id")];
        if (typeof CanvasXpress != 'undefined' && chart instanceof CanvasXpress)
        {
          chart.updateColors(options["newColors"]);
          if (!options["colorOnly"])
          {
            var data = options["newData"];
            chart.updateData(data);
          }
        }
        else if (typeof Highcharts.Chart != 'undefined' && chart instanceof Highcharts.Chart)
        {
          chart.series[0].chart.options.colors = options["newColors"];
          chart.yAxis[0].options.max = options["maxY"];
          chart.series[0].setData(options["newData"], true);
          chart.yAxis.redraw();
        }
        else
        {
          // d3?
        }
      }
      catch(e)
      {
        $.error("Unable to find a chart associated with the id " + this.attr("id"));
      }
    },
    saveChart: function(options) {
      try
      {
        var chart = charts[this.attr("id")];
        if (this.is("canvas"))
        {
          var imgData = getCanvasParent(this).find('canvas')[0].toDataURL("image/png");
          defaultSaveCanvasHandler(imgData);
        }
        else if (chart instanceof Highcharts.Chart)
        {
          chart.exportChart(options);
        }
        else if (chart.is("div"))
        {
          chart.find("svg").attr('xmlns','http://www.w3.org/2000/svg').attr('xmlns:xlink','http://www.w3.org/1999/xlink')
          var svg = chart.html();
          defaultSaveSVGHandler(svg, options.filename);
        }
      }
      catch (e)
      {
        $.error("Unable to find a chart associated with the id " + this.attr("id"));
      }
    },
    copyToClipboard: function() {
      try
      {
        var chart = charts[this.attr("id")];
        if (this.is("canvas"))
        {
          var imgData = getCanvasParent(this).find('canvas')[0].toDataURL("image/png");
          defaultCopyCanvasHandler(imgData);
        }
        else if (chart instanceof Highcharts.Chart)
        {
          var svg = chart.getSVG();
          defaultCopySVGHandler(svg);
        }
        else if (this.is("svg"))
        {
          this.find('svg').attr('xmlns','http://www.w3.org/2000/svg').attr('xmlns:xlink','http://www.w3.org/1999/xlink')
          var svg = this.html();
          defaultCopySVGHandler(svg);
        }
      }
      catch(e)
      {
        $.error("Unable to find a chart associated with the id " + this.attr("id"));
      }
    }
  };

  $.fn.chartify = function(method) {
    // Method calling logic
    if (methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || ! method) {
      return methods.drawHistogram.apply(this, arguments);
    } else {
      $.error('Method ' +  method + ' does not exist on jQuery.tooltip');
    }
  };

})(jQuery);