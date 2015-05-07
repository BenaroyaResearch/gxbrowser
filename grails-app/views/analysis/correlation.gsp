<!DOCTYPE html>
<%@ page import="grails.converters.JSON; org.sagres.sampleSet.SampleSet; org.sagres.mat.Analysis"%>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta name="layout" content="matmain">
  <title>Correlations: ${title}</title>
  
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
  <style type="text/css">
    .row {
      vertical-align: middle;
      margin: 0;
      margin-left: -20px;
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
    .tooltip-table {
      font-size: 10px;
    }
    .tooltip-table td {
      height: 12px;
      vertical-align: middle;
      padding: 0;
      margin: 0;
    }
  </style>
</head>

<body itemscope itemtype="http://schema.org/Article">

<g:javascript src="jquery.qtip.min.js"/>
<g:javascript src="modules/raphael.js"/>
<g:javascript src="modules/raphael.export.js"/>

<div class="mat-container">

  <div class="mat_sidecol">
    <div id="display-panel" class="well" style="padding:14px;${params.field?.contains(',') ? '' : 'display:none;' }">
      <h4 style="line-height:18px;"><span>Display Options</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'display-options');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="display-options">
        <div class="plotoptions" style="margin-top:10px;margin-bottom:0;padding-bottom:0;">
          <div class="button-group result" name="multi-module-count" style="margin-left:0;margin-right:0;">
            <span class="button firsthalf ${params.moduleCount != 'annotated' ? 'active' : ''}" name="all" title="Show all modules" onclick="javascript:btnClick(this);refreshChart();">All</span>
            <span class="button secondhalf ${params.moduleCount == 'annotated' ? 'active' : ''}" name="annotated" title="Only those modules with a defined gene ontology" onclick="javascript:btnClick(this);refreshChart();">Annotated</span>
          </div>
        </div>
        <div id="clustering" class="plotoptions">
          <div id="cluster_options">
            <span class="filterlabel">Cluster columns by:</span>
            <div class="button-group result" name="col_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf ${params.clusterCol != 'true' ? 'active' : ''}" name="none" title="Do not cluster columns" onclick="javascript:btnClick(this);refreshChart();">None</span>
              <span class="button secondhalf ${params.clusterCol == 'true' ? 'active' : ''}" name="variables" title="Cluster columns by samples" onclick="javascript:btnClick(this);refreshChart();">Variables</span>
            </div>

            <span class="filterlabel">Cluster rows by:</span>
            <div class="button-group result" name="row_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf ${params.clusterRow != 'true' ? 'active' : ''}" name="none" title="Do not cluster rows" onclick="javascript:btnClick(this);refreshChart();">None</span>
              <span class="button secondhalf ${params.clusterRow == 'true' ? 'active' : ''}" name="modules" title="Cluster rows by modules" onclick="javascript:btnClick(this);refreshChart();">Modules</span>
            </div>
          </div>
        </div>
        <div class="plotoptions" style="margin-bottom:0;padding-bottom:0;">
          <span class="filterlabel">Show/hide correlation scores:</span>
          <div class="button-group result" name="scores" style="margin-left:0;margin-right:0;">
            <span class="button firsthalf ${params.scores == 'hide' ? 'active' : ''}" name="hide" title="Hide correlation scores" onclick="javascript:btnClick(this);toggleScores(false);">Hide</span>
            <span class="button secondhalf ${params.scores != 'hide' ? 'active' : ''}" name="show" title="Show correlation scores" onclick="javascript:btnClick(this);toggleScores(true);">Show</span>
          </div>
        </div>
        <div id="filter-slider">
          <div class="filter-slider-caption">Show correlations with scores<br/>
          	between <span id="lfilter-value">${params.lFilter ?: -1}</span> and <span id="ufilter-value">${params.uFilter ?: 1}</span></div>
           <div class="max-slider">
             <div id="slider-range-max" class="slider slider-range-max"></div>
           </div>
        </div>
         <div id="pfilter-slider">
         	<div class="filter-slider-caption">Show correlations with p-values<br/>
            	between <span id="plfilter-value">${params.plFilter ?: 0}</span> and <span id="pufilter-value">${params.puFilter ?: 1}</span></div>
            <div class="max-slider">
            	<div id="pslider-range-max" class="slider slider-range-max"></div>
            </div>
         	<div class="filter-slider-caption" style="margin-bottom:10px;">
				<g:checkBox name="useMTC" onclick="refreshChart();" value="${params.useMTC?'checked':''}"/>Multiple Testing Correction
        	</div>
         </div>
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
          updateCorrelationListHeight();
        };
        $("#slider-range-max").slider({
          range: true,
          min: -100,
          max: 100,
          values: [${(params.double("lFilter") ?: -1) * 100}, ${(params.double("uFilter") ?: 1) * 100}],
          slide: function(event, ui) {
            $("span#lfilter-value").html(ui.values[0]/100);
            $("span#ufilter-value").html(ui.values[1]/100);
          },
          stop: function(event, ui) {
            refreshChart();
          }
        });
        $("#pslider-range-max").slider({
                    range: true,
                    min: 0,
                    max: 100,
                    values: [${(params.double("plFilter") ?: 0) * 100}, ${(params.double("puFilter") ?: 1) * 100}],
                    slide: function(event, ui) {
                      $("span#plfilter-value").html(ui.values[0]/100);
                      $("span#pufilter-value").html(ui.values[1]/100);
                    },
                    stop: function(event, ui) {
                      refreshChart();
                    }
                  });
      </g:javascript>
    </div>

    <div id="correlations" class="well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Compare Correlations</span></h4>
      <div id="compareCorrelationList" style="overflow-y:auto;margin-top:10px;">
      </div>
    </div>

  </div><!--end mat_sidecol -->

  <div class="mat_maincol">
    <div id="imageInfo">
      <g:if test="${title != null}">
        <h2 itemprop="name">${title}</h2>
      </g:if>
      <g:else>
        <h2 itemprop="name">${sampleSetName} (${groupsName})</h2>
      </g:else>
      <h3><span itemprop="description" id="imageTitle"></span></h3>
      <p class="info" id="imageDescription"></p>
    </div>
    <div id="imageBody" class="image">
      <div id="imageControls">
        <div class="size">
          <a href="javascript:zoomIn();" class="imageControl imageControl-max" title="Zoom In">  </a>
          <a href="javascript:zoomOut();" class="imageControl imageControl-min" title="Zoom Out"> </a>
          <a href="javascript:fitToScreen();" class="imageControl imageControl-fit" title="Fit to Screen"> </a>
        </div>
        <div class="downloads">
          <a href="javascript:saveChart();" id="download_link" class="icon_download" title="Download Chart as Image"></a>
          <g:link name="download_file" controller="analysis" action="correlationFile" id="${analysis.id}" class="icon_download_csv" title="Download Correlation CSV File"/>
        </div>
      </div>
      <div id="correlationPlot"></div>
    </div><!--end imagebody-->
    <div id="correlation-error" class="alert-message error" style="display:none;">No values passed the filter parameters.</div>
  </div><!--end mat_maincol-->

</div><!--end mat-container-->
<g:javascript>
  var analysisId = ${analysis.id};
  var sampleSetId = ${analysis.sampleSetId ?: -1};
  var mode = '${params.mode ?: "normal"}';
  var isIE8 = $.browser.msie && $.browser.version < 9;

  Raphael.st.translate = function(tString) {
    this.forEach(function(el) {
      el.translate(tString);
    });
  };
  Raphael.st.transform = function(tString) {
    this.forEach(function(el) {
      el.transform(tString);
    });
  };

  var plotSets = new Array();
  var selectedCorrelations = new Array(), selectedTitles = new Array();
  var r = Raphael("correlationPlot", 10, 10), bg = null,
      boxes = null, annotSet = null, fullAnnotSet = null, annotLabelSet = null,
      annotMaxY = 0, spots = null, spotTexts = null, columnHeaders = null;
  var histogram = ${params.histogram ?: true};
  var annotLabelVisible = false;
  var currentScale = ${params.currentScale ?: 1.0};
  var currentShift = 0;
  var originalWidth = 800;
  var originalHeight = 600;

  r.ca.selectFunction = function(func,color) {
    var annot = this.data("annotation");
    if (annot && annot === func) {
      return { "stroke-width":2, "stroke":color };
    } else {
      return { "stroke-width":0, "stroke":"none" };
    }
  };

  var generateArgs = function() {
    var useMTC = $("#useMTC").prop("checked");
    var fields = selectedCorrelations.join(",");
    var titles = selectedTitles.join(",");
    var multi = selectedCorrelations.length > 1;
    var isMulti = multi || mode === "multi";
    var showScores = $("div[name='scores'] .button.active").attr("name");
    var clusterCol = $("div[name='col_cluster_type'] .button.active").attr("name") === "variables" && isMulti;
    var clusterRow = $("div[name='row_cluster_type'] .button.active").attr("name") === "modules" && isMulti;
    var moduleCount = $("div[name='multi-module-count'] .button.active").attr("name");
    var args = {
      _controller: "analysis",
      _action: "correlation",
      _id: analysisId,
      analysisId: analysisId,
      field: fields,
      title: titles,
      moduleCount: moduleCount,
      clusterCol: clusterCol,
      clusterRow: clusterRow,
      scores: showScores,
      mode: mode,
      useMTC: useMTC,
      histogram: histogram,
      lFilter: $("span#lfilter-value").text(),
      uFilter: $("span#ufilter-value").text(),
      plFilter: $("span#plfilter-value").text(),
      puFilter: $("span#pufilter-value").text()
    };
    return args;
  };
  var generateLink = function(showEmail,isClient) {
    var args = generateArgs();
    $.getJSON(getBase()+"/miniURL/create", args, function(json) {
      if (showEmail || isClient) {
        var subject = "Module Analysis Tool Link for '${title}' - Correlation";
        var text = "Hello,\r\n\r\nI thought you'd be interested in looking at the analysis '${title}' in the Module Analysis Tool.\r\n\r\n" +
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
  var toggleScores = function(show) {
    if (spotTexts !== null) {
      if (show) {
        spotTexts.show();
      } else {
        spotTexts.hide();
      }
    }
  };
  var refreshChart = function() {
    drawChart(selectedCorrelations.join(","), selectedTitles.join(","));
  };
  var checkCorrelation = function(elt) {
    $("div#correlations").qtip("hide").qtip("disable");
    var it = $(elt);
    var field = it.attr("id");
    var title = it.parent().text();
    if (it.is(":checked")) {
      if (selectedCorrelations.length === 0) {
        histogram = true;
        $("div#display-panel").hide();
      } else {
        $("div#display-panel").show();
      }
      addCorrelation(field,title);
    } else {
      removeCorrelation(field);
      if (selectedCorrelations.length < 2) {
        $("div#display-panel").hide();
      }
    }
    updateCorrelationListHeight();
  };
  var updateCorrelationListHeight = function() {
    var windowHeight = $(window).height();
    var multiList = $("div#compareCorrelationList");
    var listHeight = windowHeight - multiList.position().top - 100;
    multiList.height(listHeight);
  };
  var addCorrelation = function(field,title) {
    $("#correlationSelect").val("none");
    selectedCorrelations.push(field);
    selectedTitles.push(title);
    drawChart(selectedCorrelations.join(","), selectedTitles.join(","));
  };
  var removeCorrelation = function(field) {
    var idx = selectedCorrelations.indexOf(field);
    selectedCorrelations.splice(idx,1);
    selectedTitles.splice(idx,1);
    drawChart(selectedCorrelations.join(","), selectedTitles.join(","));
    if (selectedCorrelations.length === 0)
    {
      showInitialTooltip();
    }
  };

  var saveChart = function() {
    var svg = null;
    if ($.browser.msie && $.browser.version < 9) {
      svg = r.toSVG();
    } else {
      if ($.browser.msie && $.browser.version == 9) {
        $("div#correlationPlot svg").removeAttr("xmlns");
      }
      svg = $("div#correlationPlot").html();
    }
    $.post(getBase()+"/charts/saveImg", { svg:svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      var filename = "analysis_".concat(analysisId).concat("correlations");
      window.location.href = location.concat("?filename=").concat(filename);
    });
  };
  var clearChart = function() {
    if (barSet !== null) { barSet.clear(); }
    if (plotSets !== null) { plotSets.splice(0, plotSets.length); }
    bg = null;
    if (boxes !== null) { boxes.clear(); }
    if (annotSet !== null) { annotSet.clear(); }
    if (fullAnnotSet !== null) { fullAnnotSet.clear(); }
    if (annotLabelSet !== null) { annotLabelSet.clear(); }
    if (spots !== null) { spots.clear(); }
    r.clear();
    if (columnHeaders !== null) { columnHeaders.clear(); }
    annotLabelVisible = false;
    annotMaxY = 0;
    currentScale = 1.0;
    currentShift = 0;
  };
  var drawChart = function(fields,titles) {
    clearChart();
    var hasSelection = selectedCorrelations.length > 0;
    if (hasSelection) {
      var trimmedTitles = titles;
      if (trimmedTitles.length > 65) {
      	trimmedTitles = titles.trim().substring(0, 65)
      	.split(" ") // separate characters into an array of words
    	.slice(0, -1)    // remove the last full or partial word
    	.join(" ") + "..."; // combine into a single string and append "..."
      }
      $("span#imageTitle").html("Correlation of Modules and " + trimmedTitles);
      $("p#imageDescription").html("Shown is the value of the Spearman's correlation (rho) between the selected variable(s) and the module percent difference for each combination of module and variable. Module percent difference is calculated as the difference between the number of (significant) positive and negative fold change probes, divided by the total number of probes in the module.");
    } else {
      $("span#imageTitle").html("");
      $("p#imageDescription").html("");
    }

    var multi = selectedCorrelations.length > 1;
    var isMulti = multi || mode === "multi";
    var clusterCol = $("div[name='col_cluster_type'] .button.active").attr("name") === "variables" && isMulti;
    var clusterRow = $("div[name='row_cluster_type'] .button.active").attr("name") === "modules" && isMulti;
    var moduleCount = $("div[name='multi-module-count'] .button.active").attr("name");
    var useMTC = $("#useMTC").prop("checked");

    if (hasSelection)
    {
      var args = {
        analysisId: analysisId,
        field: fields,
        title: titles,
        moduleCount: moduleCount,
        clusterCol: clusterCol,
        clusterRow: clusterRow,
        mode: mode,
        histogram: histogram,
        useMTC: useMTC,
        lFilter: $("span#lfilter-value").text(),
        uFilter: $("span#ufilter-value").text(),
        plFilter: $("span#plfilter-value").text(),
        puFilter: $("span#pufilter-value").text()
      };
      $.getJSON(getBase()+"/analysis/getCorrelation", args, function(json) {
        if (json) {
          var downloadLink = "${createLink(controller: 'analysis', action: 'correlationFile')}?".concat($.param(args));
          $("a[name='download_file']").attr("href",downloadLink);

          $("div#correlation-error").hide();
          //----- CUSTOM ATTRIBUTES -----//
          if (!isMulti && histogram) {
            setTimeout(function() { drawCorHistogram(json); }, 0);
          } else {
            originalWidth = json.width + 20;
            originalHeight = json.height + 20;

            r.setSize(originalWidth, originalHeight);
            r.setViewBox(0, 0, originalWidth, originalHeight, true);
            setTimeout(function() { drawBg(json); drawAxes(json,fields); }, 0);
          }
        } else {
          r.setSize(10,10);
          $("div#correlation-error").show();
        }
      });
    }
  };

  var drawBg = function(json) {
    // draw background
    bg = r.rect(0, 0, originalWidth, originalHeight).attr({ fill:"#FFF", stroke:"none", "stroke-width":0 });
    if (json.bg) {
      var pBg = json.bg;
      plotSets.push(r.rect(pBg.x, pBg.y, pBg.width, pBg.height).attr(pBg.attr));
    }
  };

  var drawAxes = function(json,field) {
    if (json.xAxis) {
      columnHeaders = r.add(json.xAxis);
      plotSets.push(columnHeaders);
    }
    if (json.yAxis) {
      plotSets.push(r.add(json.yAxis));
    }
    setTimeout(function() { drawDendrograms(json,field); }, 0);
  };

  var drawDendrograms = function(json,field) {
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
    setTimeout(function() { drawSpots(json); }, 0);
  };

  var drawSpots = function(json) {
    if (json.spots) {
      if (spots === null) {
        spots = r.set();
      }
      var corrSet = r.set();
      $.each(json.spots, function(i,c) {
        var circle = r.rect(c.x,c.y,c.width,c.height).attr(c.attr);
        if (c.data) {
          $.each(c.data, function(d,value) {
           circle.data(d,value);
          });
        }
        circle.mouseover(spotTooltipShow);
        corrSet.push(circle);
        spots.push(circle);
      });
      plotSets.push(spots);
    }
    if (json.spotTexts) {
      if (spotTexts === null) {
        spotTexts = r.set();
      }
      $.each(json.spotTexts, function(i,st) {
        var txt = r.text(st.cx, st.cy, st.text).attr(st.attr);
        if (st.data) {
          $.each(st.data, function(d,value) {
           txt.data(d,value);
          });
        }
        txt.mouseover(spotTooltipShow);
        spotTexts.push(txt);
      });
      if ($("div[name='scores'] .button.active").attr("name") === "hide") {
        spotTexts.hide();
      }
      plotSets.push(spotTexts);
    };
    setTimeout(function() { finish(); }, 0);
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
    if (elt.data("pValue") && elt.data("pValue") < 0.0001) {
        	tooltipText +=  "pValue: " + elt.data("pValue").toExponential(4) + "<br/>";
    } else if (elt.data("pValue")) {
    	tooltipText +=  "pValue: " + elt.data("pValue").toFixed(4) + "<br/>";
    }
    if (elt.data("gxbLink")) {
      tooltipText += elt.data("gxbLink");
    }
    tooltip($(elt.node), tooltipText);
    if (boxes && elt.data("annotation")) {
      setTimeout(function() { boxes.attr({ selectFunction:[ elt.data("annotation"), elt.data("color") ] }); }, 0);
    }
  };
  var spotTooltipShow = function() {
//    this.attr({ stroke:"#FFF", "stroke-width":2 });
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

  //----- HISTOGRAM -----//
  var barTooltip = function() {
    var tooltipText = "# of Modules: " + this.data("count");
    tooltipText += "<div style='max-height:180px;overflow-y:auto'><table class='tooltip-table'><tbody>";
    $.each(this.data("modules"), function(i,v) {
      tooltipText += "<tr><td>" + v.module;
      if (v.annotation) {
        tooltipText += " - " + v.annotation;
      }
      tooltipText += "</td><td>" + v.score + "</td></tr>";
    });
    tooltipText += "</tbody></table></div>";
    this.attr("fill-opacity", 0.8);
    $(this.node).qtip({
      content: {
        text: tooltipText
      },
      position: {
        my: "bottom center",
        at: "top center",
        viewport: $(window)
      },
      show: {
        solo: true,
        ready: true,
        delay: 100
      },
      hide: {
        delay: 100,
        fixed: true,
        event: "click mouseleave"
      },
      style: {
        width: 220,
        classes: 'ui-tooltip-tipsy'
      },
      events: {
        hide: function(event, api) { api.destroy(); }
      }
    });
  };
  var barTooltipHide = function() {
    this.attr("fill-opacity", 0.3);
  };
  var barClick = function() {
    histogram = false;
    $("div#display-panel").show();
    $("#slider-range-max").slider("values", 0, this.data("lRange") * 100);
    $("#slider-range-max").slider("values", 1, this.data("uRange") * 100);
    $("#pslider-range-max").slider("values", 0, this.data("plRange") * 100);
    $("#pslider-range-max").slider("values", 1, this.data("puRange") * 100);
    $("span#lfilter-value").html(this.data("lRange"));
    $("span#ufilter-value").html(this.data("uRange"));
    $("span#plfilter-value").html(this.data("plRange"));
    $("span#pufilter-value").html(this.data("puRange"));
    drawChart(selectedCorrelations.join(","), selectedTitles.join(","));
  };

  var barSet = r.set();
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
      barAttr: { fill:"#277041", "fill-opacity":0.3, stroke:"#277041", "stroke-width":2 },
      axesAttr: { fill:"#666", stroke:"none", "stroke-width":0 },
      xTickAttr: { fill:"#333", font:"14px Helvetica" },
      yTickAttr: { fill:"#333", font:"14px Helvetica", "text-anchor":"end" }
    };
    return settings;
  };
  var drawCorHistogram = function(json) {
    var settings = plotSettings();
    var x = settings.yAxisMargin + (settings.buffer / 2);
    var y = settings.topMargin + settings.buffer;

    originalWidth = settings.width + 20;
    originalHeight = settings.height;

    r.setSize(settings.width + 20, settings.height);
    r.setViewBox(0, 0, settings.width + 20, settings.height, true);
    r.rect(0, 0, settings.width + 20, settings.height).attr({ fill:"#fff", stroke:"none", "stroke-width":0 });

    var max = json.max;
    if (max > 0) {
      var xTickHeight = settings.height - settings.xAxisMargin + 14;
      r.text(x, xTickHeight, -1).attr(settings.xTickAttr);
      // draw the bars
      $.each(json.bars, function(i, bar) {
        if (bar.count > 0) {
          var barHeight = bar.count / max * settings.pHeight;
          var yOffset = settings.pHeight - barHeight;
          var theBar = r.rect(x, y + yOffset, settings.barWidth, barHeight).attr(settings.barAttr);
          $.each(bar.data, function(dk,dv) {
            theBar.data(dk,dv);
          });
          theBar.mouseover(barTooltip).mouseout(barTooltipHide).click(barClick);
          barSet.push(theBar);
        }
        x += settings.barWidth;
        r.text(x, xTickHeight, bar.data.uRange).attr(settings.xTickAttr);
      });

      // draw the axes
      r.rect(settings.yAxisMargin, settings.topMargin + settings.buffer, 2, settings.pHeight - 1).attr(settings.axesAttr);
      r.text(settings.yAxisMargin - 5, settings.topMargin + settings.buffer, max).attr(settings.yTickAttr);
      r.text(settings.yAxisMargin - 5, settings.height - settings.xAxisMargin, 0).attr(settings.yTickAttr);
      r.rect(settings.yAxisMargin, settings.height - settings.xAxisMargin - 1, settings.pWidth, 2).attr(settings.axesAttr);
    }
  };

  var showInitialTooltip = function() {
    $("div#correlations").qtip({
      content: {
        text: "Select one or more correlation variables from this list for comparison"
      },
      position: {
        my: "left center",
        at: "right top",
        adjust: {
          y: 120
        }
      },
      show: {
        solo: true,
        ready: true,
        delay: 0,
        event: null
      },
      hide: {
        event: null
      },
      style: {
        classes: 'ui-tooltip-tipsy ui-tooltip-shadow'
      }
    });
  };
  var populateCorrelations = function() {
    var windowHeight = $(window).height();
    var multiList = $("div#compareCorrelationList");
    var listHeight = windowHeight - multiList.position().top - 100;
    multiList.height(listHeight);
    var args = { id: analysisId };
    $.getJSON(getBase()+"/analysis/correlationOptions", args, function(json) {
      if (json.error)
      {
        setTimeout(function() { populateCorrelations(); }, 0);
      }
      else if (json)
      {
        $.each(json, function(i,cor) {
          var row = "";
          if (cor.order == -1) { // section header.
          	row = '<div class="row" style="font-weight:bold;"><span class="span3">' + cor.displayName + '</span> </div>';
          } else { 
          	row = '<div class="row bold"><span class="span3"><input type="checkbox" name="' + cor.field + '" id="' + cor.field + '" onclick="checkCorrelation(this);"/> ' + cor.displayName + '</span> </div>';
          }
          multiList.append(row);
        });
//        $("div#correlations").effect("bounce");
        <g:if test="${params.title}">
          <g:each in="${params.title.split(',')}" var="t">
            selectedTitles.push("${t}");
          </g:each>
        </g:if>
        <g:if test="${params.field}">
          <g:each in="${params.field.split(',')}" var="f">
            selectedCorrelations.push("${f}");
            $("div#compareCorrelationList input[id='${f}']").attr("checked", true);
          </g:each>
          refreshChart();
        </g:if>
        <g:else>
          showInitialTooltip();
        </g:else>
      }
    });
  };

  $(document).ready(function() {
    if (mode === "multi") {
      $("div.annotationKey").hide();
      $("div#clustering").show();
      $("div[name='one-module-count']").hide();
      $("div[name='multi-module-count']").show();
    }
    setTimeout(function() { populateCorrelations(); }, 500);

    var windowWidth = $(window).width(), windowHeight = $(window).height();
    var resizing;
    $(window).resize(function() {
      if (windowWidth !== $(window).width() && windowHeight !== $(window).height()) {
        windowWidth = $(window).width();
        windowHeight = $(window).height();
        clearTimeout(resizing);
        resizing = setTimeout(function() { resizeChart(); }, 100);
      }
      var multiList = $("div#compareCorrelationList");
      var listHeight = windowHeight - multiList.position().top - 100;
      multiList.height(listHeight);
    });
  });
</g:javascript>

 <g:render template="/common/emailLink"/>
<g:render template="/common/bugReporter" model="[tool:'MAT Correlation']"/>

</body>
</html>