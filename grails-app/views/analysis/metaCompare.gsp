<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta name="layout" content="matmain">
  <title>MetaCat Compare: ${title}</title>
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
    var metaId = ${params.id};
    var maxDepth = 0;
    var currentDepth = 0;
    var colLevel = 0;
    var currentPlot = "${params.currentPlot ?: 'group_difference_annotated_spot_none_none'}";
    var moduleGeneration = "${metaCatInstance.generation ?: 2}";
    var customModules = [];
    var moduleMap = {};
	var topicContainer;
    
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
        _action: "metacatAnalysesPlot",
        _id: metaId,
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
<g:javascript src="bootstrap-modal.js"/>
<g:javascript src="bootstrap-dropdown.js"/>
<g:javascript src="modules/raphael.js"/>
<g:javascript src="modules/raphael.export.js"/>
<g:javascript src="g.raphael-min.js"/>
<g:javascript src="g.pie.js"/>

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
	          	<span class="button pill" id="group" name="group">Group</span>
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
            <span class="filterlabel">Cluster columns by:</span>
            <div class="button-group result" name="col_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf" name="none" title="Do not cluster columns">None</span>
              <span class="button secondhalf" name="samples" title="Cluster columns by analyses">Analyses</span>
            </div>

            <span class="filterlabel">Cluster rows by:</span>
            <div class="button-group result" name="row_cluster_type" style="margin-left:0;margin-right:0;">
              <span class="button firsthalf" name="none" title="Do not cluster rows">None</span>
              <span class="button secondhalf"  name="modules" title="Cluster rows by modules">Modules</span>
            </div>
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
          </g:else>
        </div>
        <div></div>
       <div class="button-group result groupView indView" name="display_type" style="margin-left:0;">
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
        <div id="row-spots" class="toggle-spotrows"><label for="show-row-spots"><g:checkBox name="show-row-spots" onclick="javascript:drawChart();" checked="${params.showRowSpots ?: true}"/> <span>Show all spots in row on filter</span></label></div>
       <div class="indView" id="cluster-slider">
          <div class="filter-slider-caption">Trim dendrogram to <span id="cluster-display">${params.colLevel ?: 0}</span> of <span id="cluster-max"></span><span id="cluster-value" style="display:none;"></span> branches</div>
          <div class="max-slider">
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
 <div class="spacer"></div>
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
      <h2>Modules vs. Analyses</h2>
      <h3><span id="imageTitle"></span></h3>
      <p class="info" id="imageDescription">Modules vs. MetaCat Collection: ${title}</p>
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
  var plotWidth = 800;
  var plotHeight = 600;

  var closeModal = function(modalId) {
  		$("#"+modalId).modal("hide");
  };

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
      var filename = "metacat".concat(metaId).concat("_module_vs_analyses");
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
      if (moduleGeneration === "2") {
          $("div[name='ind_num_modules'] .button[name='top']").addClass("active");
      } else {
          $("div[name='num_modules'] .button[name='top']").addClass("disable");
      	  $("div[name='num_modules'] .button[name='top']").prop('disable', true);
      
          $("div[name='ind_num_modules'] .button[name='top']").addClass("disable");
	      $("div[name='ind_num_modules'] .button[name='top']").prop('disable', true);
	      $("div[name='ind_num_modules'] .button[name='annotated']").addClass("active");
	  }
      $("div[name='col_cluster_type'] .button[name='none']").addClass("active");
      $("div[name='row_cluster_type'] .button[name='none']").addClass("active");
    } else {
      $("div[name='ind_num_modules'] .button[name='"+plotInfo[1]+"']").addClass("active");
      $("div[name='display_type'] .button[name='"+plotInfo[2]+"']").addClass("active");
      $("div[name='col_cluster_type'] .button[name='"+plotInfo[3]+"']").addClass("active");
      $("div[name='row_cluster_type'] .button[name='"+plotInfo[4]+"']").addClass("active");

      $("div[id='group_options'] div[name='analysis_type'] .button[name='${hasGSA ? 'gsa' : 'difference'}']").addClass("active");
      if (moduleGeneration === "2") {
      	$("div[name='num_modules'] .button[name='top']").addClass("active");
      } else {
        $("div[name='num_modules'] .button[name='top']").addClass("disable");
      	$("div[name='num_modules'] .button[name='top']").prop('disable', true);
        $("div[name='num_modules'] .button[name='annotated']").addClass("active");
      }
      $("div[name='display_type'] .button[name='spot']").addClass("active");
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
              $("div#pvalue-slider").show();
              $("div[name='display_type']").show();
            }
            $("div[name='overlay-options']").hide();
            $(".annotationKey").show();
            $(".groupKey").show();
            $(".plotKey").show();
            $("div#row-spots").show();
            $("div#controls").show();
            $("span.individualText").hide();
            $("span.groupText").show();
            $("div[name='col_cluster_type'] .button[name='samples']").html("Analyses");
            $("div[name='col_cluster_type'] .button[name='samples']").attr('title', "Cluster columns by analyses");
          } else if (this.id === "ind") {
            $("div[name='overlay-options']").show();
            $(".annotationKey").hide();
            $(".groupKey").show();
            $("div#row-spots").show();
            $("div#controls").show();
            $("#cluster-slider").hide(); // initially
            $("span.individualText").show();
            $("span.groupText").hide();
            $("div[name='col_cluster_type'] .button[name='samples']").html("Samples");
            $("div[name='col_cluster_type'] .button[name='samples']").attr('title', "Cluster columns by samples");
          }
        }
      });

     var getPlotName = function() {
        // build plot name from UI buttons
        var groupInd = $("div[name='group_ind'] .button.active").attr("name");
        if (groupInd === "group") {
          var numModules = $("div[name='num_modules'] .button.active").attr("name");
        } else if (groupInd === "individual") {
          var numModules = $("div[name='ind_num_modules'] .button.active").attr("name");
        }
        var analysisType = $("div[id='group_options'] div[name='analysis_type'] .button.active").attr("name");
        var displayType = $("div[name='display_type'] .button.active").attr("name");
        var colClusterType = $("div[name='col_cluster_type'] .button.active").attr("name");
        var rowClusterType = $("div[name='row_cluster_type'] .button.active").attr("name");
        var plotname = groupInd.concat("_").concat(analysisType).concat("_").concat(numModules).concat("_").concat(displayType).concat("_").concat(colClusterType).concat("_").concat(rowClusterType);
        return plotname;
      };


  var drawChart = function() {
    clearChart();
    var plotName = getPlotName();
    //alert("plotname: " + plotName);
    var showRowSpots = $("input#show-row-spots").is(":checked");
    var floor = $("span#floor-value").text();
    colLevel = $("span#cluster-value").text();
    var args = {
      id: metaId,
      plotName: plotName,
      showRowSpots: showRowSpots,
      floor: floor,
      colLevel: colLevel,
      customModules: customModules
    };
    $.getJSON(getBase()+"/analysis/metaCatPlot", $.param(args, true), function(json) {  // $.params() so I can pass an array in on customModules.
      if (json) {
        originalWidth = json.width + 20;
        originalHeight = json.height + 20;
      	startX = json.startX - 2;
      	startY = json.startY - 2;
      	endY = json.endY + 4;
		maxDepth = json.maxDepth;
		currentDepth = json.currentDepth;
		plotWidth = json.bg.width;
		plotHeight = json.bg.height;
		$('#slider-cluster').slider( "option", "max", maxDepth);
		$('span#cluster-max').html(maxDepth);
		if (colLevel == 0) {
			$('span#cluster-display').html(maxDepth);
		}
		if (maxDepth == 0) {
			$('#cluster-slider').hide();
		} else {
			$('#cluster-slider').show();
		}
        r.setSize(originalWidth, originalHeight);
        r.setViewBox(0, 0, originalWidth, originalHeight, true);
        
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
        setTimeout(function() { drawBg(json); drawModuleLabels(json); }, 0);
      }
    });
  };

      $(".button-group .button").click(function() {
        var btn = $(this);
        if (!btn.hasClass("disable"))
        {
          btn.siblings().removeClass("active");
          btn.addClass("active");

            // draw plot
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

  var drawBg = function(json) {
    // draw background
    bg = r.rect(0, 0, originalWidth, originalHeight).attr({ fill:"#FFF", stroke:"none", "stroke-width":0 });
    if (json.bg) {
      var pBg = json.bg;
      plotSets.push(r.rect(pBg.x, pBg.y, pBg.width, pBg.height).attr(pBg.attr));
    }
  };

  var drawAxes = function(json) {
    if (json.xAxis) {
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
//      updateChartSize();
    }
    setTimeout(function() { drawGroupKey(json); }, 0);
  };

  var drawGroupKey = function(json) {
    if (json.groupsKey) {
      groupSet = r.set();
      $.each(json.groupsKey, function(i,g) {
        //var grp = r.rect(g.x, g.y, g.width, g.height).attr(g.attr);
        var grp;
        var lbl;
        
        if (g.r) { // If there's a radius, render a piechart, else render a bar.
        	 grp = r.piechart(g.x, g.y, g.r, g.percents, { colors: g.colors, stroke: g.stroke });
        	 if (g.count > 1) { // render a count in the upper right
        		lbl = r.text(g.x + (g.r - 4), g.y - (g.r + 4), g.count).attr({ "font-size":12 });
        	} 
        } else {
        	grp = r.rect(g.x, g.y, g.width, g.height).attr(g.attr);
        }
        $.each(g.data, function(k,v) {
          grp.data(k,v);
        });
        grp.mouseover(groupMouseover);
        groupSet.push(grp);
        groupSet.push(lbl);
      });
      plotSets.push(groupSet);
    }
    setTimeout(function() { drawDendrograms(json); }, 0);
  };


  var drawDendrograms = function(json) {
    if (json.colDendrogram) {
      cuts = {};
      slice = null;
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

  var drawSpots = function(json,start,end) {
    if (json.spots) {
      var numSpots = json.spots.length;
      if (start < 0 || end < 0) {
        start = 0;
        end = json.sampleLbls ? json.sampleLbls.length : numSpots;
        circleSet = r.set();
      }
      while (start < end) {
        var c = json.spots[start];
        if (typeof c !== "undefined") {
          var circle = r.circle(c.cx,c.cy,c.r).attr(c.attr);
          $.each(c.data, function(d,value) {
           circle.data(d,value);
          });
          circleSet.push(circle);
        }
        start++;
      }
      if (json.sampleLbls && start < numSpots) {
        start = end;
        var newEnd = end + json.sampleLbls.length;
        end = Math.min(numSpots, newEnd);
        setTimeout(function() { drawSpots(json,start,end); }, 10);
      } else {
        circleSet.mouseover(tooltipShow).mouseout(tooltipHide);
        plotSets.push(spots);
        //setTimeout(function() { finish(json); }, 0);
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
      var text = "<div>" + it.find(".span3").html().replace('<br>', '; ') + "</div>";
      //console.log("text1: " + text);
      text = $(text).text();
      //console.log("text2: " + text);
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
      var filename = "metacat"+metaId+"_"+divId;
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
    if (elt.data("moduleWiki")) {
      tooltipText += elt.data("moduleWiki") + "<br/>";
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
  
  var groupMouseover = function() {
    var text = this.data("group").concat("<br/>ID: ");
    text += this.data("sampleLabel") ? this.data("sampleLabel") : this.data("sampleId");
    tooltip($(this.node),text);
  };

  //----- DENDROGRAM -----//
  var startY = 0, endY = 0;
  var startX = 0, endX = 0;
  var cuts = {};
  var slice = null;
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
      var width = originalWidth;
      var yOffset = activeOverlays * overlayHeight;
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

  var dendroSliceShow = function(max, currMax, colLevel, level) {
  	colLevel = typeof colLevel !== 'undefined' ? colLevel : 0;
	//console.log("max: " + max + " curr: " + currMax + " curlevel: " + colLevel + "; new level: " + level);
	if (slice && slice != null) {
		slice.remove();
		slice = null;
    }
   	if (level > colLevel) { 
		var lOffset = max - currMax - colLevel
		var width = plotWidth;
      	var yOffset = 20 + 5; // 20 is raw offset, 5 is a half level.
		
		level += lOffset;

      	r.setStart();
      	r.rect(startX, yOffset + (Math.max((max - level - 1), -1) * 10), width, 2).attr({ fill:"#f00", stroke:"none", "stroke-width":0 });
      	slice = r.setFinish();
    }
	
  };
  
  var dendroSliceHide = function() {
  	if (slice && slice != null) {
  		slice.remove();
	    delete slice;
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
  
    //----- Overlays -----//
  var skipLegendDraw = false;
  var colorSchemes = [5,4,3,2,1,0];
  var overlayHeight = 8;
  var activeOverlays = 0
  var addLegend = function(field,label,categories) {
      var link = 'exportLegend(\''+field+'_key\');';
      $("div.groupKey #legends").append('<div id="'+field+'_keylabel" class="key-label"><span class="key-label-title">'+label+'</span><span class="icon_download-small" style="margin-left:5px;" onclick="'+link+'"></span></div>');
      var legendHtml = '<div id="'+field+'_key" class="key">';
      $.each(categories, function(name, d) {
        legendHtml += '<div class="row"><span class="swatch" style="background-color:'+d.color+';"></span><div class="span3">'+d.alink+'</div>'+d.ilink+'</div>';
      });
      legendHtml += '</div>';
      $("div.groupKey #legends").append(legendHtml);
  };
  var removeLegend = function(field) {
    $("div.groupKey #legends div[id='"+field+"_keylabel']").detach();
    $("div.groupKey #legends div[id='"+field+"_key']").detach();
  };

  var selectAnnotations = function() {
  	customModules = [];
  	$('input.cb-module:checkbox').each(function() {
  		if ($(this).is(':checked')) {
  			customModules = customModules.concat(moduleMap[$(this).attr('name')]);
  		}
  	});
 
   	//console.log("selected:" + customModules);
  	closeModal('pickModules-modal');
  	drawChart();
  };
  
  $(document).ready(function() {
  
   	topicContainer = $('tbody#ptable');
  
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
</g:javascript>

</body>
</html>