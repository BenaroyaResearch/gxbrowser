<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta name="layout" content="matmain">
  <title>MetaCat Scatter: ${title}</title>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
<style>
	.module-group span {
		display: inline-block;
		width: 66px;
		margin-bottom: 5px;
	}

	.module-group span:first-child {
		margin-left: 15px;
	}
	.ui-tooltip-content a {
      color: #F1D031;
    }
    .ui-tooltip-content p {
		font-size: 11px;
    }
    .qtip-wide {
    	max-width: 600px;
    }
	.studyinfo {
		margin-left: 5px;
		margin-top:  2px;
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

	path { 
		stroke-width: 1;
		fill: none;
	}
	
	.axis path,
	.axis line {
	  fill: none;
	  stroke: #000;
	  shape-rendering: crispEdges;
	}
	div.tooltip {   
	  position: absolute;           
	  text-align: center;           
	  width: 40px;                  
	  height: 14px;                 
	  padding: 2px;             
	  font: 12px sans-serif;        
	  background: lightsteelblue;   
	  border: 0px;      
	  border-radius: 8px;           
	  pointer-events: none;         
	}
	.dot {
	  stroke: #000;
	  opacity: 0.7;
	}
	.line {
	  stroke: #000;
	  opacity: 1.0;
	}
	
	.value {
	  stroke: #f00;
	  opacity: 1.0;
	  stroke-width: 2;
	}
	
	.tick {
	  font: 10px sans-serif;
	}
	.grid .tick {
      stroke: red;
      opacity: 0.5;
	}
	
	.groupKey #legends {
      max-height: 600px;
      overflow-x: hidden;
      overflow-y: auto;
    }
	
    #pickModules-modal {
		width: 900px; /* SET THE WIDTH OF THE MODAL */
		margin: -200px 0 0 -450px; /* CHANGE MARGINS TO ACCOMMODATE THE NEW WIDTH (original = margin: -250px 0 0 -280px;) */
	}

</style>
 <g:javascript>
    var metaCatId = ${params.id};
    var currentSort = "none";
    var minWidth = 1000;
    var caseCount = ${metaCatInstance?.noCases ?: 100};
    var maxDepth = 0;
    var currentDepth = 0;
    var colLevel = 0;
    var currentPlot = "${params.currentPlot ?: 'individual_difference_annotated_spot_scatter_sample'}";
	var isLine = false;
	var isCustom = false;
    var customModules = [];
    var moduleMap = {};
	var topicContainer;
	var buttonContainer;
</g:javascript>
</head>
<body>

<g:javascript src="jquery.qtip.min.js"/>
<g:javascript src="bootstrap-modal.js"/>
<g:javascript src="bootstrap-dropdown.js"/>
<script src="http://d3js.org/d3.v3.min.js"></script>

<div class="mat-container">

  <div class="mat_sidecol">
  
  
	<div id="plot-type-well" class="well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Plot Type</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'plot-type-options');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="plot-type-options">
      
      <div id="plot_type_chooser" class="plot-group-ind-chooser">
        <div class="button-group result" id="group_ind" name="group_ind" style="margin-top:10px;">
<%--          <g:if test="${grailsApplication.config.mat.access.on}">--%>
<%--          	<span class="button pill" id="group" name="group">Group</span>--%>
<%--          	<span class="button pill" id="ind" name="individual">Individual</span>--%>
<%--          </g:if>--%>
<%--          <g:else>--%>
<%--          	<span class="button" id="group" name="group">Group</span>--%>
<%--          </g:else>--%>
		<span class="button pill" id="ind" name="individual">Individual</span>
        </div>
       </div>
       <div id="group_options" class="plotoptions groupView" style="display:none;">
        <div class="button-group result" name="analysis_type">
          <g:if test="${isFocusedArray}">
            <span class="button nobutton fullwidth active" name="focusedarray" title="Focused array...">Fold Change Plot</span>
          </g:if>
          <g:else>
            <span class="button nobutton fullwidth active" name="difference" title="Differential expression analysis carried out via linear models for microarray data (LIMMA)">Difference Plot</span>
            <span class="button secondhalf" name="gsa" title="Gene set analysis (GSA)" style="display:none;">Gene Set</span>
          </g:else>
        </div>
      </div>
        
        <div id="clustering" class="plotoptions">
          <div id="cluster_options">
          <div style="display:none;">
            <span class="filterlabel">Cluster columns by:</span>
            <div class="button-group result" name="col_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf" name="none" title="Do not cluster columns">None</span>
              <span class="button secondhalf" name="samples" title="Cluster columns by analyses">Samples</span>
            </div>
			</div>
            <span class="filterlabel">Sort rows by:</span>
            <div class="button-group result" name="row_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf" name="sample" title="Sort rows by sample">Sample Id</span>
              <span class="button secondhalf"  name="variance" title="Sort rows by variance">Module Variance</span>
              <span class="button firsthalf"  name="signed" title="Sort rows by signedvariance">Signed Variance</span>
              <span class="button secondhalf nodraw"  name="custom" title="Sort rows by custom features">Custom</span>
            </div>
            <div name="overlay-options" class="clinical-data" >
            <span class="button clinical-data-options" title="Select clinical data to show" style="text-align:left;">Clinical Data <span class="button-arrow"></span></span>
            <ul class="clinical-data-optionlist">
            </ul>
          </div>
          <g:javascript>
            $("div[name='overlay-options'] span.button").click(function() {
              $("ul.clinical-data-optionlist").toggle();
            });
            var updateOverlays = function() {
              var args = { metaCatId: metaCatId };
              $.getJSON(getBase()+"/analysis/scatterSortOptions", args, function(json) {
                if (json) {
                  var html = "";
                  $.each(json, function(i, cat) {
                    var key = cat.collection + "_" + cat.key;
                    var key = cat.key;
                    html += '<li><label>';
                    html += '<input type="radio" class="scatterSort" name="scatterSort" value="' + key + '" onclick="javascript:refreshSort(this);"/>';
                    html += ' <span>' + cat.displayName + '</span>';
                    html += '</label></li>';
                  });
                  $("ul.clinical-data-optionlist").html(html);
                } else {
                  // console.log("no scatter sort options");
                  $("li#correlation-menu").detach();
                  $("div[name='overlay-options']").detach();
                }
              });
            };
            setTimeout(function() { updateOverlays(); }, 0);
          </g:javascript>
            
          </div>
        </div>
	</div> <!-- plot-type-options -->
	</div> <!-- plot-type-well -->

      <div id="display-options-well" class="well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Display Options</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="javascript:minMax(this,'display_options');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="display_options" class="plotoptions display-options">
        <div class="button-group result groupView" name="num_modules" style="margin-top:10px;">
          <g:if test="${isFocusedArray}">
          <span class="button nobutton fullwidth active" name="all" title="All Modules">All Modules</span>
          </g:if>
          <g:else>
          <span class="button three" name="all" title="All Modules">All</span>
          <span class="button three" name="top" title="Most general modules created in the first six rounds of algorithm">Top</span>
          <span class="button three-long" name="annotated" title="Only those modules with a defined gene ontology">Annotated</span>
          </g:else>
        </div>
        <div></div>
        <div class="button-group result indView" name="ind_num_modules" style="margin-top:10px;margin-left:0;margin-right:0;display:none;">
          <g:if test="${isFocusedArray}">
          <span class="button firsthalf" name="all" title="All modules">All</span>
          <span class="button secondhalf" name="annotated" title="Only those modules with a defined gene ontology">Annotated</span>
          </g:if>
          <g:else>
          <span class="button firsthalf" name="all" title="All modules">All</span>
          <span class="button secondhalf" name="top" title="Most general modules created in the first six rounds of algorithm">Top</span>
          <span class="button firsthalf" name="annotated" title="Only those modules with a defined gene ontology">Annotated</span>
          <span class="button secondhalf nodraw" name="custom" data-keyboard="true" data-backdrop="true" data-controls-modal="pickModules-modal" >Custom</span>
        	<div id="pickModules-modal" class="modal hide" >
            <div class="modal-header">
              <a href="#" class="close">×</a>
              <h4>Pick Modules for Analysis</h4>
            </div>
            <div class="modal-body" style="overflow-y:scroll; max-height:400px;">
	 			<span>
	 				<table>
	 				<tbody id="ptable"></tbody>
	 				</table>
				</span>
				<br/>
				<br/>
				<input type="button" class="check" value="Uncheck All" />
            </div>
            <div class="modal-footer">
              <button class="btn primary small" onclick="selectAnnotations();">Submit</button>
              <button class="btn primary small" onclick="closeModal('pickModules-modal');">Cancel</button>
            </div>
           </div>
           <div class="button-group result indView" name="col_cluster_type" style="margin-top:10px;margin-left:0;margin-right:0;">
            <span class="button firsthalf" name="scatter" title="Scatter plot">Scatter</span>
           	<span class="button secondhalf" name="line" title="Line plot + points">Line</span>
         	</div>
         	<div id="zero-values" class="toggle-spotrows"><label for="show-zero-values"><g:checkBox name="show-zero-values" onclick="javascript:drawChart();" checked="${params.showZeroValues ?: false}"/> <span>Show zero value modules</span></label></div>
          </g:else>
        </div>
       <div class="button-group result groupView indView" name="display_type" style="margin-left:0;display:none;">
          <g:if test="${isFocusedArray}">
          <span class="button nobutton fullwidth active" name="spot" title="Significance or intensity noted by color hue">Spot Chart</span>
          </g:if>
          <g:else>
          <span class="button firsthalf" name="spot" title="Significance or intensity noted by color hue">Spot Chart</span>
          <span class="button secondhalf" name="piechart" title="Percent with fold change up and/or down noted by color">Pie Chart</span>
          </g:else>
        </div>
        <div class="groupView indView" id="filter-slider">
          <div class="filter-slider-caption">Show modules with at <br />least one sample > <span id="floor-value">${params.floor ?: 0}</span>%</div>
          <div class="max-slider">
            <div id="slider-filter" class="slider-range-max"></div>
          </div>
        </div>
        <div id="row-spots" class="toggle-spotrows"><label for="show-row-spots"><g:checkBox name="show-row-spots" onclick="javascript:drawChart();" checked="${params.showRowSpots ?: true}"/> <span>Show all samples for filtered module</span></label></div>
       <div class="indView" id="cluster-slider" style="display:none;">
          <div class="filter-slider-caption" style="display:none;">Trim dendrogram to <span id="cluster-display">${params.colLevel ?: 0}</span> of <span id="cluster-max"></span><span id="cluster-value" style="display:none;"></span> branches</div>
          <div class="max-slider" style="display:none;">
            <div id="slider-cluster" class="slider-range-max"></div>
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
            drawChart();
          }
        });
        $("#slider-cluster").slider({
          range: "max",
          step: 1,
          min: 0,
          max: maxDepth,
          value: ${params.colLevel ?: 0},
          slide: function(event, ui) {
          	var value = ui.value;
            $("span#cluster-display").html(maxDepth - value);
            $("span#cluster-value").html(value);
            dendroSliceShow(maxDepth, currentDepth, colLevel, value);
          },
          stop: function(event, ui) {
          	dendroSliceHide();
            drawChart();
          }
        });
      </g:javascript>
    </div>
         <div class="spacer"></div>
    <div class="groupKey well" style="padding:14px;display:none;">
      <h4 style="line-height:18px;"><span>Legend</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'legends');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="legends"></div>
    </div>
    
    </div><!--end mat_sidecol -->

  <div class="mat_maincol">

    <div id="imageInfo">
      <h2>Modules vs. Analyses</h2>
      <h3><span id="imageTitle"></span></h3>
      <p class="info" id="imageDescription">Modules vs. MetaCat Collection: ${title}</p>
    </div>
      <div id="moduleScatter"></div>

	<div id="mod_buttons" class="module-group container-fluid" style="width: 1000px;"></div><!-- use same width as d3 svg space -->
	
    <g:render template="/common/emailLink"/>

  </div><!--end mat_maincol-->

</div><!--end mat-container-->

<g:render template="/common/bugReporter" model="[tool:'MAT XProject']"/>
  
<script>
var currentScale = ${params.currentScale ?: 1.0};
var currentShift = 0;
var originalWidth = Math.max(minWidth, caseCount * 10); // 1000;
var originalHeight = 600;
var plotWidth = Math.max(minWidth, caseCount * 10);
var plotHeight = 600;

var closeModal = function(modalId) {
		$("#"+modalId).modal("hide");
};

var margin = {top: 20, right: 40, bottom: 30, left: 40},
    width = plotWidth - margin.left - margin.right,
    height = plotHeight - margin.top - margin.bottom;

var x = d3.scale.linear().range([0, width]);
var y = d3.scale.linear().range([height, 0]);
var v = d3.scale.linear().range([height, 0]);

var color = d3.scale.category10();
//for (i = 0; i < 10; i++) { console.log("color " + i + " " + color(i));}

var xAxis = d3.svg.axis().scale(x).orient("bottom").tickSubdivide(1);
var yAxis = d3.svg.axis().scale(y).orient("left");
var yZero = d3.svg.axis().scale(y).orient("left").tickValues([0]);
var vAxis; //= d3.svg.axis().scale(v).orient("right").ticks(5);
    
//var svg = d3.select("#moduleScatter").append("svg")
//    .attr("width", width + margin.left + margin.right)
//    .attr("height", height + margin.top + margin.bottom)
//  .append("g")
//    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
var svg;
var node;
var lines;
var values;

var mydiv = d3.select("body").append("div")   
.attr("class", "tooltip")               
.style("opacity", 0);

var epsilon = 0.3;
if (isLine) {
	var epsilon = 0;
}
var baseR = 3.7; // 3.5;

var clearChart = function() {
	d3.select("svg").remove();
    currentScale = 1.0;
    currentShift = 0;
    svg = d3.select("#moduleScatter").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    
  };

 // col_cluster_type now being used to choose scatter vs. line; is only used within JS/GSP
 // row_cluster_type used to select x-axis sorting, "sample", "variance", "signed", or "custom"
 // row_cluster_type is passed to AJAX/Controller+Action for ordering returned JSON data.
 // SPresnell@benaroyaresearch.org, 8-13-2013
   
  var updateChoosers = function() {
    var plotInfo = currentPlot.split("_");
    var isGroup = plotInfo[0] === "group";
    if (isGroup) {
        $("div[id='group_options'] div[name='analysis_type'] .button[name='"+plotInfo[1]+"']").addClass("active");
      $("div[name='num_modules'] .button[name='"+plotInfo[2]+"']").addClass("active");
      $("div[name='display_type'] .button[name='"+plotInfo[3]+"']").addClass("active");

      if (plotInfo[1] === "gsa") {
        $("div[name='display_type']").hide();
      }

      $("div[name='ind_num_modules'] .button[name='top']").addClass("active");
      $("div[name='col_cluster_type'] .button[name='none']").addClass("active");
      $("div[name='row_cluster_type'] .button[name='sample']").addClass("active");
    } else {
        $("div[name='ind_num_modules'] .button[name='"+plotInfo[2]+"']").addClass("active");
      $("div[name='display_type'] .button[name='"+plotInfo[3]+"']").addClass("active");
      $("div[name='col_cluster_type'] .button[name='"+plotInfo[4]+"']").addClass("active");
      $("div[name='row_cluster_type'] .button[name='"+plotInfo[5]+"']").addClass("active");

      //$("div[id='group_options'] div[name='analysis_type'] .button[name='${hasGSA ? 'gsa' : 'difference'}']").addClass("active");
      //$("div[name='ind_num_modules'] .button[name='top']").addClass("active");
      $("div[name='display_type']").hide();
    }
    $("div[name='group_ind'] .button[name='"+plotInfo[0]+"']").addClass("active").trigger("click");
  };

      $("div#group_ind .button").click(function() {
        if (!$(this).hasClass("disable"))
        {
          var hideView = this.id === "group" ? ".indView" : ".groupView";
          $(hideView).hide();
          $("."+this.id+"View").show();

		  $("#group_options").hide();
          if (this.id === "group") {
            if ($("div[id='group_options'] div[name='analysis_type'] .button.active").attr("name") === "gsa")
            {
              $("div#pvalue-slider").hide();
              $("div[name='display_type']").hide();
            }
            else
            {
              $("div#pvalue-slider").hide();
              $("div[name='display_type']").hide();
            }
            //$("div[name='overlay-options']").hide();
            $(".annotationKey").show();
            $(".groupKey").show();
            $(".plotKey").show();
            //$("div#row-spots").show();
            $("div#controls").show();
            $("span.individualText").hide();
            $("span.groupText").show();
          } else if (this.id === "ind") {
            $("div[name='overlay-options']").show();
            //$(".annotationKey").hide();
            //$(".groupKey").show();
            $("div#row-spots").show();
            $("div#zero-values").show();
            $("div#controls").show();
            //$("#cluster-slider").hide(); // initially
            //$("span.individualText").show();
            //$("span.groupText").hide();
            $("div[name='display_type']").hide();
          }
        }
         //console.log("ind modules: "+ $("div[name='ind_num_modules'] .button.active").attr("name"))
        
         
      });

     var getPlotName = function() {
        // build plot name from UI buttons
        var groupInd = $("div[name='group_ind'] .button.active").attr("name");
        if (groupInd === "group") {
          var numModules = $("div[name='num_modules'] .button.active").attr("name");
        } else if (groupInd === "individual") {
          var numModules = $("div[name='ind_num_modules'] .button.active").attr("name");
        }
        isCustom = numModules == "custom";
        var analysisType = $("div[id='group_options'] div[name='analysis_type'] .button.active").attr("name");
        var displayType = $("div[name='display_type'] .button.active").attr("name");
        var colClusterType = $("div[name='col_cluster_type'] .button.active").attr("name");
		isLine = colClusterType == "line";        
        var rowClusterType = $("div[name='row_cluster_type'] .button.active").attr("name");
        if (rowClusterType == "custom") {
        	rowClusterType = currentSort;
        }
        var plotname = groupInd.concat("_").concat(analysisType).concat("_").concat(numModules).concat("_").concat(displayType).concat("_").concat(colClusterType).concat("_").concat(rowClusterType);
        return plotname;
      };


  var drawChart = function() {
    clearChart();
    var plotName = getPlotName();
    //alert("plotname: " + plotName);
    var showRowSpots = $("input#show-row-spots").is(":checked");
    var showZeroValues = $("input#show-zero-values").is(":checked");
    var floor = $("span#floor-value").text();
    colLevel = $("span#cluster-value").text();
    var args = {
      id: metaCatId,
      plotName: plotName,
      showRowSpots: showRowSpots,
      floor: floor,
      colLevel: colLevel,
      customModules: customModules
    };
    var carry = jQuery.makeArray(customModules);
    
    d3.json("../scatterAnalysesPlot/${params.id}?plotName=" + plotName + "&customModules=" + carry + "&floor=" + floor + "&showRowSpots=" + showRowSpots + "&showZeroValues=" + showZeroValues, function(error, json) {
	// console.log("were back!");
    //if (error) { console.log(error) };
	data = json.results;
	noSamples = json.noSamples;
	clinical = json.clinical;
	sortName = json.sortName;
	sortFeatures = json.sortFeatures;
	
	// flatten the data for drawing the circles; and calculating domain extents.
	var fdata = d3.merge(data)
	var fclinical = d3.merge(clinical)
	
	//x.domain(d3.extent(data, function(d) { return d.sIdx; })).nice();
    x.domain([0, d3.max(fdata, function(d) { return d.sIdx; }) + 1]); // .nice() - not needed, funny gaps at end of graph.
    y.domain(d3.extent(fdata, function(d) { return d.mResp; })).nice();
    v.domain(d3.extent(fclinical, function(d) { return d.sVal; })).nice();
	
  	
  	// turn off jitter if we're drawing a line chart
  	if (isLine) {
		epsilon = 0;
  	} else {
	  	epsilon = 0.3;
  	}
  
  svg.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
    .append("text")
      .attr("class", "label")
      .attr("x", width - 5)
      .attr("y", - 5)
      .style("text-anchor", "end")
      //.text("sample index");
      .text(data.length + " modules for " + noSamples + " samples");

  svg.append("g")
      .attr("class", "y axis")
      .call(yAxis)
    .append("text")
      .attr("class", "label")
      //.attr("transform", "rotate(-90)")
      .attr("y", -10)
      .attr("x", 95)
      .attr("dy", ".71em")
      .style("text-anchor", "end")
      .text("Module Response (%)")

	if (clinical.length > 0) {
	vAxis = d3.svg.axis().scale(v).orient("right");
   	svg.append("g")             
        	.attr("class", "v axis")    
        	.attr("transform", "translate(" + width + " ,0)")   
        	.style("fill", "red")       
        	.call(vAxis)
       	.append("text")
    	.attr("class", "label")
    	.attr("transform", "rotate(-90)")
    	.attr("x", -5)
    	.attr("y", -5)
    	.style("text-anchor", "end")
    	.text(sortFeatures[sortName]);
    }

  // zero line the length of x-axis.
  	svg.append("g")
        .attr("class", "grid")
        .call(yZero.tickSize(-width, 0, 0).tickFormat(""));

	var responseline = d3.svg.line()
    	.x(function(d) { return x(d.sIdx); })
    	.y(function(d) { return y(d.mResp); })
    	.interpolate("monotone");

	var valueline = d3.svg.line()
    	.x(function(d) { return x(d.sIdx); })
    	.y(function(d) { return v(d.sVal); })
    	.interpolate("monotone");

  	node =   svg.selectAll(".dot")
      .data(fdata)
    .enter().append("circle")
//      .attr("class", "dot")
      .attr("class", function(d) { return "dot " + d.module;})
      .attr("r", baseR)
      .attr("cx", function(d) { jitter = Math.random() * (2*epsilon) - epsilon; return x(d.sIdx + jitter) ; })
      .attr("cy", function(d) { return y(d.mResp); })
      .style("fill", function(d) { return color(d.aIdx); })

//  	  .on("mouseover", function(){d3.select(this).style("fill", "aliceblue");})
//      .on("mouseout", function(){d3.select(this).style("fill", "white");});
      .on("mousedown", function (d) { //console.log("module: " + d.module);
    	    mydiv.transition().duration(200).style("opacity", .9);      
      		mydiv.html(d.mName).style("left", (d3.event.pageX) + "px").style("top", (d3.event.pageY - 28) + "px");
			d3.selectAll("." + d.module).transition().delay(0).duration(1000).attr("r", 10).style("opacity", 1);
		})
      .on("mouseup", function (d) { 
    		mydiv.transition().duration(200).style("opacity", 0);      
    		d3.selectAll("." + d.module).transition().delay(0).duration(1000).attr("r", baseR).style("opacity", 0.7);
       });
	node.append("title").text(function(d) { return d.mName + " @ " + d.mResp + " for " + d.sample});


    if (isLine) {
	     lines =  svg.selectAll(".line")
    		.data(data)
  		.enter().append("path")
  	      	.attr("class", function(d) { return "line " + d[0].module;})
		    .style("stroke", function(d) { var foo = d[0].mColor; return foo;})
	    	.attr("d", responseline);
	    	
	}
      
    if (clinical.length > 0) {
      	values = svg.selectAll(".value")
    		.data(clinical)
  		.enter().append("path")
  	      	.attr("class", "value")
    		.attr("d", valueline);
	    		
	}

//  var legend = svg.selectAll(".legend")
//      .data(color.domain())
//    .enter().append("g")
//      .attr("class", "legend")
//      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

//  legend.append("rect")
 //     .attr("x", width - 18)
//      .attr("width", 18)
//      .attr("height", 18)
//      .style("fill", color);

//  legend.append("text")
//      .attr("x", width - 24)
//      .attr("y", 9)
//      .attr("dy", ".35em")
//      .style("text-anchor", "end")
//      .text(function(d) { return d; });
   
   if (isCustom) {
   	$(".module-group .button").each(function() {
   		var btn = $(this);
   		var module = $(this).attr('name');
   		if (!btn.hasClass("active")) {
   			d3.selectAll("." + module).style("display", "none");
   		}
   	});
   }
   
          // show the legend if legend information is passed in.
       if (json.aLegend) {
           $("div.groupKey #legends").empty();
//	       if ($("div.groupKey #legends").html() == '') {
       	       $("div.groupKey").show();
        	   addLegend("analyses", "Analyses", json.aLegend);
        	   if (json.sLegend) {
        	   		addLegend("samplesets", "Sample Sets", json.sLegend)
        	   	}
//           }
    $('a.clickTip[rel]').each(function() {
        var ajaxUrl=$(this).attr('rel');
        $(this).qtip({
            content: {
                text: "Loading...",
                ajax: {
                    url: ajaxUrl,
                    type: 'GET',
                    data: {},
                    dataType: 'json',
                    success: function(result) {
                        this.set('content.text', result.content);
                    }
                },
                title: {
                    text: 'Study Information:'
                }
            },
            position: {
                my: "left center",
        		at: "right center",
                //at: 'bottom center',
                //my: 'top center',
                viewport: $(window),
                effect: false
            },
            show: {
                event: 'click',
                solo: true
            },
            hide: 'mouseout',
            style: {
                classes: 'ui-tooltip-tipsy qtip-wide',
            }
        })
    })

    // Make sure it doesn't follow the link when we click it
    .click(function(event) {
        event.preventDefault();
    });

       } else {
           $("div.groupKey #legends").empty();
       	   $("div.groupKey").hide();
       }
   
   	   // Full annotation key set it up only once.
	   if (json.moduleAnnotation && topicContainer.html() == '') {
	   		//console.log("drawing module modal");
			moduleMap = json.moduleAnnotation;
			topicContainer.empty();			
			var aKeys = $.map(json.moduleAnnotation, function(v,k){return k;});
			//console.log("keys: " + aKeys);
			var i,j, chunk = 5;
			for (i=0,j=aKeys.length; i<j; i+=chunk) {
	    		var tarray = aKeys.slice(i,i+chunk);
				//console.log("chunk: " + tarray);
				var row = $(document.createElement("tr"));				
				$.each( tarray, function( iteration, item )
				{
					row = row
			    	.append($(document.createElement("td"))
			    	.append(
			    		$(document.createElement('label')).attr({
			    			'for':	'topicFilter' + '-' + item, 'class': 'checkbox'
			    		})
			    		.text( item )
	
			    	.append(
			    		$(document.createElement("input")).attr({
			    			 id:	'topicFilter-' + item
			    			,name:	item
			    			,class: 'cb-module'
			    			,value:	item
			    			,type:	'checkbox'
			    			,checked:true
			    		})
//			    		.click( function( event ) {	var cbox = $(this)[0];	alert( cbox.value ); })
					)));
				});
				topicContainer.append(row);
				
			}
		}
   
	});
  }

       $(".button-group .button").live('click', function() {
        var btn = $(this);
        if (!btn.hasClass("disable"))
        {
          btn.siblings().removeClass("active");
          btn.addClass("active");

	        if ($("div[name='ind_num_modules'] .button.active").attr("name") === "custom")
	        {
	        	$("div#mod_buttons").show();
	        }
	        else
	        {
	            $("div#mod_buttons").hide();
	       	}

       		if ($("div[name='row_cluster_type'] .button.active").attr("name") === "custom") {
        		$("div[name='overlay-options']").show();
        	}
        	else
        	{
            	$("div[name='overlay-options']").hide();
            	// clear the radio buttons if a different sort option is selected.
            	$("input[class='scatterSort']").prop('checked',false);
        	}
        	
            // draw plot unless button has nodraw class (drawChart must be called after some secondary step)
            if (!btn.hasClass("nodraw")) {
              drawChart();
//            var plotName = getPlotName();
//            var floor = /^individual/.test(plotName) ? $("span#floor-value").text() : $("span#floor-pvalue").text();
//            var showRowSpots = $("input#show-row-spots").is(":checked");
//            var showControls = $("input#show-controls").is(":checked");
//            loadInteractiveImage(plotName,floor,showRowSpots,showControls);
			}
        }
      });
  
       $(".module-group .button").live('click', function() {
      	var btn=$(this);
      	var module=btn.attr('name'); 
      	if (!btn.hasClass("active"))
      	{
      		btn.addClass("active");
      		//console.log(module + " is now active");
      		d3.selectAll("." + module).style("display", "inline");
      	}
      	else
      	{
      		btn.removeClass("active");
      		//console.log(module + " is now inactive");
      		d3.selectAll("." + module).style("display", "none");
      	} 
				
      });
  
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

  var refreshSort = function(inputField) {
    var field = inputField.id;
    if ($(inputField).is(":checked"))
    {
      currentSort = $(inputField).attr("value");
      drawChart();
    } else {
       currentSort = "none";
    }
  };

  var addLegend = function(field,label,categories) {
      var link = 'exportLegend(\''+field+'_key\');';
      $("div.groupKey #legends").append('<div id="'+field+'_keylabel" class="key-label"><span class="key-label-title">'+label+'</span><span class="icon_download-small" style="margin-left:5px;" onclick="'+link+'"></span></div>');
      var legendHtml = '<div id="'+field+'_key" class="key">';
      $.each(categories, function(name, d) {
        legendHtml += '<div class="row"><span class="swatch" style="background-color:'+color(d.color)+';"></span><div class="span3">'+d.alink+'</div>'+d.ilink+'</div>';
      });
      legendHtml += '</div>';
      $("div.groupKey #legends").append(legendHtml);
  };
  var removeLegend = function(field) {
    $("div.groupKey #legends div[id='"+field+"_keylabel']").detach();
    $("div.groupKey #legends div[id='"+field+"_key']").detach();
  };

  var selectAnnotations = function() {
  	closeModal('pickModules-modal');

  	customModules = [];
  	$('input.cb-module:checkbox').each(function() {
  		if ($(this).is(':checked')) {
  			customModules = customModules.concat(moduleMap[$(this).attr('name')]);
  		}
  	});
 	//create list of buttons that will activate/deactiveate the plots on the page.
   	//console.log("selected:" + customModules);

  	buttonContainer.empty();
	var i,j, chunk = 5;
	for (i=0; i < customModules.length; i++) {
		var moduleClass = customModules[i].replace(/\./g, '_');
		var button = $(document.createElement("span")).attr({ 'class': 'button active', 'name': moduleClass}).text("Module " + customModules[i]);
		buttonContainer.append(button);
	}
  	
  	drawChart();
  };

  $(document).ready(function() {
  
  	topicContainer = $('tbody#ptable');
  	buttonContainer = $('div#mod_buttons');
  
  	// check/uncheck button in module selection modal dialog
     $('.check:button').toggle(function(){
        $('input.cb-module:checkbox').removeAttr('checked');
        $(this).val('Check All');
     }, function() {
        $('input.cb-module:checkbox').attr('checked','checked');
        $(this).val('Uncheck All');
     });
  
  
   
    updateChoosers();
    //drawChart();
  });

</script>