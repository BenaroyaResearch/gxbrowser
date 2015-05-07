<%@ page import="org.sagres.sampleSet.SampleSet" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="layout" content="sampleSetMain"/>

<title>Sample Set Sample Groups</title>

<link rel="stylesheet" href="${resource(dir: 'css', file: 'box.css')}"/>
<g:javascript src="d3/d3.js"/>
<g:javascript src="d3/d3.chart.min.js"/>

<!--[if IE]><g:javascript src="canvasXpress/excanvas.js"/><![endif]-->
<g:javascript src="canvasXpress/sprintf.min.js"/>
<g:javascript src="canvasXpress/canvasXpress.custom.min.js"/>
<g:javascript src="highcharts/highcharts.js"/>
<g:javascript src="highcharts/exporting.js"/>
<g:javascript src="charting/chartify.js"/>
<g:javascript src="charting/sampleSetCharts.js"/>
<g:javascript>

  $(document).ready(function() {

    var cxMouseOver = function(data, event) {
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
    }

    function getArgs() {
      return { sampleSetId: $("#sampleSets").val(), groupSetId: $("#groupSets").val(), probeId: $("#probeIds").val() };
    }

    $("#drawBoxplots").click(function() {
      drawCxBoxplot(getArgs(), { showLegend: true });
    });

    $("#drawHistogram").click(function() {
      drawCxHistogram(getArgs(), {});
    });

    $("#drawHighchartsHistogram").click(function() {
      drawHistogram(getArgs(), {});
    });

    $("#drawHighchartsLineChart").click(function() {
      drawLineChart({ groupSetId: $("#groupSets").val() }, {});
    });

    $("#drawLineChart").click(function() {
      drawCxLineChart({ groupSetId: $("#groupSets").val() }, {});
    });

    $("#drawD3Boxplots").click(function() {
      drawD3Boxplot(getArgs(), {});
    });

    $("#drawHighchartsBipolarChart").click(function() {
      drawBipolarChart(getArgs(), {});
    });

    $("#sampleSets").change(function() {
      var sampleSetId = $(this).val();
      $.ajax({
        type: 'GET',
        url: 'getDatasetGroups',
        data: { sampleSetId: sampleSetId },
        async: false,
        cache: false,
        success: function(result) {
          var options = "<option value='-select group set-'>-select group set-</option>";
          for (var i = 0; i < result.length; i++)
          {
            options += "<option value='" + result[i].id + "'>" + result[i].name+ "</option>";
          }
          $("#groupSets").html(options);
        }
      });
    });

    $("#groupSets").change(function() {
      var groupSetId = $(this).val();
      $.ajax({
        type: 'GET',
        url: 'getProbeIds',
        data: { groupSetId: groupSetId },
        async: false,
        cache: false,
        success: function(result) {
          var options = "<option value='-select probe id-'>-select probe id-</option>";
          for (var i = 0; i < result.length; i++)
          {
            options += "<option value='" + result[i] + "'>" + result[i] + "</option>";
          }
          $("#probeIds").html(options);
        }
      });
    });

  });
</g:javascript>

</head>

<body>
<div class="sampleset-container">
  <p>Select a sample set, group set and probe id:</p>
<g:select name="sampleSets" from="${SampleSet.list()}" optionKey="id" optionValue="name" noSelection="['-select sample set-':'-select sample set-']" value="null"/>
<g:select name="groupSets" from="${[]}" noSelection="['-select group set-':'-select group set-']" value="null"/>
<g:select name="probeIds" from="${[]}" noSelection="['-select probe id-':'-select probe id-']" value="null"/>

<div class="top-margin">
  <p>Click a button below to draw the charts:</p>
<button id="drawHistogram" class="btn small">Draw CanvasXPress Histogram</button>
<button id="drawLineChart" class="btn small">Draw CanvasXPress Line Chart</button>
<button id="drawBoxplots" class="btn small">Draw CanvasXPress Boxplots</button>
<button id="drawHighchartsHistogram" class="btn small">Draw Highcharts Histogram</button>
<button id="drawHighchartsLineChart" class="btn small">Draw Highcharts Line Chart</button>
<button id="drawHighchartsBipolarChart" class="btn small">Draw Highcharts Bipolar Chart (Example Data)</button>
<button id="drawD3Boxplots" class="btn small">Draw d3 Boxplot</button>
  </div>

  <div id="chart"></div>
</div>



</body>
</html>

