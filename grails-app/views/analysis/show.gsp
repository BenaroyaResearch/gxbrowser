<!DOCTYPE html>
<%@ page import="grails.converters.JSON; org.sagres.sampleSet.SampleSet; org.sagres.mat.Analysis"%>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta name="layout" content="matmain">
  <title>MAT Analysis: ${title}</title>
  
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
  <style type="text/css">
    .icon.settings:before {
      margin: -0.15em 0.75em 0 -0.25em;
    }
    .overlay-dropdown {
      display: none;
      position:absolute;
      background-color:#fdfdfd;
      border: 1px solid #ccc;
      list-style-type:none;
      z-index: 100;
      margin:0;
      margin-top: -2px;
      width:172px;
      padding: 8px 10px 0;

      -moz-border-radius-bottomright:5;
      -webkit-border-bottom-right-radius:5;
      border-bottom-right-radius:5;
      -moz-border-radius-bottomleft:5;
      -webkit-border-bottom-left-radius:5;
      border-bottom-left-radius:5;
    }
    .overlay-dropdown li {
      padding: 2px 0;
    }
    .groupKey #legends {
      max-height: 200px;
      overflow-x: hidden;
      overflow-y: auto;
    }

  </style>
  <g:javascript>
    var groups = ${groups as JSON};
    var controls = ${controls as JSON};
    var analysisId = ${analysisInstance.id};
    var sampleSetId = ${hasValidSampleSet ? analysisInstance.sampleSetId : -1};
    var csvDownloadLink = "${createLink(controller:"analysis", action:"plotFile", id:analysisInstance.id)}";
    var plotKeyLabel = null;

    var currentPlotKey = "${params.currentPlotKey ?: (isFocusedArray ? 'foldchange' : 'difference')}";
    var currentPlot = "${params.currentPlot ?: 'group_difference_top_spot'}";
    function filter(min, maxFoldChange) {
      var showRowSpots = $("input#show-row-spots").is(":checked");
      var showControls = $("input#show-controls").is(":checked");
      var downloadLink = "${createLink(controller: 'analysis', action: 'plotFile', id: analysisInstance.id)}?plotName=".concat(currentPlot)
        .concat("&floor=").concat(min).concat("&maxFoldChange=").concat(maxFoldChange).concat("&showRowSpots=").concat(showRowSpots).concat("&showControls=").concat(showControls);
      csvDownloadLink = downloadLink;
      $("a[name='download_file']").attr("href",downloadLink);
      drawChart(analysisId,currentPlot,min,maxFoldChange,showRowSpots,showControls);
    };
    function filterPValue(pvalue) {
      var downloadLink = "${createLink(controller: 'analysis', action: 'plotFile', id: analysisInstance.id)}?plotName=".concat(currentPlot)
        .concat("&floor=").concat(pvalue);
      csvDownloadLink = downloadLink;
      $("a[name='download_file']").attr("href",downloadLink);

      if (plotKeyLabel !== null) {
        plotKeyLabel.attr("text", "% probe sets with p < " + pvalue);
      }

      drawChart(analysisId,currentPlot,pvalue,null,false,true);
    };

    function loadInteractiveImage(name,floor,maxFoldChange,showRowSpots,showControls) {
      $.getJSON(getBase()+"/analysis/moduleInfo", { id:analysisId, plotName:name }, function(json) {
        $("#imageTitle").html(json.title);
        $("#imageDescription").html(json.description);
      });

      var downloadLink = "${createLink(controller: 'analysis', action: 'plotFile', id: analysisInstance.id)}?plotName=".concat(name)
        .concat("&floor=").concat(floor).concat("&maxFoldChange").concat(maxFoldChange).concat("&showRowSpots=").concat(showRowSpots).concat("&showControls=").concat(showControls);
      csvDownloadLink = downloadLink;
      $("a[name='download_file']").attr("href",downloadLink);

      currentPlot = name;
      setTimeout(function() { drawChart(analysisId,name,floor,maxFoldChange,showRowSpots,showControls); }, 0);
    };

    function updateGXBLink() {
      if (sampleSetId != -1) {
        var gxbLink = $("#GXBLink").html("GXB");
        gxbLink.attr("href", "${createLink(controller: 'geneBrowser', action: 'show', id:analysisInstance.sampleSetId)}");
      }
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
        var filename = $("#imageTitle").html().concat(" Plot Key");
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
        var keyType = $("div#"+divId).prev().text();
        var filename = $("#imageTitle").html().concat(" "+keyType);
        window.location.href = location.concat("?filename=").concat(filename);
      });
    };

    var enableGSA = function(elt) {
      var gsaOption = $("span[name='gsa']");
      if (gsaOption.is(":visible")) {
        $(elt).html("Enable GSA Plot");
        var diffElt = $("span[name='difference']");
        diffElt.removeClass("firsthalf").addClass("nobutton").addClass("fullwidth");
        if (!diffElt.hasClass("active")) {
          diffElt.trigger("click");
        }
        gsaOption.hide();
      } else {
        $(elt).html("Disable GSA Plot");
        $("span[name='difference']").removeClass("nobutton").removeClass("fullwidth").addClass("firsthalf");
        gsaOption.show();
      }
    };

    var generateArgs = function() {
      var initOverlays = activeOverlays > 0;
      var args =
      {
        _controller: "analysis",
        _action: "show",
        _id: analysisId,
        currentPlot: currentPlot,
        currentPlotKey: currentPlotKey,
        filter: $("span#floor-value").text(),
        showRowSpots: $("input#show-row-spots").is(":checked"),
        showControls: $("input#show-controls").is(":checked"),
        sampleField: $("#sampleLabelOption").val(),
        annotationKey: $("#annotationKey span.active").attr("id"),
        currentScale: currentScale,
        initOverlays: initOverlays
      };
      if ($("span#floor-pvalue").length > 0) {
        args.pvalue = $("span#floor-pvalue").text();
      }
      if ($("span#fcfilter-value").length > 0) {
        args.maxFoldChange = $("span#fcfilter-value").text();
      }
      $.each(overlayOrder, function(k,order) {
        var color = overlayColor[k];
        args["categorical_overlay:".concat(k)] = order + ":" + color;
      });
      return args;
    };
    var generateLink = function(showEmail,isClient) {
      var args = generateArgs();
      $.getJSON(getBase()+"/miniURL/create", args, function(json) {
        if (showEmail || isClient) {
          var subject = "Module Analysis Tool Link for '${title}'";
          var text = "Hello,\r\n\r\n I thought you'd be interested in looking at the analysis '${title}' in the Module Analysis Tool. \r\n\r\n" +
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
       %{--<div class="sidecol-hideshow"></div>--}%
    %{-- VIEW PLOT UI --}%
    <div id="plot-type-well" class="well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Plot Type</span>
        <div class="ui-icons">
          <span class="ui-icon-minimize tiny" onclick="minMax(this,'plot-type-options');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
      <div id="plot-type-options">
      <div id="plot_type_chooser" class="plot-group-ind-chooser">
        <div class="button-group result" id="group_ind" name="group_ind" style="margin-top:10px;">
        <g:if test="${grailsApplication.config.mat.plot.types.contains('individual')}">
          	<span class="button pill" id="group" name="group">Group</span>
          	<span class="button pill" id="ind" name="individual">Individual</span>
          </g:if>
          <g:else>
          	<span class="button" id="group" name="group">Group</span>
          </g:else>
        </div>
      </div>

      <div id="group_options" class="plotoptions groupView">
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

      <div id="ind_options" class="plotoptions indView" style="display:none;">
        %{--<div class="button-group result" name="clustering">--}%
          %{--<span class="button active firsthalf" name="unclustered" title="Unclustered">Unclustered</span>--}%
          %{--<span class="button secondhalf" name="clustered" title="Clustered">Clustered</span>--}%
        %{--</div>--}%

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
        <div class="button-group result groupView" name="num_modules" style="margin-top:10px;">
          <g:if test="${isFocusedArray}">
          <span class="button nobutton fullwidth active" name="all" title="All Modules">All Modules</span>
          </g:if>
          <g:else>
          <span class="button firsthalf" name="top" title="Most general modules created in the first six rounds of algorithm">Top Modules</span>
          <span class="button secondhalf" name="all" title="All Modules">All Modules</span>
          </g:else>
        </div>
        <div></div>
        <div class="button-group result indView" name="ind_num_modules" style="margin-top:10px;margin-left:0;margin-right:0;display:none;">
          <g:if test="${isFocusedArray}">
          <span class="button firsthalf" name="all" title="All modules">All</span>
          <span class="button secondhalf" name="annotated" title="Only those modules with a defined gene ontology">Annotated</span>
          </g:if>
          <g:else>
          <span class="button three" name="all" title="All modules">All</span>
          <span class="button three" name="top" title="Most general modules created in the first six rounds of algorithm">Top</span>
          <span class="button three-long" name="annotated" title="Only those modules with a defined gene ontology">Annotated</span>
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
        <g:if test="${hasProbeLvlStats}">
        <div class="groupView" id="pvalue-slider">
          <div class="filter-slider-caption">Count probes with p-value &lt <span id="floor-pvalue">${params.pvalue ?: analysisInstance.fdr}</span></div>
          <div class="max-slider">
            <div id="slider-pvalue" class="slider-range-max"></div>
          </div>
          <div class="center reset-slider">
            <button class="button reset-value" onclick="resetPValue();">Reset p-value</button>
          </div>
        </div>
        </g:if>
        <g:if test="${isFocusedArray}">
          <div id="fc-threshold-slider">
            <div class="filter-slider-caption">Set FC Treshold at <span id="fcfilter-value">${params.maxFoldChange ?: 2}</span> FC</div>
            <div class="max-slider">
              <div id="slider-fc" class="slider-range-max"></div>
            </div>
          </div>
        </g:if>
        <div class="indView" id="filter-slider" style="display:none;">
          <div class="filter-slider-caption">Show modules with at <br />least one sample > <span id="floor-value">${params.filter ?: 0}</span>${isFocusedArray ? " FC" : "%"}</div>
          <div class="max-slider">
            <div id="slider-filter" class="slider-range-max"></div>
          </div>
        </div>
        <div id="controls" class="toggle-spotrows indView" style="display:none;"><label for="show-controls"><g:checkBox name="show-controls" onclick="javascript:updateChart();" checked="${params.showControls ?: true}"/> <span>Show controls</span></label></div>
        <div id="row-spots" class="toggle-spotrows indView" style="display:none;"><label for="show-row-spots"><g:checkBox name="show-row-spots" onclick="javascript:updateSpots();" checked="${params.showRowSpots ?: true}"/> <span>Show all spots in row on filter</span></label></div>
        <g:javascript>
          var updateSpots = function() {
            var floor = $("span#floor-value").text();
            var maxFoldChange = $("span#fcfilter-value").text();
            if (floor != 0)
            {
              filter(floor, maxFoldChange);
            }
          };
          $("#slider-filter").slider({
            range: "max",
            min: 0,
            max: ${isFocusedArray ? 10 : 100},
            step: ${isFocusedArray ? 0.1 : 1},
            value: ${params.filter ?: 0},
            slide: function(event, ui) {
              $("span#floor-value").html(ui.value);
            },
            stop: function(event, ui) {
              var maxFoldChange = $("span#fcfilter-value").text();
              filter(ui.value, maxFoldChange);
            }
          });
          $("#slider-pvalue").slider({
            range: "max",
            min: 0,
            max: 1,
            step: 0.01,
            value: ${params.pvalue ?: analysisInstance.fdr},
            slide: function(event, ui) {
              $("span#floor-pvalue").html(ui.value);
              $("span[name='pvalue-text']").html(Math.round(ui.value * 100));
              $("span[name='pvalue-text-orig']").html(ui.value);
            },
            stop: function(event, ui) {
              filterPValue(ui.value);
            }
          });
          $("#slider-fc").slider({
            range: "max",
            min: 1,
            max: 10,
            step: 0.5,
            value: ${params.maxFoldChange ?: 2},
            slide: function(event, ui) {
              $("span#fcfilter-value").html(ui.value);
            },
            stop: function(event, ui) {
              var floor = $("span#floor-value").text();
              filter(floor, ui.value);
              fcPlotKey();
            }
          });
          var resetPValue = function() {
            var origPval = ${params.pvalue ?: analysisInstance.fdr};
            if ($("#slider-pvalue").slider("option", "value") !== origPval) {
              $("#slider-pvalue").slider("option", "value", origPval);
              $("span#floor-pvalue").html(origPval);
              $("span[name='pvalue-text']").html(origPval * 100);
              $("span[name='pvalue-text-orig']").html(origPval);
              filterPValue(origPval);
            }
          };
        </g:javascript>

        <g:if test="${sampleSetFields}">
        <div class="indView label-options" style="display:none;">
          <span class="filter-slider-caption">Sample Label:</span>
          <g:select name="sampleLabelOption" from="${sampleSetFields}" optionKey="key" optionValue="displayName"
            noSelection="['sampleBarcode':'Default']" value="sampleBarcode" onchange="javascript:switchSampleLabels();"/>
          <g:javascript>
            var switchSampleLabels = function() {
              var field = $("#sampleLabelOption").val();
              $.getJSON(getBase()+"/analysis/sampleLabels", { sampleSetId:sampleSetId, field:field }, function(json) {
                updateSampleLabels(json);
              });
            };
          </g:javascript>
        </div>
        </g:if>

          <div name="overlay-options" class="clinical-data" style="display:none;">
            <span class="button clinical-data-options" title="Select clinical data to show" style="text-align:left;">Clinical Data <span class="button-arrow"></span></span>
            <ul class="clinical-data-optionlist">
            </ul>
          </div>
          <g:javascript>
            $("div[name='overlay-options'] span.button").click(function() {
              $("ul.clinical-data-optionlist").toggle();
            });
            var updateOverlays = function() {
              var args = { sampleSetId: sampleSetId };
              $.getJSON(getBase()+"/analysis/overlayOptions", args, function(json) {
                if (json) {
                  var html = "";
                  $.each(json, function(i, cat) {
                    var key = cat.collection + "_" + cat.key;
                    html += '<li><label>';
                    html += '<input type="checkbox" id="' + key + '" name="' + key + '" onclick="javascript:refreshOverlay(this);"/>';
                    html += ' <span>' + cat.displayName + '</span>';
                    html += '</label></li>';
                  });
                  $("ul.clinical-data-optionlist").html(html);
                } else {
                  $("li#correlation-menu").detach();
                  $("div[name='overlay-options']").detach();
                }
              });
            };
            setTimeout(function() { updateOverlays(); }, 0);
          </g:javascript>
      </div>
    </div>

    %{-- SHOW ANNOTATION KEY UI - ONLY FOR GROUP COMPARISON --}%
    <div id="annotations-well" class="annotationKey well" style="padding:14px;">
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
     <div class="spacer"></div>
    <div class="groupKey well" style="padding:14px;display:none;">
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
    <div id="parameters-well" class="plotkey well" style="padding:14px;">
      <h4 style="line-height:18px;"><span>Parameters</span>
        <div class="ui-icons">

          <span class="ui-icon-minimize tiny" onclick="minMax(this,'parameters');"><span></span></span>
        </div><!--end ui icons-->
      </h4>
        <div id="parameters">
          <p style="font-size:12px;">
            <g:if test="${isFocusedArray}">
            For each circle the red intensity indicates the degree to which the module is up-regulated in cases (${cases.join(",")})
            compared to controls (${controls.join(",")}) while the blue intensity indicates down-regulation in cases versus controls.
            The up- and down-regulation is represented by <b>fold change</b> values in the image.
            </g:if>
            <g:else>
            For each circle the red intensity indicates the degree to which the module is up-regulated in cases (${cases.join(",")})
            compared to controls (${controls.join(",")}) while the blue intensity indicates down-regulation in cases versus controls.
            </g:else>
          </p>
          <p style="font-size:12px;">
            <g:if test="${isFocusedArray}">
            The analysis shown here was performed on the <g:if test="${sampleSetName}">on the <em>'${sampleSetName}'</em> sample set</g:if>
            using <span style="font-weight:bold;">${groups.keySet().join(" and ")}</span> as the groups. To compare these groups,
            <span class="groupText">the overall module fold change was calculated using the geometric mean of the cases</span>
            <span class="individualText" style="display:none;">the fold change of each individual value was calculated</span> compared to
            the geometric mean of the controls.
            </g:if>
            <g:else>
              The analysis shown here was performed <g:if test="${sampleSetName}">on the <em>'${sampleSetName}'</em> sample set</g:if>
              using <span style="font-weight:bold;">${groups.keySet().join(" and ")}</span> as the groups. To compare these groups,
              <span class="individualText" style="display:none;">
                each individual's value was compared to the average of the controls with filters based on fold change and difference.
                The cutoff for fold change was <span style="font-weight:bold;">${analysisInstance.deltaType == "fold" ? analysisInstance.foldCut : foldCut.zscoreCut}</span>
                for up-regulation and <span style="font-weight:bold;">${analysisInstance.deltaType == "fold" ? 1/analysisInstance.foldCut : 1/foldCut.zscoreCut}</span> for down-regulation,
                and the cutoff for difference was ${analysisInstance.deltaCut} for up-regulation and -${analysisInstance.deltaCut} for down-regulation.
              </span>
              <span class="groupText">
              To compare these groups, a
              two group linear model comparison was run using limma.
              <g:if test="${analysisInstance.multipleTestingCorrection == 'TRUE'}">
              The false discovery rate was set to
              <span style="font-weight:bold;"><span name="pvalue-text">${Math.round((params.double('pvalue') ?: analysisInstance.fdr) * 100)}</span> percent</span>, and
              multiple testing correction was <span style="font-weight:bold;">On</span>.
              </g:if>
              <g:else>
              The p-value cutoff was set to
              <span style="font-weight:bold;"><span name="pvalue-text">${Math.round((params.double('pvalue') ?: analysisInstance.fdr) * 100)}</span> percent</span>, and
              no multiple testing correction was performed.
              </g:else>
              </span>

           %{--The analysis shown here was performed--}%
          %{--<g:if test="${sampleSetName}">on the <em>'${sampleSetName}'</em> sample set</g:if> using--}%
          %{--<span style="font-weight:bold;">${groups.keySet().join(" and ")}</span> as the groups.--}%
        %{--To compare these groups, the <span style="font-weight:bold;">${analysisInstance.deltaType == "fold" ? "fold change" : "zcore" }</span> method was used with--}%
        %{--a cutoff of <span style="font-weight:bold;">${analysisInstance.deltaType == "fold" ? analysisInstance.foldCut : foldCut.zscoreCut}</span>.--}%
        %{--The ${analysisInstance.multipleTestingCorrection == "TRUE" ? "false discovery rate" : "p-value threshold" } was set to--}%
        %{--<span style="font-weight:bold;"><span name="pvalue-text">${Math.round((params.pvalue ?: analysisInstance.fdr) * 100)}</span> percent</span>,--}%
        %{--and multiple testing correction was <span style="font-weight:bold;">${analysisInstance.multipleTestingCorrection == "TRUE" ? "On" : "Off" }</span>.--}%
            </g:else>
        </p>
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

      var gsaPlotKey = function() {
        plotKeyR.clear();
        plotKeyR.setSize(200,160);
        plotKeyR.rect(0,0,195,160).attr({ fill:"#fff", stroke:"none" });
        plotKeyR.text(36,15,"OVER-XP").attr({ "font-size":12, "fill":"#333333" });
        plotKeyR.text(156,15,"UNDER-XP").attr({ "font-size":12, "fill":"#333333" });

        plotKeyR.text(96,46,"p < 0.001").attr({ "font-size":12, "fill":"#333333" });
        plotKeyR.text(96,76,"p < 0.01").attr({ "font-size":12, "fill":"#333333" });
        plotKeyR.text(96,106,"p < 0.03").attr({ "font-size":12, "fill":"#333333" });
        plotKeyR.text(96,136,"p < 0.05").attr({ "font-size":12, "fill":"#333333" });

        plotKeyR.setStart();
        plotKeyR.circle(36,45,12).attr({ fill:"#ff3b2d", stroke:"none" }).data("max",0.001);
        plotKeyR.circle(156,45,12).attr({ fill:"#0049f8", stroke:"none" }).data("max",-0.001);

        plotKeyR.circle(36,75,12).attr({ fill:"#ff6964", stroke:"none" }).data("max",0.01);
        plotKeyR.circle(156,75,12).attr({ fill:"#5970f9", stroke:"none" }).data("max",-0.01);

        plotKeyR.circle(36,105,12).attr({ fill:"#ff9b98", stroke:"none" }).data("max",0.03);
        plotKeyR.circle(156,105,12).attr({ fill:"#939efa", stroke:"none" }).data("max",-0.03);

        plotKeyR.circle(36,135,12).attr({ fill:"#ffcdcc", stroke:"none" }).data("max",0.05);
        plotKeyR.circle(156,135,12).attr({ fill:"#cacefc", stroke:"none" }).data("max",-0.05);
        circleSet = plotKeyR.setFinish();

        currentPlotKey = "gsa";
      };

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

      var fcPlotKey = function() {
        plotKeyR.clear();
        plotKeyR.setSize(200,320);
        plotKeyR.rect(0,0,195,320).attr({ fill:"#fff", stroke:"none" });
        plotKeyR.text(46,20,"OVER-XP").attr({ "font-size":12, "fill":"#333333" });
        plotKeyR.text(146,20,"UNDER-XP").attr({ "font-size":12, "fill":"#333333" });

        var maxFc = parseFloat($("span#fcfilter-value").text());
        var interval = maxFc < 2.5 ? (220 /maxFc / 2) : 220 / maxFc;
        var fcInterval = maxFc < 2.5 ? 0.5 : 1;

        var yStart = 45;
        while (yStart <= 265.0) {
          plotKeyR.text(96, yStart, maxFc).attr({ "font-size":12, "fill":"#333333" });
          maxFc = maxFc - fcInterval;
          yStart = yStart + interval;
        }

        plotKeyR.rect(36,45,20,220).attr({ fill:"90-#fff-#ff0000", stroke:"#333", "stroke-width":2 });
        plotKeyR.rect(136,45,20,220).attr({ fill:"90-#fff-#0000ff", stroke:"#333", "stroke-width":2 });

        plotKeyLabel = plotKeyR.text(96,295,"Fold Change").attr({ "font-size":12, "fill":"#333333" });

        currentPlotKey = "foldchange";
      };

      $("div#group_ind .button").click(function() {
        if (!$(this).hasClass("disable"))
        {
          var hideView = this.id === "group" ? ".indView" : ".groupView";
          $(hideView).hide();
          $("."+this.id+"View").show();

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
            $(".groupKey").hide();
            $(".plotKey").show();
            $("div#row-spots").hide();
            $("div#controls").hide();
            $("span.individualText").hide();
            $("span.groupText").show();
          } else if (this.id === "ind") {
            $("div[name='overlay-options']").show();
            $(".annotationKey").hide();
            $(".groupKey").show();
            $("div#row-spots").show();
            $("div#controls").show();
            $("span.individualText").show();
            $("span.groupText").hide();
          }
        }
      });

      $("div[name='analysis_type'] .button").click(function() {
        if (!$(this).hasClass("disable"))
        {
          if ($(this).attr("name") === "difference") {
            $("div#pvalue-slider").show();
            $("div[name='display_type']").show();
          } else {
            $("div#pvalue-slider").hide();
            $("div[name='display_type']").hide();
          }
        }
      });

      var getPlotName = function() {
        // build plot name from UI buttons
        var groupInd = $("div[name='group_ind'] .button.active").attr("name");
        if (groupInd === "group") {
          var analysisType = $("div[id='group_options'] div[name='analysis_type'] .button.active").attr("name");
          var numModules = $("div[name='num_modules'] .button.active").attr("name");
          var displayType = $("div[name='display_type'] .button.active").attr("name");
          var plotname = groupInd.concat("_").concat(analysisType).concat("_").concat(numModules).concat("_").concat(displayType);
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
          $("div[id='group_options'] div[name='analysis_type'] .button[name='"+plotInfo[1]+"']").addClass("active");
          $("div[name='num_modules'] .button[name='"+plotInfo[2]+"']").addClass("active");
          $("div[name='display_type'] .button[name='"+plotInfo[3]+"']").addClass("active");

          if (plotInfo[1] === "gsa") {
            $("div[name='display_type']").hide();
          }

          $("div[name='ind_num_modules'] .button[name='all']").addClass("active");
          $("div[name='col_cluster_type'] .button[name='none']").addClass("active");
          $("div[name='row_cluster_type'] .button[name='none']").addClass("active");
        } else {
          $("div[name='ind_num_modules'] .button[name='"+plotInfo[1]+"']").addClass("active");
          $("div[name='display_type'] .button[name='"+plotInfo[2]+"']").addClass("active");
          $("div[name='col_cluster_type'] .button[name='"+plotInfo[3]+"']").addClass("active");
          $("div[name='row_cluster_type'] .button[name='"+plotInfo[4]+"']").addClass("active");

          $("div[id='group_options'] div[name='analysis_type'] .button[name='${hasGSA ? 'gsa' : 'difference'}']").addClass("active");
          $("div[name='num_modules'] .button[name='top']").addClass("active");
          $("div[name='display_type'] .button[name='spot']").addClass("active");
        }
        $("div[name='group_ind'] .button[name='"+plotInfo[0]+"']").addClass("active").trigger("click");
      };

      $(".button-group .button").click(function() {
        var btn = $(this);
        if (!btn.hasClass("disable"))
        {
          btn.siblings().removeClass("active");
          btn.addClass("active");

          var parentName = btn.parent().attr("name");
          if (parentName !== "annotation_key" && parentName !== "abbrevShowHide") {
            var groupInd = $("div[name='group_ind'] .button.active").attr("name");
            if (groupInd === "group")
            {
              var analysisType = $("div[name='analysis_type'] .button.active").attr("name");
              if (currentPlotKey !== "gsa" && analysisType === "gsa")
              {
                gsaPlotKey();
              }
              else if (currentPlotKey !== "difference" && analysisType === "difference")
              {
                modPlotKey();
              }
              else if (currentPlotKey !== "foldchange" && analysisType === "focusedarray")
              {
                fcPlotKey();
              }
              if (analysisType !== 'focusedarray' && plotKeyLabel !== null) {
                var pval = $("span#floor-pvalue").text();
                plotKeyLabel.attr("text", "% probe sets with p < " + pval);
              }
            }
            else
            {
              if (currentPlotKey !== "difference" && currentPlotKey !== "foldchange")
              {
                modPlotKey();
              }

              if (currentPlotKey !== "foldchange" && plotKeyLabel !== null) {
                plotKeyLabel.attr("text", "% probe sets with p < 0.05");
              }
            }

            // draw plot
            updateChart();
//            var plotName = getPlotName();
//            var floor = /^individual/.test(plotName) ? $("span#floor-value").text() : $("span#floor-pvalue").text();
//            var showRowSpots = $("input#show-row-spots").is(":checked");
//            var showControls = $("input#show-controls").is(":checked");
//            loadInteractiveImage(plotName,floor,showRowSpots,showControls);
          }
        }
      });

      var updateChart = function()
      {
        var plotName = getPlotName();
        var floor = /^individual/.test(plotName) ? $("span#floor-value").text() : $("span#floor-pvalue").text();
        var showRowSpots = $("input#show-row-spots").is(":checked");
        var showControls = $("input#show-controls").is(":checked");
        var maxFoldChange = 2;
        if ($("span#fcfilter-value").length > 0) {
          maxFoldChange = $("span#fcfilter-value").text();
        }
        loadInteractiveImage(plotName,floor,maxFoldChange,showRowSpots,showControls);
      };
    </g:javascript>
     <div class="spacer"></div>
    <div id="datafiles-well" class="well datafiles" style="padding:10px;">
      <h4 style="line-height:18px;"><span>Download Data Files</span></h4>
      <g:select from="${csvFiles}" name="csv-files" noSelection="[null:'Select a file:']"
                optionKey="key" optionValue="value" onchange="downloadFile()" value="null" style="margin-top:10px;"/>
    </div>
    <g:javascript>
      function downloadFile() {
        var fileLink = $("select#csv-files").val();
        if (fileLink !== "null")
        {
          location.href = fileLink;
        }
      };
    </g:javascript>

    <div id="related-well" class="well related" style="padding:10px;">
      <h4 style="line-height:18px;"><span>Related Group Set Results</span></h4>
      <g:select from="${relatedAnalyses}" name="related-analyses" noSelection="[null:'Select an analysis result:']"
                optionKey="id" optionValue="name" onchange="goToAnalysis()" value="null" style="margin-top:10px;"/>
    </div>
    <g:javascript>
      function goToAnalysis() {
        var analysisId = $("select#related-analyses").val();
        if (analysisId !== "null")
        {
          location.href = getBase()+"/analysis/show/"+analysisId;
        }
      };
    </g:javascript>
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

          <a href="javascript:saveChart();" id="download_link" class="icon_download" title="Download Plot as Image"></a>
          <g:link name="download_file" controller="analysis" action="plotFile" id="${analysisInstance.id}" class="icon_download_csv" title="Download Plot CSV File" onclick="updateDownloadLink();"/>
          <g:javascript>
            var updateDownloadLink = function(e) {
              if ($("#sampleLabelOption").is(":visible"))
              {
                var selectedLabel = $("#sampleLabelOption").val();
                var downloadLink = $("a[name='download_file']");
                var link = csvDownloadLink.concat("&sampleField=").concat(selectedLabel);
                downloadLink.attr("href", link);
              }
            };
          </g:javascript>

        </div>
      </div>
      <g:render template="matplot" model="[groups:groups]"/>
    </div><!--end imagebody-->

    <g:render template="/common/emailLink"/>

    <!-- javascript coding -->
    <g:javascript>
      // What is $(document).ready ? See: http://flowplayeplotKeyplotKeyR.org/tools/documentation/basics.html#document_ready
      $(document).ready(function() {
        $("#annotationKey span#${params.annotationKey ?: 'annot_show'}").addClass("active");
        $("#sampleLabelOption").val("${params.sampleField ?: 'sampleBarcode'}");
        if (currentPlotKey === "gsa") {
          $("a#enable-gsa-option").trigger("click");
          $("span[name='difference']").removeClass("active");
//          $("span[name='gsa']").trigger("click");
        }
        updateChoosers();
		    setPublishing(true); // display: block publishing links in matmain
		
        if (currentPlotKey === "gsa") {
          gsaPlotKey();
        } else if (currentPlotKey === "difference") {
          modPlotKey();
        } else if (currentPlotKey === "foldchange") {
          fcPlotKey();
        }

        addLegend("groups", "Groups", groups);

        updateGXBLink();

        var helpTipOptions = {
          show: {
            event: false
          },
          hide: {
            event: false
          },
          style: {
            classes: 'ui-tooltip-tipsy ui-tooltip-shadow'
          }
        };
        $('.help-tips').qtip($.extend({}, helpTipOptions, {
          content: 'Plot type',
          position: {
            my: "left center",
            at: "right center",
            target: $("div#plot-type-well")
          }
        }))
        .removeData('qtip')
        .qtip($.extend({}, helpTipOptions, {
          content: 'Choose the display options for the current plot',
          position: {
            my: "left center",
            at: "right center",
            target: $("div#display-options-well")
          }
        }))
        .removeData('qtip')
        .qtip($.extend({}, helpTipOptions, {
          content: 'Toggle between dog-eared, hidden and full visibility of annotation color keys in the current plot.',
          position: {
            my: "left center",
            at: "right center",
            target: $("div#annotations-well")
          }
        }))
        .removeData('qtip')
        .qtip($.extend({}, helpTipOptions, {
          content: 'This shows the color key for the current plot. You can download the plot key by clicking on the download icon.',
          position: {
            my: "left center",
            at: "right center",
            target: $("div#plotkey-well")
          }
        }))
        .removeData('qtip')
        .qtip($.extend({}, helpTipOptions, {
          content: 'Select a backing data file to download for this analysis.',
          position: {
            my: "left center",
            at: "right center",
            target: $("div#datafiles-well")
          }
        })).removeData('qtip')
        .qtip($.extend({}, helpTipOptions, {
          content: 'Select a related analysis with the same sample set and group set to view.',
          position: {
            my: "left center",
            at: "right center",
            target: $("div#related-well")
          }
        }));
      });
    </g:javascript>

  </div><!--end mat_maincol-->

</div><!--end mat-container-->

<g:render template="/common/bugReporter" model="[tool:'MAT']"/>
<g:render template="moduleParams" model="[ sampleSetName:sampleSetName, groups:groups, analysisInstance:analysisInstance ]"/>

</body>
</html>