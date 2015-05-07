<!DOCTYPE html>
<%@ page import="grails.converters.JSON; org.sagres.sampleSet.SampleSet; org.sagres.mat.Analysis"%>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta name="layout" content="matmain">
  <title>Module Plot: ${title}</title>
  
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
  <style type="text/css">
    .icon.settings:before {
      margin: -0.15em 0.75em 0 -0.25em;
    }
    .groupKey #legends {
      max-height: 200px;
      overflow-x: hidden;
      overflow-y: auto;
    }

  </style>
  <g:javascript>
    var plotId = "${plotId}";
    var fileType = "${fileType}";
    <g:if test="${groups}">
      var groups = ${groups as JSON};
    </g:if>
    <g:else>
      var groups = null;
    </g:else>
    var currentPlot = "${defaultPlot}";
    var plotKeyLabel = null;
    var generateArgs = function() {
      var args =
      {
        _controller: "visualizer",
        _action: "matPlot",
        _id: "",
        plotId: plotId,
        fileType: fileType
      };
      return args;
    };
    function filter(pvalue) {
      if (plotKeyLabel !== null) {
        plotKeyLabel.attr("text", "% probe sets with p < " + pvalue);
      }
      var showRowSpots = $("input#show-row-spots").is(":checked");
      var mts = $("input#mts").is(":checked");
      drawChart(currentPlot,mts,pvalue,showRowSpots);
    };

    function loadInteractiveImage(name,mts,floor,showRowSpots) {
      currentPlot = name;
      setTimeout(function() { drawChart(name,mts,floor,showRowSpots); }, 0);
    };

    var exportPlotKey = function() {
      var svg = null;
      if ($.browser.msie && $.browser.version < 9) {
        svg = plotKeyR.toSVG();
      } else {
        svg = $("div#plotkey").html();
      }
      $.post(getBase()+"/charts/saveImg", { svg: svg }, function(id) {
        var location = getBase()+"/charts/downloadImg/"+id;
        var filename = "Plot Key";
        window.location.href = location.concat("?filename=").concat(filename);
      });
    };

    var exportGroupKey = function(divId) {
      var svg = null;
      if ($.browser.msie && $.browser.version < 9) {
        svg = fieldLegends[divId].toSVG();
      } else {
        svg = $("div#"+divId).html();
      }
      $.post(getBase()+"/charts/saveImg", { svg: svg }, function(id) {
        var location = getBase()+"/charts/downloadImg/"+id;
        var filename = "Groups Key";
        window.location.href = location.concat("?filename=").concat(filename);
      });
    };
    var generateLink = function(showEmail,isClient) {
      var args = generateArgs();
      $.getJSON(getBase()+"/miniURL/create", args, function(json) {
        if (showEmail || isClient) {
          var subject = "Module Visualizer Tool Link for '${title}'";
          var text = "Hello,\r\n\r\n I thought you'd be interested in looking at the analysis '${title}' in the Module Visualizer Tool. \r\n\r\n" +
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

<body itemscope itemtype="http://schema.org/Article">

<g:javascript src="jquery.qtip.min.js"/>
<g:javascript src="modules/raphael.js"/>
<g:javascript src="modules/raphael.export.js"/>

<div class="mat-container">

  <div class="mat_sidecol">

    <div id="plot-type-well" class="well indView" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Plot Type</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'plot-type-options');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="plot-type-options">
        %{--<div id="plot_type_chooser" class="plot-group-ind-chooser">--}%
          %{--<div class="button-group result" id="group_ind" name="group_ind" style="margin-top:10px;">--}%
            %{--<span class="button pill" id="group" name="group">Group</span>--}%
            %{--<span class="button pill" id="ind" name="individual">Individual</span>--}%
          %{--</div>--}%
        %{--</div>--}%

        %{--<div id="group_options" class="plotoptions groupView" style="text-align:center;">--}%
          %{--<span name="difference" title="Differential expression analysis carried out via linear models for microarray data (LIMMA)">Difference Plot</span>--}%
        %{--</div>--}%

        <div id="ind_options" class="plotoptions">
          <div id="cluster_options">
            <span class="filterlabel">Cluster columns by:</span>
            <div class="button-group result" name="col_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf" name="none" title="Do not cluster columns">None</span>
              <span class="button secondhalf" name="samples" title="Cluster columns by samples">Samples</span>
            </div>

            <span class="filterlabel">Cluster rows by:</span>
            <div class="button-group result" name="row_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf" name="none" title="Do not cluster rows">None</span>
              <span class="button secondhalf" name="modules" title="Cluster rows by modules">Modules</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div id="display-options-well" class="well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Display Options</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="javascript:minMax(this,'display_options');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="display_options" class="plotoptions display-options">
        <g:if test="${grailsApplication.config.mat.top.only}">
        	<div class="button-group result groupView" name="num_modules" style="margin-top:10px;">
		  		<span class="button firsthalf" name="top"  title="Most general modules created in the first six rounds of algorithm">Top Modules</span>
          		<span class="button secondhalf" name="all" title="All Modules">All Modules</span>
			</div>
        </g:if>
        <g:else>	          	
        	<div class="button-group result groupView" name="num_modules" style="margin-top:10px; display: block; text-align: center;">
		  		<span class="button" name="top" style="float:none;" title="Most general modules created in the first six rounds of algorithm">Top Modules</span>
			</div>
        </g:else>
        <div></div>
        <div class="button-group result indView" name="ind_num_modules" style="margin-top:10px;margin-left:0;margin-right:0;display:none;">
          <span class="button firsthalf" name="all" title="All modules">All</span>
          <span class="button secondhalf" name="annotated" title="Only those modules with a defined gene ontology">Annotated</span>
        </div>
        <div></div>
        <div class="button-group result" name="display_type" style="margin-left:0;">
          <span class="button firsthalf" name="spot" title="Significance or intensity noted by color hue">Spot Chart</span>
          <span class="button secondhalf" name="piechart" title="Percent with fold change up and/or down noted by color">Pie Chart</span>
        </div>
        <div class="groupView" id="pvalue-slider">
          <div class="filter-slider-caption" style="margin-bottom:10px;"><g:checkBox name="mts" checked="true" value="checked" onchange="javascript:updateMts();"/> Multiple Testing Correction</div>
          <div class="filter-slider-caption">Count probes with p-value &lt <span id="floor-pvalue">${params.pvalue ?: 0.05}</span></div>
          <div class="max-slider">
            <div id="slider-pvalue" class="slider-range-max"></div>
          </div>
          <div class="center reset-slider">
            <button class="button reset-value" onclick="resetPValue();">Reset p-value</button>
          </div>
        </div>
        <div class="indView" id="filter-slider" style="display:none;">
          <div class="filter-slider-caption">Show modules with at <br />least one sample > <span id="floor-value">${params.filter ?: 0}</span>%</div>
          <div class="max-slider">
            <div id="slider-filter" class="slider-range-max"></div>
          </div>
        </div>
        <div id="row-spots" class="toggle-spotrows indView" style="display:none;"><label for="show-row-spots"><g:checkBox name="show-row-spots" onclick="javascript:updateSpots();" checked="${params.showRowSpots ?: true}"/> <span>Show all spots in row on filter</span></label></div>
        <g:javascript>
          var updateSpots = function() {
            var floor = $("span#floor-value").text();
            if (floor != 0)
            {
              filter(floor);
            }
          };
          $("#slider-filter").slider({
            range: "max",
            min: 0,
            max: 100,
            value: ${params.filter ?: 0},
            slide: function(event, ui) {
              $("span#floor-value").html(ui.value);
            },
            stop: function(event, ui) {
              filter(ui.value);
            }
          });
          var updateMts = function() {
            var floor = $("span#floor-pvalue").text();
            if (floor != 0)
            {
              filter(floor);
            }
          };
          $("#slider-pvalue").slider({
            range: "max",
            min: 0,
            max: 1,
            step: 0.01,
            value: ${params.pvalue ?: 0.05},
            slide: function(event, ui) {
              $("span#floor-pvalue").html(ui.value);
              $("span[name='pvalue-text']").html(Math.round(ui.value * 100));
              $("span[name='pvalue-text-orig']").html(ui.value);
            },
            stop: function(event, ui) {
              filter(ui.value);
            }
          });
          var resetPValue = function() {
            var origPval = ${params.pvalue ?: 0.05};
            if ($("#slider-pvalue").slider("option", "value") !== origPval) {
              $("#slider-pvalue").slider("option", "value", origPval);
              $("span#floor-pvalue").html(origPval);
              $("span[name='pvalue-text']").html(origPval * 100);
              $("span[name='pvalue-text-orig']").html(origPval);
              filter(origPval);
            }
          };
        </g:javascript>
      </div>
    </div>

    %{-- SHOW ANNOTATION KEY UI - ONLY FOR GROUP COMPARISON --}%
    <div id="annotations-well" class="annotationKey well groupView" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Annotation Key</span>
         <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'annotationKey');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="annotationKey" class="button-group" style="margin-top:10px;margin-left:0px;" name="annotation_key">
        <span id="annot_show" class="button annot-show" onclick="javascript:showAnnotationKey(1.0,false);">Show</span>
        <span id="annot_hide" class="button annot-hide" onclick="javascript:showAnnotationKey(0,false);">Hide</span>
        <span id="annot_full" class="button annot-full" onclick="javascript:showAnnotationKey(1.0,true);">Full</span>
      </div>

      <div id="abbrevShowHide" class="button-group" style="margin-top:10px;margin-left:0px;display:none;" name="abbrevShowHide">
        <strong>Show/Hide Abbreviations</strong>
        <span id="abbrev_show" class="button abbrev-show" onclick="javascript:showAbbreviation(true);">Show</span>
        <span id="abbrev_hide" class="button abbrev-hide active" onclick="javascript:showAbbreviation(false);">Hide</span>
      </div>
    </div>

    <div class="groupKey well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Legend</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'legends');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="legends">
      </div>
    </div>

    <div class="spacer"></div>
    %{-- PLOT KEY UI --}%
    <div id="plotkey-well" class="plotkey well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Plot Key</span>
        <div class="ui-icons">
          <span class="icon_download-small" onclick="exportPlotKey();"></span>
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'plotkey');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="plotkey" style="margin-top:10px;"></div>
    </div>

    <g:javascript>
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

      var plotKeyR = Raphael("plotkey", 200, 300);
      var circleSet = null;

      var modPlotKey = function() {
        plotKeyR.clear();
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

        plotKeyLabel = plotKeyR.text(96,295,"% probe sets with p < 0.05").attr({ "font-size":12, "fill":"#333333" });

        currentPlotKey = "difference";
      };

      var getPlotName = function() {
        // build plot name from UI buttons
        var plotInfo = currentPlot.split("_");
//        var groupInd = $("div[name='group_ind'] .button.active").attr("name");
        var groupInd = plotInfo[0];
        if (groupInd === "group") {
          var numModules = $("div[name='num_modules'] .button.active").attr("name");
          var displayType = $("div[name='display_type'] .button.active").attr("name");
          var plotname = groupInd.concat("_difference_").concat(numModules).concat("_").concat(displayType);
          return plotname;
        } else if (groupInd === "individual") {
          var numModules = $("div[name='ind_num_modules'] .button.active").attr("name");
          var displayType = $("div[name='display_type'] .button.active").attr("name");
          var plotname = groupInd.concat("_");
          var colClusterType = $("div[name='col_cluster_type'] .button.active").attr("name");
          var rowClusterType = $("div[name='row_cluster_type'] .button.active").attr("name");
          plotname = plotname.concat(numModules).concat("_").concat(displayType).concat("_").concat(colClusterType).concat("_").concat(rowClusterType);
          return plotname;
        }
      };

      var updateChoosers = function() {
        var plotInfo = currentPlot.split("_");
        var isGroup = plotInfo[0] === "group";
        if (isGroup) {
          $(".groupView").show();
          $(".indView").hide();

          $("div[id='group_options'] div[name='analysis_type'] .button[name='"+plotInfo[1]+"']").addClass("active");
          $("div[name='num_modules'] .button[name='"+plotInfo[2]+"']").addClass("active");
          $("div[name='display_type'] .button[name='"+plotInfo[3]+"']").addClass("active");

          $("div[name='ind_num_modules'] .button[name='all']").addClass("active");
          $("div[name='col_cluster_type'] .button[name='none']").addClass("active");
          $("div[name='row_cluster_type'] .button[name='none']").addClass("active");
        } else {

          $(".indView").show();
          $(".groupView").hide();

          $("div[name='ind_num_modules'] .button[name='"+plotInfo[1]+"']").addClass("active");
          $("div[name='display_type'] .button[name='"+plotInfo[2]+"']").addClass("active");
          $("div[name='col_cluster_type'] .button[name='"+plotInfo[3]+"']").addClass("active");
          $("div[name='row_cluster_type'] .button[name='"+plotInfo[4]+"']").addClass("active");

          $("div[name='num_modules'] .button[name='top']").addClass("active");
          $("div[name='display_type'] .button[name='spot']").addClass("active");
        }
//        $("div[name='group_ind'] .button[name='"+plotInfo[0]+"']").addClass("active").trigger("click");
      };

      var updatePlot = function() {
        // draw plot
       	var plotInfo = currentPlot.split("_");
//        var groupInd = $("div[name='group_ind'] .button.active").attr("name");
        var groupInd = plotInfo[0];
        var plotName = getPlotName();
        var floor = groupInd === "group" ? $("span#floor-pvalue").text() : $("span#floor-value").text();
        var showRowSpots = $("input#show-row-spots").is(":checked");
        var mts = $("input#mts").is(":checked");
        loadInteractiveImage(plotName,mts,floor,showRowSpots);
      };

      $(".button-group .button").click(function() {
        var btn = $(this);
        var id = btn.attr("id");
        if (!btn.hasClass("disable"))
        {
          btn.siblings().removeClass("active");
          btn.addClass("active");

          var parentName = btn.parent().attr("name");
          if (parentName === "group_ind") {
            var hideView = id === "group" ? ".indView" : ".groupView";
            $(hideView).hide();
            $("."+id+"View").show();
          }

          if (parentName !== "annotation_key" && parentName !== "abbrevShowHide")
          {
            updatePlot();
          }
        }
      });
    </g:javascript>
  </div><!--end mat_sidecol -->

  <div class="mat_maincol">

    <h2 itemprop="name">${title}</h2>

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
      <g:render template="mat"/>
    </div><!--end imagebody-->
	<span itemprop="description" style="display: none">A module plot of ${title}</span>

    <!-- javascript coding -->
    <g:javascript>
      // What is $(document).ready ? See: http://flowplayeplotKeyplotKeyR.org/tools/documentation/basics.html#document_ready
      $(document).ready(function() {
        $("#annotationKey span#${params.annotationKey ?: 'annot_show'}").addClass("active");
        updateChoosers();
        modPlotKey();
        if (groups) {
          addLegend("groups", "Groups", groups);
        } else {
          $("div.groupKey").hide();
        }
        filter(${params.pvalue ?: 0.05});
        updatePlot();
      });
    </g:javascript>

  </div><!--end mat_maincol-->

</div><!--end mat-container-->
<g:render template="/common/emailLink"/>
<g:render template="/common/bugReporter" model="[tool:'Visualizer']"/>
</body>
</html>