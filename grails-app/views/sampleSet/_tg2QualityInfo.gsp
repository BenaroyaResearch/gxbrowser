<%@ page import="java.sql.Timestamp" %><g:if test="${tg2QualityInfo.isEmpty()}">
<div>No matching samples found in TG2</div>
</g:if>
<g:else>
<g:if test="${dataKeys}">
<g:javascript src="highcharts/highcharts.js"/>
<g:javascript src="highcharts/exporting.js"/>
<g:javascript src="charting/chartify.js"/>
<g:javascript>
  $(document).ready(function() {
//    var goToPoint = function() {
//      var sampleId = this.name;
//      $("tr#"+sampleId).scrollTop();
//    };

    function drawChart() {
      var chartType = $("a.chartType-button.active").attr("id");
      var dataKey = "";
      $("a.datakey-button.active").each(function(i, button) {
        if (dataKey !== "")
        {
          dataKey += ",";
        }
        dataKey += button.id;
      });
      var args = { sampleSetId: ${sampleSet.id}, dataKeys: dataKey };
      $.get(getBase()+"/charts/getTg2Histogram", args, function(result) {
        if (result.data != null)
        {
          var chartWidth = $("#tg2Chart").closest(".sampleset-container").width();
          var pointWidth = null;
          var keyWidth = (chartType === "drawHistogram") ? result.numKeys : 1 ;
          var maxChartWidth = 7 + ((result.numPoints + result.numGroups - 1) * 12 * keyWidth);
          if (maxChartWidth < chartWidth)
          {
            maxChartWidth = chartWidth;
          }
          else
          {
            pointWidth = 5;
          }
          $("#tg2Chart").chartify(chartType, { data: result.data, showExport: false, showYLastLabel: false, showYLabels: false, showLegend: true,
            yTickInterval: result.max, yTickWidth: 0, minY: 0, maxY: (result.max * 1.1), colorByPoint: false, shadow: false, borderWidth: 0, lineWidth: 1,
            markerRadius: 2, showXLabels: false, xTickWidth: 0, chartWidth: maxChartWidth, pointWidth: pointWidth });
        }
      });
    }

    $(".chartType-button").bind("click", function() {
      $("a.chartType-button.active").each(function() {
        $(this).removeClass("active");
      });
      $(this).addClass("active");
      drawChart();
    });
    $(".datakey-button").bind("click", function() {
      var numKeys = $("a.datakey-button.active").length;
      if (!(numKeys === 1 && $(this).hasClass("active")))
      {
        $(this).toggleClass("active");
        $(this).blur();
        drawChart();
      }
    });
    drawChart();
  });
</g:javascript>
<div style="text-align:center;">
  <div class="button-group">
    <a id="drawHistogram" href="#" class="chartType-button button active">Histogram</a>
    <a id="drawLineChart" href="#" class="chartType-button button">Line Chart</a>
  </div>
  <div class="button-group">
    <g:each in="${dataKeys}" var="datakey">
      <a id="${datakey}" href="#" class="datakey-button button pill ${datakey == "biotin" ? "active" : ""}">${datakey.encodeAsHumanize()}</a>
    </g:each>
  </div>
  <div id="tg2Chart" style="height:200px;width:100%;overflow-x:auto;overflow-y:hidden;"></div>
</div>
</g:if>
<div class="scrollable-container no-wrap-content" style="max-height: 600px">
<table class="zebra-striped pretty-table" id="tg2-samplequality-table">
  <thead>
  <tr>
    <g:each in="${tg2QualityHeaders}" var="header">
      <th>${header.encodeAsHumanize()}</th>
    </g:each>
  </tr>
  </thead>
  <tbody>
    <g:each in="${tg2QualityInfo}" var="tg2Values">
      <tr id="${tg2Values.get("sample_id")}">
        <g:each in="${tg2QualityHeaders}" var="h">
          <td>
            <g:if test="${tg2Values.get(h) instanceof Timestamp}">
              <g:formatDate date="${tg2Values.get(h)}" format="yyyy-MM-dd"/>
            </g:if>
            <g:else>
              ${tg2Values.get(h)}
            </g:else>
          </td>
        </g:each>
      </tr>
    </g:each>
  </tbody>
</table>
</div>
</g:else>