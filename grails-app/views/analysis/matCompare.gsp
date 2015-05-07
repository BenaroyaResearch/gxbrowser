<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
  <style type="text/css">
    .icon.settings:before {
      margin: -0.15em 0.75em 0 -0.25em;
    }
    .ui-tooltip-content a {
      color: #F1D031;
    }
    .tooltip-value {
      font-weight:bold;
      font-size:14px;
      margin-bottom: 4px;
    }
    .up:after {
      content: "";
      position: relative;
      top: 5px;
      float:left;
      width: 0;
      height: 0;
      border-left: 7px solid transparent;
      border-right: 7px solid transparent;
      border-bottom: 7px solid #DB2929;
      margin-right: 5px;
    }
    .down:after {
      content: "";
      position: relative;
      top: 5px;
      float:left;
      width: 0;
      height: 0;
      border-left: 7px solid transparent;
      border-right: 7px solid transparent;
      border-top: 7px solid #33A1DE;
      margin-right: 5px;
    }
  </style>
  <g:javascript>
    var genId = ${params.id};
    var exportPlotKey = function() {
      var svg = null;
      if ($.browser.msie && $.browser.version < 9) {
        svg = plotKeyR.toSVG();
      } else {
        svg = $("div#plotkey").html();
      }
      $.post(getBase()+"/charts/saveImg", { svg: svg }, function(id) {
        var location = getBase()+"/charts/downloadImg/"+id;
        var filename = $("#imageTitle").html().concat(" Plot Key");
        window.location.href = location.concat("?filename=").concat(filename);
      });
    };

    var generateArgs = function() {
      var args =
      {
        _controller: "analysis",
        _action: "moduleAnalysesPlot",
        _id: genId,
        filter: $("span#floor-value").text(),
        showRowSpots: $("input#show-row-spots").is(":checked"),
        currentScale: currentScale
      };
      return args;
    };
    var generateLink = function(showEmail,isClient) {
      var args = generateArgs();
      $.getJSON(getBase()+"/miniURL/create", args, function(json) {
        if (showEmail || isClient) {
          var subject = "Module Analysis Tool Link for ${params.id}";
          var text = "Hello,\r\n\r\n I thought you'd be interested in looking at the analysis '${params.id}' in the Module Analysis Tool. \r\n\r\n" +
            "To view it, click the link below: ".concat(json.link);
          if (isClient) {
            var encodedText = encodeURIComponent(text);
            window.location = "mailto:?subject=" + subject + "&body=" + encodedText;
          }
          if (showEmail) {
            $("form#emailLinkForm #subject").val(subject);
            $("form#emailLinkForm #message").val(text);
            showEmailPanel();
          }
        } else {
          showLinkPanel(json.link);
        }
      });
    };
  </g:javascript>
</head>

<body>

<g:javascript src="jquery.qtip.min.js"/>
<g:javascript src="modules/raphael.js"/>
<g:javascript src="modules/raphael.export.js"/>

<div class="mat-container">

  <div class="mat_sidecol">

    <div id="display-panel" class="well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Display Options</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'display-options');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="display-options">
        <div class="plotoptions" style="margin-top:10px;margin-bottom:0;padding-bottom:0;">
          <div class="button-group result" name="module-count" style="margin-left:0;margin-right:0;">
            <span class="button firsthalf ${params.moduleCount != 'annotated' ? 'active' : ''}" name="all" title="Show all modules" onclick="javascript:btnClick(this);drawChart();">All</span>
            <span class="button secondhalf ${params.moduleCount == 'annotated' ? 'active' : ''}" name="annotated" title="Only those modules with a defined gene ontology" onclick="javascript:btnClick(this);drawChart();">Annotated</span>
          </div>
        </div>
        <div id="clustering" class="plotoptions">
          <div id="cluster_options">
            <span class="filterlabel">Cluster columns by:</span>
            <div class="button-group result" name="col_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf ${params.clusterCol != 'true' ? 'active' : ''}" name="none" title="Do not cluster columns" onclick="javascript:btnClick(this);drawChart();">None</span>
              <span class="button secondhalf ${params.clusterCol == 'true' ? 'active' : ''}" name="analyses" title="Cluster columns by analyses" onclick="javascript:btnClick(this);drawChart();">Analyses</span>
            </div>

            <span class="filterlabel">Cluster rows by:</span>
            <div class="button-group result" name="row_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf ${params.clusterRow != 'true' ? 'active' : ''}" name="none" title="Do not cluster rows" onclick="javascript:btnClick(this);drawChart();">None</span>
              <span class="button secondhalf ${params.clusterRow == 'true' ? 'active' : ''}" name="modules" title="Cluster rows by modules" onclick="javascript:btnClick(this);drawChart();">Modules</span>
            </div>
          </div>
        </div>
        <div class="indView" id="filter-slider">
          <div class="filter-slider-caption">Show modules with at <br />least one sample > <span id="floor-value">${params.floor ?: 0}</span>%</div>
          <div class="max-slider">
            <div id="slider-range-max"></div>
          </div>
        </div>
        <div id="row-spots" class="toggle-spotrows"><label for="show-row-spots"><g:checkBox name="show-row-spots" onclick="javascript:drawChart();" checked="${params.showRowSpots ?: true}"/> <span>Show all spots in row on filter</span></label></div>
      </div>
      <g:javascript>
        var btnClick = function(elt) {
          var it = $(elt);
          it.siblings().removeClass("active");
          if (!it.hasClass("active")) {
            it.addClass("active");
          }
        };
        var minMax = function(elt,divId) {
          var minimize = $(elt).hasClass("ui-icon-minimize");
          if (minimize) {
            $("div#"+divId).hide();
          } else {
            $("div#"+divId).show();
          }
          $(elt).toggleClass("ui-icon-minimize");
          $(elt).toggleClass("ui-icon-expand");
        };
        $("#slider-range-max").slider({
          range: "max",
          min: 0,
          max: 100,
          value: ${params.filter ?: 0},
          slide: function(event, ui) {
            $("span#floor-value").html(ui.value);
          },
          stop: function(event, ui) {
            drawChart();
          }
        });
      </g:javascript>
    </div>

    %{-- PLOT KEY UI --}%
    <div class="plotkey well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Plot Key</span>
        <div class="ui-icons">
          <span class="icon_download-small" onclick="exportPlotKey();"></span>
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'plotkey');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="plotkey" style="margin-top:10px;"></div>
    </div>

    <g:javascript>
      var plotKeyR = Raphael("plotkey", 200, 300);
      plotKeyR.setSize(200,320);
      plotKeyR.rect(0,0,195,320).attr({ fill:"#fff", stroke:"none" });
      plotKeyR.text(46,20,"OVER-XP").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(146,20,"UNDER-XP").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,45,"100%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,67,"90%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,89,"80%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,111,"70%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,133,"60%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,155,"50%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,177,"40%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,199,"30%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,221,"20%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,243,"10%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.text(96,265,"0%").attr({ "font-size":12, "fill":"#333333" });
      plotKeyR.rect(36,45,20,220).attr({ fill:"90-#fff-#ff0000", stroke:"#333", "stroke-width":2 });
      plotKeyR.rect(136,45,20,220).attr({ fill:"90-#fff-#0000ff", stroke:"#333", "stroke-width":2 });
      plotKeyR.text(96,295,"% probe sets with p < 0.05").attr({ "font-size":12, "fill":"#333333" });
    </g:javascript>
  </div><!--end mat_sidecol -->

  <div class="mat_maincol">

    <div id="imageInfo">
      <h2>Module vs. Analyses Plot</h2>
      <h3><span id="imageTitle"></span></h3>
      <p class="info" id="imageDescription">Module Generation ${params.id} modules vs. Analyses Plots</p>
    </div>

    <div id="imageBody" class="image">
      <div id="imageControls">
        <div class="size">
          <a href="javascript:zoomIn();" class="imageControl imageControl-max" title="Zoom In">  </a>
          <a href="javascript:zoomOut();" class="imageControl imageControl-min" title="Zoom Out"> </a>
          <a href="javascript:fitToScreen();" class="imageControl imageControl-fit" title="Fit to Screen"> </a>
        </div>
        <div class="downloads">
          <a href="javascript:saveChart();" id="download_link" class="icon_download" title="Download Plot as Image"></a>
        </div>
      </div>
    </div><!--end imagebody-->

    <div id="modulePlot"></div>

    <g:render template="/common/emailLink"/>

  </div><!--end mat_maincol-->

</div><!--end mat-container-->

<g:render template="/common/bugReporter" model="[tool:'MAT XProject']"/>

<g:javascript>
  var plotSets = new Array();
  var r = Raphael("modulePlot", 10, 10), bg = null,
      boxes = null, spots = null, columnHeaders = null;
  var currentScale = ${params.currentScale ?: 1.0};
  var currentShift = 0;
  var originalWidth = 800;
  var originalHeight = 600;

  var saveChart = function() {
    var svg = null;
    if ($.browser.msie && $.browser.version < 9) {
      svg = r.toSVG();
    } else {
      if ($.browser.msie && $.browser.version == 9) {
        $("div#modulePlot svg").removeAttr("xmlns");
      }
      svg = $("div#modulePlot").html();
    }
    $.post(getBase()+"/charts/saveImg", { svg:svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      var filename = "generation_".concat(genId).concat("_module_vs_analyses");
      window.location.href = location.concat("?filename=").concat(filename);
    });
  };

  var clearChart = function() {
    if (plotSets !== null) { plotSets.splice(0, plotSets.length); }
    bg = null;
    if (boxes !== null) { boxes.clear(); }
    if (spots !== null) { spots.clear(); }
    r.clear();
    if (columnHeaders !== null) { columnHeaders.clear(); }
    currentScale = 1.0;
    currentShift = 0;
  };

  var drawChart = function() {
    clearChart();
    var clusterCol = $("div[name='col_cluster_type'] .button.active").attr("name") === "analyses";
    var clusterRow = $("div[name='row_cluster_type'] .button.active").attr("name") === "modules";
    var moduleCount = $("div[name='module-count'] .button.active").attr("name");
    var showRowSpots = $("input#show-row-spots").is(":checked");
    var floor = $("span#floor-value").text();

    var args = {
      id: genId,
      showRowSpots: showRowSpots,
      floor: floor,
      moduleCount: moduleCount,
      clusterCols: clusterCol,
      clusterRows: clusterRow
    };
    $.getJSON(getBase()+"/analysis/moduleAnalysesPlot", args, function(json) {
      if (json) {
        originalWidth = json.width + 20;
        originalHeight = json.height + 20;

        r.setSize(originalWidth, originalHeight);
        r.setViewBox(0, 0, originalWidth, originalHeight, true);
        setTimeout(function() { drawBg(json); drawModuleLabels(json); }, 0);
      }
    });
  };

  var drawBg = function(json) {
    // draw background
    bg = r.rect(0, 0, originalWidth, originalHeight).attr({ fill:"#FFF", stroke:"none", "stroke-width":0 });
    if (json.bg) {
      var pBg = json.bg;
      plotSets.push(r.rect(pBg.x, pBg.y, pBg.width, pBg.height).attr(pBg.attr));
    }
  };

  var drawAxes = function(json) {
    if (json.moduleLabels) {
      columnHeaders = r.add(json.xAxis);
      plotSets.push(columnHeaders);
    }
    if (json.yAxis) {
      plotSets.push(r.add(json.yAxis));
    }
    setTimeout(function() { drawDendrograms(json); }, 0);
  };

  var drawModuleLabels = function(json) {
    if (json.moduleLabels) {
      var moduleSet = r.set();
      $.each(json.moduleLabels, function(i, m) {
        var mLbl = r.text(m.x,m.y,m.text).attr(m.attr);
        $.each(m.data, function(d,value) {
          mLbl.data(d,value);
        });
//        mLbl.mouseover(moduleTooltip);
        moduleSet.push(mLbl);
      });
      plotSets.push(moduleSet);
    }
    if (json.annotationKeys) {
      plotSets.push(r.add(json.annotationKeys));
    }
    setTimeout(function() { drawSampleLabels(json); }, 0);
  };

  var drawSampleLabels = function(json) {
    if (json.sampleLbls) {
      sampleLblSet = r.set();
      $.each(json.sampleLbls, function(i,s) {
        var sLbl = r.text(s.x,s.y,s.text).attr(s.attr)
          .transform("r-90,"+s.x+","+s.y);
        $.each(s.data, function(d,value) {
          sLbl.data(d,value);
        });
        sampleLblSet.push(sLbl);
      });
//      updateChartSize();
    }
    setTimeout(function() { drawDendrograms(json,-1,-1); }, 0);
  };

  var drawDendrograms = function(json) {
    if (json.colDendrogram) {
      var dendroSet = r.set();
      $.each(json.colDendrogram.lines, function(i,l) {
        var lr = r.rect(l.x,l.y,l.width,l.height).attr(l.attr);
        if (l.type === "hLine") {
          lr.data("c0", l.data.c0);
          lr.data("c1", l.data.c1);
        }
        dendroSet.push(lr);
      });
      plotSets.push(dendroSet);
    }
    setTimeout(function() { drawSpots(json, -1, -1); }, 0);
  };

  var drawSpots = function(json,start,end) {
    if (json.spots) {
      var numSpots = json.spots.length;
      if (start < 0 || end < 0) {
        start = 0;
        end = json.sampleLbls ? json.sampleLbls.length : numSpots;
        spots = r.set();
      }
      while (start < end) {
        var c = json.spots[start];
        var circle = r.circle(c.cx,c.cy,c.r).attr(c.attr);
        $.each(c.data, function(d,value) {
         circle.data(d,value);
        });
        spots.push(circle);
        start++;
      }
      if (json.sampleLbls && start < numSpots) {
        start = end;
        var newEnd = end + json.sampleLbls.length;
        end = Math.min(numSpots, newEnd);
        setTimeout(function() { drawSpots(json,start,end); }, 10);
      } else {
        spots.mouseover(spotTooltipShow).mouseout(spotTooltipHide);
        plotSets.push(spots);
      }
    }
//    setTimeout(function() { finish(); }, 0);
  };

  var finish = function() {
    var bBox = columnHeaders.getBBox();
    var width = Math.ceil((bBox.width + bBox.x) - originalWidth), height = Math.ceil(bBox.height);
    shift(width,height);
  };

  //----- Scaling chart -----//
  var scaleChart = function() {
    if (currentScale >= 0.5)
    {
      var newWidth = originalWidth * currentScale;
      var newHeight = (originalHeight + currentShift) * currentScale;
      r.setSize(newWidth, newHeight);
      r.setViewBox(0,0,originalWidth,originalHeight+currentShift,true);
      $("div#chart").width(newWidth + 20);
    }
    else
    {
      currentScale = 0.5;
    }
  };
  var zoomIn = function() {
    currentScale += 0.1;
    scaleChart();
  };
  var zoomOut = function() {
    currentScale -= 0.1;
    scaleChart();
  };
  var fitToScreen = function() {
    var wWidth = $(window).width() - $("div.mat_sidecol").width() - 120;
    var wScale = wWidth / originalWidth;
    currentScale = Math.max(0.5,wScale);
    scaleChart();
  };
  var originalSize = function() {
    currentScale = 1.0;
    scaleChart();
  };
  var shift = function(x,y) {
    $.each(plotSets, function(i,s) {
      s.transform("T0,"+y);
    });
    $.each(columnHeaders, function(i,h) {
      var cx = h.attr("x"), cy = h.attr("y");
      h.translate(0,20).rotate(-45, cx, cy);
    });
    originalHeight += y;
    originalWidth += x;
    bg.attr("height",originalHeight);
    bg.attr("width",originalWidth);
    scaleChart();
  };

  //----- TOOLTIPS -----//
  var tooltip = function(elt,text) {
    elt.qtip({
      content: {
        text: text
      },
      position: {
        my: "left center",
        at: "right center",
        viewport: $(window)
      },
      show: {
        solo: true,
        ready: true,
        delay: 100
      },
      hide: {
        delay: 100,
        inactive: 1500,
        fixed: true,
        event: "click mouseleave"
      },
      style: {
        classes: 'ui-tooltip-tipsy'
      },
      events: {
        hide: function(event, api) { api.destroy(); }
      }
    });
  };
  var infoTooltip = function(elt) {
    var tooltipText = "";
    if (elt.data("value")) {
      var upDown = elt.data("up") ? "up" : "down";
      tooltipText += "<div class='tooltip-value'><span class='"+upDown+"'>" + elt.data("value") + "</span></div>";
    }
    $.each(elt.data("info"), function(key,value) {
      tooltipText += key + ": " + value + "<br/>";
    });
    if (elt.data("gxbLink")) {
      tooltipText += elt.data("gxbLink");
    }
    tooltip($(elt.node), tooltipText);
    if (boxes && elt.data("annotation")) {
      setTimeout(function() { boxes.attr({ selectFunction:[ elt.data("annotation"), elt.data("color") ] }); }, 0);
    }
  };
  var spotTooltipShow = function() {
    this.attr({ stroke:"#222", "stroke-width":2});
    infoTooltip(this);
  };
  var spotTooltipHide = function() {
    this.attr({ stroke:"none", "stroke-width":0 });
  };
  var tooltipShow = function() {
    infoTooltip(this);
  };
  var tooltipHide = function() {
    if (boxes) {
      boxes.attr({ "stroke-width":0, "stroke":"none" });
    }
  };

  var plotSettings = function() {
    var div = $("div#correlationPlot");
    var width = div.width() - 20;
    var height = $(window).height() - div.position().top - 250;
    var yAxisMargin = 50, xAxisMargin = 30, topMargin = 30, buffer = 10;
    var pWidth = width - yAxisMargin;
    var settings = {
      width: width,
      height: height,
      yAxisMargin: yAxisMargin,
      xAxisMargin: xAxisMargin,
      topMargin: topMargin,
      buffer: buffer,
      barWidth: Math.floor((pWidth - buffer)/10),
      pWidth: pWidth,
      pHeight: height - xAxisMargin - topMargin - buffer,
      axesAttr: { fill:"#666", stroke:"none", "stroke-width":0 },
      xTickAttr: { fill:"#333", font:"14px Helvetica" },
      yTickAttr: { fill:"#333", font:"14px Helvetica", "text-anchor":"end" }
    };
    return settings;
  };

  $(document).ready(function() {
    drawChart();
  });
</g:javascript>

</body>
</html>