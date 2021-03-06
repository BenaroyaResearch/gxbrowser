<%@ page import="org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap; grails.converters.JSON" %>
<div id="chart"></div>
<style>
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
  var isIE8 = $.browser.msie && $.browser.version < 9;
  var img = null;
  var isIndividualPlot = false;
  var plotSets = new Array();
  var r = Raphael("chart", 100, 100), bg = null, boxes = null, cis = null, annotSet = null, fullAnnotSet = null,
  circlesSet = null, segmentsSet = null, pcirclesSet = null, groupSet = null, sampleLblSet = null, annotLabelSet = null, abbrevSet = null;
  var currentScale = ${params.currentScale ?: 0.0};
  var originalWidth = 800;
  var originalHeight = 600;
  var annotLabelVisible = false, annotMaxY = 0;

  var label = null;
  var saveChart = function() {
    var svg = null;
    if ($.browser.msie && $.browser.version < 9) {
      svg = r.toSVG();
    } else {
      if ($.browser.msie && $.browser.version == 9) {
        $("div#chart svg").removeAttr("xmlns");
      }
      svg = $("div#chart").html();
    }
    $.post(getBase()+"/charts/saveImg", { svg:svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      var filename = $("#imageTitle").html();
      window.location.href = location.concat("?filename=").concat(filename);
    });
  };
  var exportLegend = function(divId) {
    // create svg
    var title = $("div[id='"+divId+"']").prev().text();
    var legendR = Raphael(0,0,200,200);
    var mx = 0, y = 10;
    var lt = legendR.text(8, y+10, title).attr({ fill:"#333", font:"14px Arial", "font-weight":"bold", "text-anchor":"start" });
    mx = Math.max(mx, lt.getBBox().width);
    y += 20;
    $("div[id='"+divId+"'] div.row").each(function(i,elt) {
      var it = $(elt);
      var color = it.find(".swatch").css("background-color");
      var text = it.find(".span3").html();
      legendR.rect(10, y, 12, 12).attr({ fill:color, stroke:"none", "stroke-width":0 });
      var t = legendR.text(28, y+7, text).attr({ fill:"#333", font:"12px Arial", "text-anchor":"start" });
      mx = Math.max(mx, t.getBBox().width);
      y += 16;
    });
    legendR.rect(0,0,mx+55,y+10).attr({ fill:"#fff", stroke:"#333", "stroke-width":1 }).toBack();
    legendR.setSize(mx+57,y+12);
    var svg = legendR.toSVG();
    legendR.clear();
    delete legendR;
    $.post(getBase()+"/charts/saveImg", { svg: svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      var filename = "analysis"+analysisId+"_"+divId;
      window.location.href = location.concat("?filename=").concat(filename);
    });
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
        inactive: 3000,
        fixed: true,
        event: "click mouseleave"
      },
      style: {
        classes: 'ui-tooltip-tipsy'
      }
    });
  };

  // Tooltips for spots
  var tooltipShow = function() {
    var lblText = "";
    if (this.data("group")) {
      var gColor = "background-color:"+groups[this.data("group")];
      lblText += "<div style='"+gColor+";height:3px;margin-bottom:12px;'></div>";
    }
    if (typeof this.data("value") !== "undefined") {
      var upDown = this.data("up") ? "up" : "down";
      lblText += "<div class='tooltip-value'><span class='"+upDown+"'>" + this.data("value") + "</span></div>";
    }
    $.each(this.data("info"), function(lbl,val) {
      lblText += lbl + ": " + val + "<br/>";
    });
    if (this.data("moduleWiki")) {
      lblText += this.data("moduleWiki") + "<br/>";
    }
    if (this.data("gxbLink")) {
      lblText += this.data("gxbLink");
    }
    if (this.data("sampleLabel")) {
      lblText += "Sample: " + this.data("sampleLabel");
    } else if (this.data("sampleId")) {
      lblText += "Sample: " + this.data("sampleId");
    }
    if (boxes && this.data("info")["Function"]) {
      boxes.attr({ selectFunction:[this.data("info")["Function"], this.data("color")] });
    }
    this.attr({ stroke:"#222", "stroke-width":2});
    tooltip($(this.node),lblText);
  };
  var tooltipHide = function() {
    if (boxes) {
      boxes.attr({ "stroke-width":0, "stroke":"none" });
    }
    if (typeof this.data("value") !== "undefined") {
      this.attr({ "stroke-width":0, stroke:"none" });
    } else {
      this.attr({ "stroke-width":1, stroke:"#999" });
    }
  };

  // Tooltip for boxes
  var boxTooltipShow = function() {
    var boxText = "";
    $.each(this.data("info"), function(lbl,val) {
      boxText += lbl + ": " + val + "<br/>";
    });
    if (this.data("moduleWiki")) {
      boxText += this.data("moduleWiki") + "<br/>";
    }
    if (this.data("gxbLink")) {
      boxText += this.data("gxbLink");
    }
    if (this.data("info")["Function"]) {
      boxes.attr({ selectFunction:[this.data("info")["Function"], this.data("color")] });
    }
    tooltip($(this.node), boxText);
  };
  var boxTooltipHide = function() {
    boxes.attr({ "stroke-width":0, "stroke":"none" });
  };

  // Tooltip for group comparison annotation
  var annotationNoHighlight = function() {
    annotationTooltip(this);
  };
  var annotationHighlight = function() {
    annotationTooltip(this);
    boxes.attr({ selectFunction:[this.data("annotation"), this.data("color")] });
  };
  var annotationTooltip = function(elt) {
    if (elt.data("annotation")) {
      var annotText = elt.data("annotation") + "<br/>";
      if (elt.data("info")) {
        annotText += elt.data("info")["# of probes"] + " probes";
      }
      tooltip($(elt.node), annotText);
    }
  };
  var annotationTooltipHide = function() {
    boxes.attr({ "stroke-width":0, "stroke":"none" });
  };
  var showAbbreviation = function(show) {
    if (show) {
      abbrevSet.attr({ "fill-opacity":1, opacity:1 });
      abbrevSet.show();
    } else {
      abbrevSet.attr({ "fill-opacity":0, opacity:0 });
      abbrevSet.hide();
    }
  };
  var showAnnotationKey = function(opacity,full) {
    var currentSet = full ? fullAnnotSet : annotSet;
    if (full) {
      fullAnnotSet.attr("fill-opacity", opacity);
      annotSet.attr("fill-opacity", 0);
      annotLabelSet.show();
      annotLabelVisible = true;
      originalHeight += annotMaxY + 40;
      bg.attr("height",originalHeight);
      scaleChart();
      $("div#abbrevShowHide").show();
    } else {
      annotSet.attr("fill-opacity", opacity);
      fullAnnotSet.attr("fill-opacity", 0);
      if (annotLabelVisible)
      {
        annotLabelSet.hide();
        originalHeight -= annotMaxY + 40;
        bg.attr("height",originalHeight);
        scaleChart();
        annotLabelVisible = false;
      }
      $("div#abbrevShowHide").hide();
    }
    fullAnnotSet.toBack();
    annotSet.toBack();
    if (opacity == 1.0) {
      currentSet.toFront();
    } else {
      currentSet.toBack();
    }
  };
  var groupMouseover = function() {
    var text = this.data("group").concat("<br/>ID: ");
    text += this.data("sampleLabel") ? this.data("sampleLabel") : this.data("sampleId");
    tooltip($(this.node),text);
  };

  // Module tooltip for 'Individual' plots
  var moduleTooltip = function() {
    var modText = "";
    $.each(this.data("info"), function(lbl,val) {
      modText += lbl + ": " + val + "<br/>";
    });
    if (this.data("moduleWiki")) {
      modText += this.data("moduleWiki") + "<br/>";
    }
    if (this.data("gxbLink")) {
      modText += this.data("gxbLink");
    }
    tooltip($(this.node), modText);
  };

  //----- DENDROGRAM -----//
  var startY = 0, endY = 0;
  var cuts = {};
  var dendroMouseover = function() {
    this.attr({ height:4 });
  };
  var dendroMouseout = function() {
    this.attr({ height:2 });
  };
  var dendroClick = function() {
    dendroCutShow(this);
  };
  var dendroCutShow = function(point) {
    var m1 = point.data("c0"), m2 = point.data("c1");
    var key = m1 + "," + m2;
    if (!cuts[key])
    {
      var height = endY - startY;
      var yOffset = activeOverlays * overlayHeight + 1;
      r.setStart();
      if (m1 != -1) { r.rect(m1-1, startY + yOffset, 1, height).attr({ fill:"#f00", stroke:"none", "stroke-width":0 }) };
      if (m2 != -1) { r.rect(m2-1, startY + yOffset, 1, height).attr({ fill:"#f00", stroke:"none", "stroke-width":0 }) };
      cuts[key] = r.setFinish();
      point.attr("fill","#f00");
    }
    else
    {
      dendroCutHide(point);
    }
  };
  var dendroCutHide = function(point) {
    var m1 = point.data("c0"), m2 = point.data("c1");
    var key = m1 + "," + m2;
    cuts[key].remove();
    delete cuts[key];
    point.attr("fill","#333");
  };

  var drawChart = function(analysisId,plotName,floor,maxFoldChange,showRowSpots,showControls) {
    r.clear();
    r.setSize(600,50);
    r.setViewBox(0,0,600,80, false);
    r.text(40,40,"LOADING PLOT ...").attr({ "font-size":40, "text-anchor":"start", "fill":"#666" });
    var args = {
      id: analysisId,
      plotName: plotName,
      floor: floor,
      maxFoldChange: maxFoldChange,
      sampleField: $("#sampleLabelOption").val(),
      showRowSpots: showRowSpots,
      showControls: showControls
    };
    $.getJSON(getBase()+"/analysis/modulePlot", args, function(json) {
      isIndividualPlot = /^individual/.test(plotName);
      if (json.error) {
      	r.clear();
    	r.setSize(600,50);
    	r.setViewBox(0,0,600,80, false);
      	var node = r.text(40,40, json.error).attr({ "font-size":32, "text-anchor":"start", "fill":"#f00"});
      	if (json.logFile) {
      		node.attr({"href": json.logFile});
      	}
      	return;
      }
      
      originalWidth = json.width;
      originalHeight = json.height;
      startY = json.startY - 2;
      endY = json.endY + 4;

      // Clear
      r.clear();
      if (isIE8) {
        r.setSize(originalWidth,originalHeight);
        r.setViewBox(0,0,originalWidth,originalHeight,true);
      } else {
        fitToScreen();
      }

      //----- CUSTOM ATTRIBUTES -----//
      // Highlights related annotations in group comparison plots
      r.ca.selectFunction = function(func,color) {
        var annot = this.data("info")["Function"];
        if (annot && annot === func) {
          return { "stroke-width":2, "stroke":color };
        } else {
          return { "stroke-width":0, "stroke":"none" };
        }
      };
      // Draws segments for pie plots
      r.ca.pieslice = function (x, y, r, a1, a2, posNeg) {
        var flag = (a2 - a1) > 180,
        clr = posNeg === 1 ? "#ff0000" : "#0000ff" ;
        a1 = (a1 % 360) * Math.PI / 180;
        a2 = (a2 % 360) * Math.PI / 180;
        return {
          path: [["M", x, y], ["l", r * Math.cos(a1), r * Math.sin(a1)], ["A", r, r, 0, +flag, 1, x + r * Math.cos(a2), y + r * Math.sin(a2)], ["z"]],
          fill: clr,
          "stroke-width": 0
        };
      };
      r.ca.sampleLbl = function(newLabels) {
        var id = this.data("sampleId");
        if (newLabels === null) {
          return { text: id };
        } else {
          return { text: newLabels[id] };
        }
      };

      if (isIndividualPlot) {
        setTimeout(function() { drawBg(json); drawModuleLabels(json); }, 0);
      } else {
        setTimeout(function() { drawBg(json); drawAxes(json); }, 0);
      }
    });
  };

  var drawBg = function(json) {
    // draw background
    bg = r.rect(0,0,originalWidth,originalHeight).attr({ fill:"#FFF", stroke:"none", "stroke-width":0 });
    plotSets.push(r.add([json.bg]));
  };

  var drawAxes = function(json) {
    if (json.xAxis) {
      plotSets.push(r.add(json.xAxis));
    }
    if (json.yAxis) {
      plotSets.push(r.add(json.yAxis));
    }
    setTimeout(function() { drawBoxes(json); }, 0);
  };

  var drawModuleLabels = function(json) {
     if (json.moduleLabels) {
      var moduleSet = r.set();
      $.each(json.moduleLabels, function(i, m) {
        var mLbl = r.text(m.x,m.y,m.text).attr(m.attr);
        $.each(m.data, function(d,value) {
          mLbl.data(d,value);
        });
        mLbl.mouseover(moduleTooltip);
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
      updateChartSize();
    }
    setTimeout(function() { drawGroupKey(json); }, 0);
  };

  var drawBoxes = function(json) {
    if (json.boxes) {
      boxes = r.set();
      $.each(json.boxes, function(i, b) {
        var box = r.rect(b.x,b.y,b.width,b.height).attr(b.attr);
        $.each(b.data, function(d,value) {
          box.data(d,value);
        });
        boxes.push(box);
      });
      boxes.mouseover(boxTooltipShow).mouseout(boxTooltipHide);
      plotSets.push(boxes);
    }
    if (json.blanks) {
      plotSets.push(r.add(json.blanks));
    }
    setTimeout(function() { drawAnnotations(json); }, 0);
  };

  var drawAnnotations = function(json) {
    // Group comparison annotations
    if (json.aBoxes) {
      annotSet = r.set();
      fullAnnotSet = r.set();
      abbrevSet = r.set();
      $.each(json.aBoxes, function(i, abox) {
        var box = r.rect(abox.x,abox.y,abox.width,abox.height).attr(abox.attr);
        var tri = r.path(abox.path).attr(abox.attr);
        $.each(abox.data, function(d,value) {
          box.data(d,value);
          tri.data(d,value);
        });
        annotSet.push(tri);
        fullAnnotSet.push(box);
        if (abox.text) {
          var abbr = r.text(abox.cx, abox.cy, abox.text).attr({ fill:"#FFF", stroke:"none", font:"14px Helvetica", "font-weight":"bold" });
          abbrevSet.push(abbr);
          fullAnnotSet.push(abbr);
        }
      });
      annotSet.mouseover(annotationHighlight).mouseout(annotationTooltipHide);
      fullAnnotSet.mouseover(annotationNoHighlight);
      plotSets.push(annotSet);
      plotSets.push(fullAnnotSet);
    }

    // Group comparison full annotation key
    if (json.annotations)
    {
      annotMaxY = 0;
      annotLabelSet = r.set();
      $.each(json.annotations, function(i, aLabel) {
        annotLabelSet.push(r.rect(aLabel.x, json.height+aLabel.y, aLabel.width, aLabel.height).attr(aLabel.attr.box));
        annotLabelSet.push(r.text(aLabel.x + 16, json.height+aLabel.y + 6, aLabel.text).attr(aLabel.attr.text));
        annotMaxY = aLabel.y;//Math.max(annotMaxY, aLabel.y);
      });
      annotLabelSet.hide();
      plotSets.push(annotLabelSet);
    }
    setTimeout(function() { drawSegments(json, -1, -1); }, 0);
  };

  var drawGroupKey = function(json) {
    if (json.groupsKey) {
      groupSet = r.set();
      $.each(json.groupsKey, function(i,g) {
        var grp = r.rect(g.x, g.y, g.width, g.height).attr(g.attr);
        $.each(g.data, function(k,v) {
          grp.data(k,v);
        });
        grp.mouseover(groupMouseover);
        groupSet.push(grp);
      });
      plotSets.push(groupSet);
    }
    setTimeout(function() { drawDendrograms(json); }, 0);
  };

  var drawDendrograms = function(json) {
    if (json.colDendrogram) {
      cuts = {};
      $.each(json.colDendrogram.lines, function(i,l) {
        var lr = r.rect(l.x,l.y,l.width,l.height).attr(l.attr);
        if (l.type === "hLine") {
          lr.data("c0", l.data.c0);
          lr.data("c1", l.data.c1);
          lr.click(dendroClick).mouseover(dendroMouseover).mouseout(dendroMouseout);
        }
      });
    }
    setTimeout(function() { drawSegments(json, -1, -1); }, 0);
  };

  var drawSegments = function(json, start, end) {
    if (json.segments) {
      var numSegments = json.segments.length;
      if (start < 0 || end < 0) {
        start = 0;
        end = json.sampleLbls ? json.sampleLbls.length : numSegments;
        segmentsSet = r.set();
      }
      while (start < end) {
        var p = json.segments[start];
        segmentsSet.push(r.path().attr({ pieslice:p.segment, stroke:"none", "stroke-width":0 }));
        start++;
      }
      if (json.sampleLbls && start < numSegments) {
        start = end;
        var newEnd = end + json.sampleLbls.length;
        end = Math.min(numSegments, newEnd);
        setTimeout(function() { drawSegments(json,start,end); }, 10);
      } else {
        plotSets.push(segmentsSet);
        setTimeout(function() { drawSpots(json, -1, -1); }, 0);
      }
    } else {
      setTimeout(function() { drawSpots(json, -1, -1); }, 0);
    }
  };

  var drawSpots = function(json, start, end) {
    if (json.spots) {
      var numSpots = json.spots.length;
      if (start < 0 || end < 0) {
        start = 0;
        end = json.sampleLbls ? json.sampleLbls.length : numSpots;
        circlesSet = r.set();
      }
      while (start < end) {
        var c = json.spots[start];
        if (typeof c !== "undefined") {
          var circle = r.circle(c.cx,c.cy,c.r).attr(c.attr);
          $.each(c.data, function(d,value) {
           circle.data(d,value);
          });
          circlesSet.push(circle);
        }
        start++;
      }
      if (json.sampleLbls && start < numSpots) {
        start = end;
        var newEnd = end + json.sampleLbls.length;
        end = Math.min(numSpots, newEnd);
        setTimeout(function() { drawSpots(json,start,end); }, 10);
      } else {
        circlesSet.mouseover(tooltipShow).mouseout(tooltipHide);
        plotSets.push(circlesSet);
        setTimeout(function() { finish(json); }, 0);
      }
    }
  };

  var finish = function(json) {
    // Initialize group comparison annotations
    if (json.aBoxes)
    {
      var showAnnotation = $("#annotationKey span.active").text();
      if (showAnnotation === "Show") {
        showAnnotationKey(1.0, false);
      } else if (showAnnotation === "Full") {
        showAnnotationKey(1.0, true);
      }
      var showAbbrev = $("#abbrevShowHide span.active").attr("id");
      if (showAbbrev === "abbrev_show") {
        showAbbreviation(true);
      } else if (showAbbrev === "abbrev_hide") {
        showAbbreviation(false);
      }

    }
    if (json.groupsKey && activeOverlays > 0)
    {
      setTimeout(function() { redrawOverlays(); }, 0);
    }
//    if (isIE8) {
//      setTimeout(function() { fitToScreen(); }, 100);
//    }
  };

  var updateSampleLabels = function(newLabels) {
    sampleLblSet.attr({sampleLbl:newLabels});
    updateChartSize();

    if (groupSet !== null)
    {
      if (newLabels === null)
      {
        groupSet.forEach(function(el) {
          el.removeData("sampleLabel");
        });
      } else {
        groupSet.forEach(function(el) {
          var id = el.data("sampleId");
          el.data("sampleLabel", newLabels[id]);
        });
      }
    }

    if (circlesSet !== null)
    {
      if (newLabels === null)
      {
        circlesSet.forEach(function(el) {
          el.removeData("sampleLabel");
        });
      } else {
        circlesSet.forEach(function(el) {
          var id = el.data("sampleId");
          el.data("sampleLabel", newLabels[id]);
        });
      }
    }
  };

  var updateChartSize = function() {
    var maxHeight = 0, maxY = 0;
    sampleLblSet.forEach(function(el) {
      maxHeight = Math.max(maxHeight,el.getBBox().height);
      maxY = el.getBBox().y;
    });
    originalHeight = maxY + maxHeight + 20;
    bg.attr("height",originalHeight);
    scaleChart();
  };

  //----- Scaling chart -----//
  var scaleChart = function() {
    if (currentScale >= 0.5)
    {
      var newWidth = originalWidth * currentScale;
      var newHeight = originalHeight * currentScale;
      r.setSize(newWidth, newHeight);
      r.setViewBox(0,0,originalWidth,originalHeight,true);
      $("div#chart").width(newWidth + 20);
//      var isFull = $("#annotationKey span.active").hasClass("annot-full");
//      var annotHeight = isFull ? 0 : annotMaxY + 40;
//      var newWidth = originalWidth * currentScale;
//      var newHeight = (originalHeight + annotHeight) * currentScale;
//      r.setSize(newWidth, newHeight);
//      r.setViewBox(0,0,originalWidth,(originalHeight + annotHeight),true);
//      $("div#chart").width(newWidth + 20);
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

  //----- Overlays -----//
  var skipLegendDraw = false;
  var colorSchemes = [5,4,3,2,1,0];
  var overlayHeight = 8;
  var activeOverlays = 0, overlays = {}, overlayOrder = {}, overlayColor = {};
  <g:each in="${params.keySet()}" var="p">
    <g:if test="${p.startsWith('categorical_overlay')}">
    <g:if test="${!(params.get(p) instanceof GrailsParameterMap)}">
    <g:set var="v" value="${params.get(p).split(':')}"/>
      overlayOrder["${p.substring(20)}"] = ${v[0]};
      overlayColor["${p.substring(20)}"] = ${v[1]};
      activeOverlays++;
    </g:if>
    </g:if>
  </g:each>
  var refreshOverlay = function(inputField) {
    var field = inputField.id;
    if ($(inputField).is(":checked"))
    {
      drawOverlay(field);
    }
    else
    {
      removeOverlay(field);
    }
  };
  var drawOverlay = function(field) {
    var scale = currentScale;
    if ($.browser.msie && $.browser.version < 9) {
      originalSize();
    }
    var colorExists = field in overlayColor;
    var colorScheme = colorExists ? overlayColor[field] : colorSchemes.pop();
    var args =
    {
      sampleSetId: sampleSetId,
      field: field,
      colorScheme: colorScheme
    };
    $.ajax({
      url: getBase()+"/charts/groupSetOverlay",
      dataType: "json",
      data: args,
      async: false,
      cache: false,
      success: function(json) {
        var categories = json.categories;
        var key = json.key, header = json.displayName;

        addLegend(field,header,categories);

        var result = {};
        $.each(json.data, function(i,point) {
          result[point.sampleId] = point[key];
        });
        var overlaySet = r.set();
        sampleLblSet.forEach(function(el) {
          var sampleId = el.data("sampleId");
          var value = result[sampleId];
          var tooltip = header.concat(": ").concat(value);
          if (value)
          {
            var color = categories[value];
            overlaySet.push(r.rect(el.data("cx"), startY - overlayHeight - 6, 39, overlayHeight).attr({ fill:color, stroke:"none" }).data("tooltip",tooltip).mouseover(overlayMouseover));
          }
          else
          {
            overlaySet.push(r.rect(el.data("cx"), startY - overlayHeight - 6, 39, overlayHeight).attr({ fill:"#eee", stroke:"none", "stroke-width":0 }));
          }
        });
        $.each(overlays, function(f,o) {
          o.translate(0,(overlayHeight+2));
        });
        if ($.browser.msie && $.browser.version < 9) {
          currentScale = scale;
          scaleChart();
        }
        overlays[field] = overlaySet;
        overlayOrder[field] = activeOverlays;
        overlayColor[field] = colorScheme;
        shiftY((overlayHeight+2));
        activeOverlays++;
        if (activeOverlays > 5) {
          $("div[name='overlay-options'] input:not(:checked)").attr("disabled","disabled");
        }
      }
    });
  };
  var removeOverlay = function(field) {
    removeLegend(field);

    overlays[field].remove();
    delete overlays[field];

    var order = overlayOrder[field];
    delete overlayOrder[field];

    colorSchemes.push(overlayColor[field]);
    delete overlayColor[field];

    $.each(overlayOrder, function(f,o) {
      if (o < order) {
        overlayOrder[f]--;
        overlays[f].translate(0,-(overlayHeight+2));
      }
    });
    shiftY(-(overlayHeight+2));

    activeOverlays--;
    if (activeOverlays < 6) {
      $("div[name='overlay-options'] input:not(:checked)").removeAttr("disabled");
    }
  };
  var overlayMouseover = function() {
    tooltip($(this.node),this.data("tooltip"));
  };
  var shiftY = function(y) {
    sampleLblSet.translate(-y,0);
    $.each(plotSets, function(i,s) {
      s.translate(0,y);
    });
    $.each(cuts, function(i,cut) {
      cut.translate(0,y);
    });
    originalHeight += y;
    bg.attr("height",originalHeight);
    scaleChart();
  };
  var redrawOverlays = function() {
    var cats = new Array();
    $.each(overlayOrder, function(k,i) {
      cats[i] = k;
    });
    overlays = {};
    overlayOrder = {};
    activeOverlays = 0;
    skipLegendDraw = true;
    $.each(cats, function(i,field) {
      drawOverlay(field);
    });
    skipLegendDraw = false;
  };
  var addLegend = function(field,label,categories) {
    if (!skipLegendDraw)
    {
      var link = 'exportLegend(\''+field+'_key\');';
      $("div.groupKey #legends").append('<div id="'+field+'_keylabel" class="key-label"><span class="key-label-title">'+label+'</span><span class="icon_download-small" style="margin-left:5px;" onclick="'+link+'"></span></div>');
      var legendHtml = '<div id="'+field+'_key" class="key">';
      $.each(categories, function(cat,clr) {
        legendHtml += '<div class="row"><span class="swatch" style="background-color:'+clr+';"></span><div class="span3">'+cat+'</div></div>';
      });
      legendHtml += '</div>';
      $("div.groupKey #legends").append(legendHtml);
    }
  };
  var removeLegend = function(field) {
    $("div.groupKey #legends div[id='"+field+"_keylabel']").detach();
    $("div.groupKey #legends div[id='"+field+"_key']").detach();
  };
</g:javascript>

