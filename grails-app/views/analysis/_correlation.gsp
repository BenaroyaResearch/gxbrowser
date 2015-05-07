<div id="correlationPanel" class="pullout" style="width:500px;">
  <div class="page-header">
    <h4 style="max-width:100%">Correlation Histograms</h4>
  </div>
  <div style="text-align:left;">
    <div style="float: left"><strong>Correlate: </strong>
    <select name="correlationSelect" id="correlationSelect" onchange="javascript:renderCorrelation();">
      <option value="none" selected="selected">-select a clinical variable-</option>
      <g:each in="${overlays.categorical+overlays.numerical}" var="cVar">
        <option value="${cVar.collection}_${cVar.key}">${cVar.displayName}</option>
      </g:each>
    </select>
    </div>
    <div class="correlations-options">
    <span title="click to choose a color for the histogram" class="ui-icon-color-swatch" onclick="toggleSwatches(event);"></span>
    <g:render template="/common/default_swatches_template"/>
    <span class="icon_download-small" title="Download the correlation plot as an image" onclick="saveCorrelationChart();"></span>
    </div>
  </div>
  <div style="height:400px;width:460px;">
    <div id="correlationPlot"></div>
  </div>
  <div class="button-actions" style="clear: both;">
    <button class="btn" onclick="closeCorrelationPanel();">Close</button>
  </div>
</div>
<g:javascript>
  var rgb2hex = function(rgb) {
    rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
    return "#" +
      ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) +
      ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) +
      ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
  };
  var toggleSwatches = function(e) {
    if (!e) {
      e = window.event;
    }
    var hidden = $("#presetColors").toggle().is(":hidden");
    if (!hidden) {
      $('#presetColors').position({
        my: 'right top',
        of: '.ui-icon-color-swatch',
        at: 'right bottom',
        offset: '0 2'
      });
    }
    e.cancelBubble = true;
    e.returnValue = false;

    if (e.stopPropagation) {
      e.stopPropagation();
      e.preventDefault();
    }
  };
  var showCorrelationPanel = function() {
    $("div#correlationPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
  };
  var closeCorrelationPanel = function() {
    $("div#correlationPanel").hide().css({ top:0, left:0 });
  };
  var analysisId = ${analysis.id};
  var corPaper = Raphael("correlationPlot", 100, 100);
  var barSet = corPaper.set();
  var renderCorrelation = function() {
    var field = $("#correlationSelect").val();
    clearHistogram();
    if (field !== "none") {
      drawHistogram(field);
    }
  };
  var plotSettings = function() {
    var div = $("div#correlationPlot");
    var width = div.width() - 20;
    var height = 380;
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
  var clearHistogram = function() {
    corPaper.clear();
    barSet.clear();
  };
  var drawHistogram = function(field) {
    var settings = plotSettings();
    var x = settings.yAxisMargin + (settings.buffer / 2);
    var y = settings.topMargin + settings.buffer;

    corPaper.setSize(settings.width + 20, settings.height);
    corPaper.rect(0,0,settings.width + 20,settings.height).attr({ fill:"#fff", stroke:"none", "stroke-width":0 });

    var args = { analysisId: analysisId, field:field };
    $.getJSON(getBase()+"/analysis/correlation", args, function(json) {
      var max = json.max;
      if (max > 0) {
        var xTickHeight = settings.height - settings.xAxisMargin + 14;
        corPaper.text(x, xTickHeight, -1).attr(settings.xTickAttr);
        // draw the bars
        $.each(json.bars, function(i, bar) {
          if (bar.count > 0) {
            var barHeight = bar.count / max * settings.pHeight;
            var yOffset = settings.pHeight - barHeight;
            barSet.push(corPaper.rect(x, y + yOffset, settings.barWidth, barHeight).attr(settings.barAttr));
          }
          x += settings.barWidth;
          corPaper.text(x, xTickHeight, bar.data.uRange).attr(settings.xTickAttr);
        });

        // draw the axes
        corPaper.rect(settings.yAxisMargin, settings.topMargin + settings.buffer, 2, settings.pHeight - 1).attr(settings.axesAttr);
        corPaper.text(settings.yAxisMargin - 5, settings.topMargin + settings.buffer, max).attr(settings.yTickAttr);
        corPaper.text(settings.yAxisMargin - 5, settings.height - settings.xAxisMargin, 0).attr(settings.yTickAttr);
        corPaper.rect(settings.yAxisMargin, settings.height - settings.xAxisMargin - 1, settings.pWidth, 2).attr(settings.axesAttr);
      }
    });
  };
  var saveCorrelationChart = function() {
    var field = $("#correlationSelect").val();
    if (field !== "none") {
      var svg = null;
      if ($.browser.msie && $.browser.version < 9) {
        svg = corPaper.toSVG();
      } else {
        svg = $("div#correlationPlot").html();
      }
      $.post(getBase()+"/charts/saveImg", { svg:svg }, function(id) {
        var location = getBase()+"/charts/downloadImg/"+id;
        var filename = "analysis_".concat(analysisId).concat("correlation_").concat(field);
        window.location.href = location.concat("?filename=").concat(filename);
      });
    }
  };
  $(document).ready(function() {
    $(".preset-color").click(function() {
      var hex = $(this).find('div').css('background-color');
      if (!/^#.*/.test(hex)) {
        hex = rgb2hex(hex);
      }
      barSet.attr({ fill:hex, stroke:hex });
    });
  });
</g:javascript>