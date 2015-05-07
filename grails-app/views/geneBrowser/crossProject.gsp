<%@ page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils; org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap; org.sagres.sampleSet.SampleSet; org.codehaus.groovy.grails.plugins.converters.codecs.JSONCodec; org.codehaus.groovy.grails.web.json.JSONWriter; grails.converters.JSON; org.sagres.sampleSet.component.OverviewComponent" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>
  <title>GXB: ${params.geneSymbol}</title>
 <link rel="stylesheet" href="${resource(dir: 'css', file: 'gxb.css')}"/>

  <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
  <g:javascript src="jquery-ui-min.js"/>

  <g:javascript src="jquery.qtip.min.js"/>
  <g:javascript src="modules/raphael-min.js"/>
  <g:javascript src="modules/raphael.export.js"/>


  <g:javascript>
  $(document).ready(function() {
      $("select#geneListCategorySelect").change(function() {
          $.ajax({
              url: getBase() + "/geneListCategory/geneListCategorySelected",
              data: "id=" + this.value,
              cache: false,
              success: function(htmlString) {
              	// Feed the resulting <select></select> group into the targeted <div></div>
              	$("#targetGeneListSelect").html(htmlString);
              }
            });
         });
       });
  </g:javascript>
        <!-- bootstrap js is in twice; once in this file, once is gxbmain. it won't load right from gxbmain-->
     <g:javascript src="bootstrap-dropdown.js"/>
     <g:javascript src="bootstrap-modal.js"/>
</head>
<body>

<div class="topbar">
  <div class="topbar-inner fill">
    %{--<div class="topbar-inner bri-fill">--}%
    <div class="gxb-container">
      <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

      <h3><g:link controller="geneBrowser" action="list"><strong> GXB</strong></g:link></h3>

      <form onsubmit="submitQuery();return false;">
        <input id="topSearch" type="text" placeholder="Search: Sample Sets" />
        <a href="#" class="btn small primary topbar-clear" onclick="clearSearch();">Reset</a>
      </form>
      <g:javascript>
        $("#topSearch").autocomplete({
          source: function(request, response) {
            request.queryString = request.term;
            request.sampleSetID = currentSampleSetID;
            request.limit = 10;
            $.getJSON(getBase()+"/sampleSet/titles", request, function(data) {
              var items = [];
              $.each(data, function(key, val) {
                items.push({ list: "Sample Set list", label: val.text, value: val.url });
              });
              response(items);
            });
          },
          select: function(event, ui) {
            $("#topSearch").val(ui.item.label);
            submitQuery();
            return false;
          },
          focus: function(event, ui) {
            $("#topSearch").val(ui.item.label);
            return false;
          },
          minLength : 1,
          delay     : 10
        });
      </g:javascript>
      <ul class="nav secondary-nav">
       	<g:if test="${grailsApplication.config.googleplus.on}">
      	   <li><g:link onclick="googlePlusLink(); return false;" class="gplus noshade" title="Post this link to your google+ circles"></g:link></li>
      	</g:if>
         <g:if test="${grailsApplication.config.send.email.on}">
          <li><g:link onclick="generateLink(false,true);return false;" class="email-link noshade" title="Email Link"></g:link></li>
        </g:if>
        <sec:ifLoggedIn>
          <li><g:link onclick="javascript:newGeneNote();return false;" class="note noshade" title="Add Note"></g:link></li>
        </sec:ifLoggedIn>

        <li class="dropdown" data-dropdown="dropdown">
          <a href="#" class="dropdown-toggle">Tools</a>
          <ul class="dropdown-menu skin-dropdown">
               <li id="annotationLink"><g:link controller="sampleSet" action="show" id="${params.id}" target="_annotationWindow">Annotation</g:link></li>
               <li class="divider"></li>
            <li><a href="#" id="chartOptions">Chart Options</a></li>
            <li class="divider"></li>
            %{--<g:if test="${grailsApplication.config.send.email.on}">--}%
              %{--<li><g:link onclick="generateLink(true);return false;"><span id="generateLinkMenu">Email Link</span></g:link></li>--}%
            %{--</g:if>--}%
            <li><g:link onclick="generateLink(false);return false;"><span id="generateLinkMenu">Copy Link</span></g:link></li>
            <li class="divider"></li>
            %{--<g:if test="${grailsApplication.config.send.email.on}">--}%
              %{--<li><a href="#" onclick="reportBug();">Report Bug</a></li>--}%
            %{--</g:if>--}%
            %{--<g:else>--}%
            <li><a href="" onclick="reportBug(true);return false;">Send Feedback</a></li>
            %{--</g:else>--}%
          </ul>
        </li>
        <g:if test="${grailsApplication.config.menu.login}">
        <sec:ifNotLoggedIn>
          <li class="dropdown login-dropdown">
            <a href="#" class="dropdown-toggle" onclick="showLoginForm();">Login</a>
            <div class="dropdown-menu skin-dropdown login-dropdown">
              <div class="login-content">
                <form action="${request.contextPath}/${grailsApplication.config.dm3.authenticationTarget}" method="post">
                <label>Username:</label><br/>
                <input type="text" name="j_username" id="username"/>
                <label>Password: </label><br/>
                <input type="password" name="j_password" id="password" class="login-pw"/>
                <g:hiddenField name="${SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter}" value="${createLink(action:params.action, params:params, absolute:true)}"/>
                <g:link controller="secUser" action="forgotPassword" class="btn small forgot-password">Forgot password</g:link>
                <button class="btn primary login-button" type="submit">Login</button>
                </form>
              </div>
            </div>
          </li>
        </sec:ifNotLoggedIn>
        </g:if>
      </ul>
    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->

<div class="body">
<g:if test="${flash.message}">
  <div class="message">${flash.message}</div>
</g:if>

<g:if test="${errorMsg != null}">
  <div class="alert-message error">${errorMsg}</div>
</g:if>
<g:else>

<div class="gxb-container">
<div class="sidepanel">
  <div class="genelist-hideshow" onclick="toggleSidePanel();"></div>
  <g:javascript>
    var toggleSidePanel = function() {
      $(".sidepanel").toggleClass("hiddenpanel", 500);
      $(".gxb-content").toggleClass("fullsize", 500, function() {
        resizeChart();
      });
    };
  </g:javascript>
  <div id="projectList" class="geneList">
    <div id="projectResultList">
      <%-- <img id="alertMsg" src="${resource(dir:'images/icons', file:'loading_filter.gif')}" alt="Updating..." style="margin:20px auto 0 auto; display:block;"> --%>
      <%-- <div id="alertMsg" class="alert-message block-message info" style="display:none;"><strong>Updating...</strong></div> --%>
      <div id="errorNotFound" class="alert-message block-message error" style="display: none;"><a class="close" href="#" title="Clear the search">&times;</a><strong><span id="notFoundMsg">No Records Found</span></strong></div>
      <div id="samplesets">
      <g:each in="${sampleSets}" var="ss">
        <div id="sampleSetBox_${ss.id}" class="geneBox ${params.long("id") == ss.id ? 'active' : ''}" style="height:51px;overflow:hidden;" title="${ss.name}" onclick="selectSampleSet(${ss.id},this);">
          <div style="font-size:13px;">${ss.name}</div>
        </div>
      </g:each>
      </div>
    </div>
  </div>
</div>
<div class="gxb-content">



<div class="page-header">
  <h4 id="sampleSetTitle">${sampleSet.name}</h4>
  <div class="btn primary" onclick="toggleTabs(this);">Hide Info Panel</div>
  <g:javascript>
    var toggleTabs = function(btn) {
      if ($(btn).html() === "Hide Info Panel") {
        $(btn).html("Show Info Panel");
      } else {
        $(btn).html("Hide Info Panel")
      }
      $("div#tab-content").toggle("blind", 500, function() {
        resizeChart();
      });
    };
  </g:javascript>
</div>

<div>
  <div class="row">
    <div class="span4">Gene Symbol: <span id="currentGeneSymbol">${params.geneSymbol}</span></div>
    <g:if test="${sampleSet.defaultSignalDisplayType?.id == 2}">
    	<div class="span3"><span id="signalType">Log<sub>2</sub> Signal</span>: <span id="currentSignal">--</span></div>
    </g:if>
	<g:elseif test="${sampleSet.defaultSignalDisplayType?.id == 6}">
    	<div class="span3"><span id="signalType">Fold Change</span>: <span id="currentSignal">--</span></div>
    </g:elseif>
    <g:else>
       	<div class="span3"><span id="signalType">Signal</span>: <span id="currentSignal">--</span></div>
    </g:else>
    %{--<g:if test="${hasLabkey}">--}%
      <div id="pid-label" class="span4">Participant ID: <span id="currentPid">--</span></div>
    %{--</g:if>--}%
    %{--<g:else>--}%
      <div id="sid-label" class="span4">Sample ID: <span id="currentSid">--</span></div>
    %{--</g:else>--}%
  </div>
</div>

<div id="tab-content">
  <ul class="pills">
    <li class="active" title="Gene information" id="geneInfoTab"><a href="#tab-gene" style="line-height:26px;">Gene</a></li>
    <li title="Study information"><a href="#tab-study">Study</a></li>
    <li title="Quality control information" class="qctab"><a href="#tab-qc">QC</a></li>
    %{--<g:if test="${hasLabkey}">--}%
      <li title="Subject / patient information" class="clinicaltabs"><a href="#tab-subject">Subject</a></li>
      <li title="Clinical data" class="clinicaltabs"><a href="#tab-clinical">Clinical</a></li>
      <li title="Lab results" class="clinicaltabs"><a href="#tab-labresults">Lab Results</a></li>
      <li title="Flow data" class="clinicaltabs"><a href="#tab-flow">Flow Data</a></li>
      <li title="Ancillary data" class="clinicaltabs"><a href="#tab-ancillary">Ancillary</a></li>
    %{--</g:if>--}%
    <g:if test="${hasTabs}">
      <g:each in="${tabs}" var="t">
        <li title="${t.name}" class="custom-tab"><a href="#tab-${t.name.encodeAsKeyCase()}">${t.name}</a></li>
      </g:each>
    </g:if>
    <li id="tracking-tab" title="Group information"><a href="#tab-group">Tracking</a></li>
  </ul>
  <g:javascript>
    $(document).ready(function() {
      $(".pills li a").live("click", function(e) {
        $(".pills li.active").removeClass("active");
        $(this).closest("li").addClass("active");
        var tab = $(this).attr("href");
        $("div.tab.active").removeClass("active");
        $("div"+tab).addClass("active");
        e.stopImmediatePropagation();
        e.preventDefault();
      });
    });
  </g:javascript>

  <div class="well infopanel">
    <div class="info-scrollPanel">
      <div id="tab-gene" class="tab active">
        <table class="tab-table">
          <tbody>
          <tr><td style="width:80px;"><strong>Full Name</strong></td><td><span id="gene.Description">--</span></td></tr>
          <tr><td style="width:80px;"><strong>Summary</strong></td><td><span id="gene.Summary">--</span></td></tr>
          <tr>
            <td style="width:80px;"><strong>Links</strong></td>
            <td>
              <span id="gene.ncbi_link"><a href="http://www.ncbi.nlm.nih.gov/gene/${params.geneID}" target="_blank"><span class="icon_ncbi"></span></a></span>
              <span id="gene.wolfram_link"><a href="http://www.wolframalpha.com/input/?i=gene+${params.geneSymbol.toUpperCase()}" target="_blank"><span class="icon_wa"></span></a></span>
              <span id="gene.wiki_link"><a href="http://en.wikipedia.org/wiki/${params.geneSymbol.toUpperCase()}" target="_blank"><span class="icon_wikipedia"></span></a></span>
            </td>
          </tr>
          <tr>
            <td style="width:80px;"><strong>Pubmed Articles</strong></td>
            <td><span id="gene-pubmedlinks">--</span></td>
          </tr>
          <tr>
            <td style="width:80px;"><strong>User Notes</strong></td>
            <td><span id="gene-usernotes">--</span></td>
          </tr>
          </tbody>
        </table>
      </div>
      <div id="tab-study" class="tab">
        <table class="tab-table">
          <tbody>
            <tr>
              <td><strong>Description</strong></td>
              <td>${sampleSet.description.replaceAll(/<\/?(?i:td)(.|\n)*?>/,"").decodeHTML()}</td>
            </tr>
            <g:each in="${sampleSetOverviewComponents}" var="ssComponent">
              <g:set var="initValue" value="${fieldValue(bean:sampleSet.sampleSetAnnotation, field:OverviewComponent.get(ssComponent.componentId).annotationName)}"/>
              <g:if test="${initValue && !initValue.isAllWhitespace()}">
                <tr>
                  <td><strong>${OverviewComponent.get(ssComponent.componentId).name}</strong></td>
                  <td>${initValue.decodeHTML()}</td>
                </tr>
              </g:if>
            </g:each>
          </tbody>
        </table>
      </div>

      <g:if test="${hasTabs}">
        <g:each in="${tabs}" var="t">
          <div id="tab-${t.name.encodeAsKeyCase()}" class="tab custom-tab">
            <div id="${t.name.encodeAsKeyCase()}Info">
              <span style="font-style:italic;">No information available.</span>
            </div>
          </div>
        </g:each>
      </g:if>
      <div id="tab-group" class="tab">
        <div id="groupInfo">
          <span style="font-style:italic;">No information available.</span>
        </div>
      </div>

      <div id="tab-qc" class="tab">
        <table id="qcInfo" class="centered-table">
          <thead><th>Background</th><th>Biotin</th><th>Genes001</th><th>Genes005</th><th>Gp95</th><th>Housekeeping</th><th>Noise</th></thead>
          <tbody><tr><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td></tr></tbody>
        </table>
        <div id="tg2Info">
          <div class="row"><div class="span2"><strong>Sample Name</strong></div><div class="span4"><span id="tg2.sample_name">--</span></div><div class="span2"><strong>Cell Population</strong></div><div class="span4"><span id="tg2.cell_population">--</span></div></div>
          <div class="row"><div class="span2"><strong>Donor ID</strong></div><div class="span4"><span id="tg2.donor_id">--</span></div><div class="span2"><strong>Tissue Type</strong></div><div class="span4"><span id="tg2.tissue_type">--</span></div></div>
          <div class="row"><div class="span2"><strong>Collection Date</strong></div><div class="span4"><span id="tg2.collection_date">--</span></div><div class="span2"><strong>Notes</strong></div><div class="span4"><span id="tg2.tg2_notes">--</span></div></div>
        </div>
      </div>
      %{--<g:if test="${hasLabkey}">--}%
      <div id="tab-subject" class="tab">
        <div id="labkeySubject">
          <span style="font-style:italic;">No information available.</span>
        </div>
      </div>
      <div id="tab-clinical" class="tab">
        <div id="labkeyClinical">
          <span style="font-style:italic;">No information available.</span>
        </div>
      </div>
      <div id="tab-labresults" class="tab">
        <span style="font-style:italic;">No information available.</span>
        <table id="labkeyLabResults" class="centered-table">
          <thead><tr></tr></thead>
          <tbody></tbody>
        </table>
      </div>
      <div id="tab-flow" class="tab">
        <span style="font-style:italic;">No information available.</span>
        <table id="labkeyFlow" class="zebra-striped">
          <thead><tr></tr></thead>
          <tbody></tbody>
        </table>
      </div>
      <div id="tab-ancillary" class="tab">
        <div id="labkeyAncillaryData">
          <span style="font-style:italic;">No information available.</span>
        </div>
      </div>
      %{--</g:if>--}%
    </div>
  </div>
</div>
<g:render template="/common/emailLink"/>
<div id="userSettings" class="tab-container pullout">
  <div class="page-header">
    <h4>Chart Options</h4>
  </div>
  <div class="button-group">
    <a href="#tab-titles" class="chart-tab yesNo button pill active">Titles</a>
    <a href="#tab-fonts" class="chart-tab yesNo button pill">Fonts</a>
    <a href="#tab-borders" class="chart-tab yesNo button pill">Borders & Axes</a>
    <a href="#tab-points" class="chart-tab yesNo button pill">Points</a>
  </div>
  <div id="tab-titles" class="option-content active">
    <form>
      <fieldset>
        <div class="clearfix">
          <label for="title">Title</label>
          <div class="input">
            <g:textField name="title" class="setting"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="xTitle">X-Axis Title</label>
          <div class="input">
            <g:textField name="xTitle" class="setting"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="yTitle">Y-Axis Title</label>
          <div class="input">
            <g:if test="${geneCountFlag}">
              <g:textField name="yTitle" value="Normalized Counts" class="setting"/>
            </g:if>
            <g:else>
              <g:textField name="yTitle" value="${params.defYAxisLabel}" class="setting"/>
            </g:else>
          </div>
        </div>
      </fieldset>
    </form>
  </div>
  <div id="tab-fonts" class="option-content">
    <form>
      <fieldset>
        <div class="clearfix">
          <label for="titleFontSize">Title Font Size</label>
          <div class="input">
            <input id="titleFontSize" name="titleFontSize" type="number" value="18" class="small setting"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="axisTitleFontSize">Axis Title Font Size</label>
          <div class="input">
            <input id="axisTitleFontSize" name="axisTitleFontSize" type="number" value="16" class="small setting"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="xFontSize">X-Axis Font Size</label>
          <div class="input">
            <input id="xFontSize" name="xFontSize" type="number" value="16" class="small setting"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="yFontSize">Y-Axis Font Size</label>
          <div class="input">
            <input id="yFontSize" name="yFontSize" type="number" value="16" class="small setting"/>
          </div>
        </div>
      </fieldset>
    </form>
  </div>
  <div id="tab-borders" class="option-content">
    <form>
      <fieldset>
        <div class="clearfix">
          <label>Chart Border</label>
          <div class="input">
            <div id="chartBorder" class="button-group">
              <span class="yesNo button">Yes</span>
              <span class="yesNo button active">No</span>
            </div>
          </div>
        </div>
        <div class="clearfix">
          <label>Show Group Labels</label>
          <div class="input">
            <div id="showGroupLabels" class="button-group">
              <span class="yesNo button${!params.showGroupLabels || params.showGroupLabels == '1' ? ' active' : ''}">Yes</span>
              <span class="yesNo button${!params.showGroupLabels || params.showGroupLabels == '1' ? '' : ' active'}">No</span>
            </div>
          </div>
        </div>
        <div class="clearfix">
        <label>Axes Visible</label>
        <div class="input">
          <div id="axesVisible" class="button-group">
            <span class="yesNo button">X-axis</span>
            <span class="yesNo button">Y-axis</span>
            <span class="yesNo button active">Both</span>
            <span class="yesNo button">None</span>
          </div>
        </div>
      </div>
      </fieldset>
    </form>
  </div>
  <div id="tab-points" class="option-content">
    <form>
      <fieldset>
        <div class="clearfix">
          <label>Show Overlay Lines</label>
          <div class="input">
            <div id="showOverlayLines" class="button-group">
              <span class="yesNo button${params.showOverlayLines == '1' ? ' active' : ''}">Yes</span>
              <span class="yesNo button${params.showOverlayLines == '1' ? '' : ' active'}">No</span>
            </div>
          </div>
        </div>
        <div class="clearfix">
          <label for="pointWidth">Size</label>
          <div class="input">
            <g:select from="[5,6,7,8,9,10]" name="pointWidth" class="small setting" value="5"/>
          </div>
        </div>
      </fieldset>
    </form>
  </div>
  <div class="button-actions" style="clear: both;">
    <button class="btn primary" onclick="closeOptionsPanel();updateChartSettings();">Update</button>
    <button class="btn" onclick="closeOptionsPanel();">Close</button>
  </div>
</div>

<div class="options-buttons">
  <div class="buttons">
  	   <div id="groupset-options-display" class="plot-options-wrapper">
<%--	<form  action="" onsubmit="submitQuery('panel'); return false;"> --%>
		<span id="groupset-options" class="btn primary plot-options disabled" title="Group Sets" onclick="toggleGroupSets(this);" style="width:80px;">
			Group Set
			<span class="button-arrow"></span>
		</span>
		<ul id="groupset-options-options" class="plot-options-options">
		<g:each in="${groupSets}" var="gs">
	        <li><label class="nowidth"><input type="radio" id="groupSet" name="groupSet" onclick="changeGroupSet(this)" value="${gs.key}"${currentGroupSetID == gs.key ? " checked='checked'" : ""}> <span>${gs.value}</span></label></li>
      	</g:each>
		</ul>
<%--
		<span class="input">
        	<g:select class="input groupset-options-options" id="groupSetSelect" onchange="changeGroupSet(); " name="groupSetSelect"
              	from="${groupSets}" optionKey="key" optionValue="value" value="${defaultGroupSetID}" />
        </span>
      </form> --%>
	  </div>
  
      <div class="plot-options-wrapper">
            <span id="plot-types" class="btn primary plot-options plot-type" onclick="togglePlotOptions(this);" title="Plot Type">
             <span class="icon"></span> <span class="button-arrow"></span>
           </span>

           <ul class="plot-options-options graph-options">
           <div class="button-group result">
             <span id="histogram" class="gxbChartType datakey-button button" title="Show a barplot of expression values">Bar Plot</span>
             <span id="boxplot" class="gxbChartType datakey-button button" title="Show a boxplot of expression values">Box Plot</span>
           </div>
         </ul>
         </div>

       <div class="key-options-wrapper">
         <span class="btn key-options chart-options" onclick="toggleKeyOptions(this);" title="Plot Key">
         <span class="icon"></span> <span class="button-arrow"></span>
         </span>
         <div class="legend-dropdown chart-legend-key" style="">
           <div class="icons"><span class="icon_download-small" onclick="exportLegend('chart-legend-content');"></span></div>
           %{--<div class="icon_download" id="chartLegendExport" title="Download Legend as Image (.png)" onclick="saveLegend();return false;" style="float:none;text-align:right;"></div>--}%
           <div id="chart-legend-content"></div>
         </div>
       </div>

           <div class="plot-options-wrapper">
           <span id="overlay-options" class="btn primary plot-options disabled" title="Overlay options" onclick="togglePlotOptions(this);"  style="width:100px;">
         <span class="icon"></span><span class="text">Overlays</span>
         <span class="button-arrow"></span>
          </span>
           <ul class="plot-options-options"></ul>
          </div>

       <div class="key-options-wrapper">
         <span id="overlay-legend" class="btn key-options disabled" onclick="toggleKeyOptions(this);" title="Overlay Key">
         <span class="icon"></span> <span class="button-arrow"></span>
         </span>
         <div class="legend-dropdown group-legend-key">
           <div class="icons"><span class="icon_download-small" onclick="exportLegend('overlay-legend-content');"></span></div>
           <div id="overlay-legend-content"></div>
         </div>
       </div>

  <a href="#" class="icon_download" id="chartExport" title="Download Chart as Image (.png)" onclick="saveChart();return false;"></a>
        </div>
</div>
<g:javascript>
  var toggleGroupSets = function(elt) {
    if (!$(elt).hasClass("disabled")) {
      var visible = $(elt).parent().find("ul.plot-options-options").is(":visible");
      $("ul.plot-options-options").hide();
      if (!visible) {
        $(elt).parent().find("ul.plot-options-options").show();
      }
    }
  };
  var togglePlotOptions = function(elt) {
    if (!$(elt).hasClass("disabled")) {
      var visible = $(elt).parent().find("ul.plot-options-options").is(":visible");
      $("div.legend-dropdown").hide();
      $("ul.plot-options-options").hide();
      if (!visible) {
        $(elt).parent().find("ul.plot-options-options").show();
      }
    }
  };
  var toggleKeyOptions = function(elt) {
    if (!$(elt).hasClass("disabled")) {
      var visible = $(elt).parent().find("div.legend-dropdown").is(":visible");
      $("ul.plot-options-options").hide();
      $("div.legend-dropdown").hide();
      if (!visible) {
        $(elt).parent().find("div.legend-dropdown").show();
      }
    }
  };
</g:javascript>
	<div>
		<div id="signalChart"></div>
		<div id="noSignal" style="display:none"><h4>No signal data found for this sample set.</h4></div>
		<img id="loading" src="${resource(dir:'images/icons', file:'loading_filter.gif')}" class="loadingIcon" alt="Loading..." style="margin:20px auto 0 auto; display:block;"/>
	</div>
</div>
</div>

<g:javascript src="common.js"/>
<g:javascript src="bootstrap-dropdown.js"/>
<script>
  var closeOptionsPanel = function() {
    $("div#userSettings").hide("drop");
  };

  $("a#chartOptions").click(function() {
    var halfPoint = $("div#userSettings").width() / 2;
    var chartWidth = $("div#signalChart").width() / 2;
    var offset = $("div#signalChart").position().left;
    var x = chartWidth - halfPoint + offset;
    $("div#userSettings").css("left", x).toggle("drop");
  });

  $(".yesNo").click(function() {
    $(this).siblings().each(function() {
      $(this).removeClass("active");
    });
    $(this).addClass("active");
  });

  // show the tab content for the selected tab
  $("div#userSettings>div>a.chart-tab").click(function() {
    var tabId = $(this).attr("href");
    var parent = $(this).closest(".tab-container");
    var oldTab = parent.find("div.option-content.active").removeClass("active");
    parent.find(tabId).addClass("active");
  });

  var newGeneNote = function() {
    $("form#noteForm input#reference").val('SAMPLESET:'.concat(currentSampleSetID));
    showNotePanel(updateGeneNotes);
  };
  var showPubmedLinks = function() {
    showInfoBrowser();
  };

  var chartWidth = $("#signalChart").width();

  var currentProbeID = "${params.probeID}";
  var currentGeneID  = "${params.geneID}";
  var currentGeneSymbol = "${params.geneSymbol}";
  var currentSampleSetID = ${params.id};
  var currentGroupSetID = "${defaultGroupSetID}";
  var currentGroupSetSize = "${groupSets.size()}";
  var activeChartType = "${params.chartType ?: 'histogram'}";
  var signalDataTable = "${signalDataTable}";
  var currentRankList = "${params.rankListID ?: '-1'}";
  var hasTg2 = ${hasTg2};
  var hasLabkey = ${hasLabkey};
  var hasOverlays = false;
  var drawChartLegend = true;

  var annotationLink = "${createLink(controller:"sampleSet", action:"show")}";

  var clearSearch = function() {
    $("#topSearch").val("");
    submitQuery();
  };
  var generateArgs = function() {
    var initOverlays = lastNumericalOverlay !== "none" || activeCategories > 0;
    var args =
    {
      _controller: "geneBrowser",
      _action: "crossProject",
      _id: currentSampleSetID,
      defaultGroupSetId: currentGroupSetID,
      probeID: currentProbeID,
      geneSymbol: currentGeneSymbol,
      geneID: currentGeneID,
      currentQuery: $("#topSearch").val(),
      chartType: activeChartType,
      numericalOverlay: lastNumericalOverlay,
      initOverlays: initOverlays
    };
    $.each(categoryOrder, function(k,order) {
      var color = categoricalColors[k];
      args["categorical_overlay:".concat(k)] = order + ":" + color;
    });
    $.extend(args, getUserSettings());
    return args;
  };
  var generateLink = function(showEmail,isClient)
  {
    var args = generateArgs();
    $.getJSON(getBase()+"/miniURL/create", args, function(json) {
      if (showEmail || isClient) {
        var subject = "Gene Expression Browser Link for Gene Symbol " + currentGeneSymbol;
        var text = "Hello,\r\n\r\nI thought you'd be interested in looking at the gene '"+currentGeneSymbol+"' across projects in the Gene Expression Browser.\r\n\r\n" +
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
  var googlePlusLink = function() 
  {
	var args = generateArgs();
	$.getJSON(getBase()+"/miniURL/create", args, function(json) {
		window.open("https://plus.google.com/share?url=" + json.link, '_blank',
				'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=400,width=400');
		return false;
	});
  };

  var updateAnnotationLink = function() {
    var link = annotationLink.concat("/").concat(currentSampleSetID);
    $("#annotationLink a").attr("href", link);
  };

  $(".gxbChartType").click(function() {
    $("span.gxbChartType.active").each(function() {
      $(this).removeClass("active");
    });
    $(this).addClass("active");
    activeChartType = this.id;
    refreshChart();
  });

  var yNToTF = function(yN, tF) {
    var value = yN.toLowerCase();
    if (value.charAt(0) === "y")
    {
      return tF[0];
    }
    return tF[1];
  };

  var getUserSettings = function() {
    var booleans = [true,false];
    var oneZero = [1,0];
    var settings = $(".setting").serializeObject();
    var showBorder = yNToTF($("div#border>span.active").text(), oneZero);
    var showShadow = yNToTF($("div#shadow>span.active").text(), booleans);
    var showChartBorder = yNToTF($("div#chartBorder>span.active").text(), oneZero);
    var showLegend = yNToTF($("div#legend>span.active").text(), booleans);
    var showGroupLabels = yNToTF($("div#showGroupLabels>span.active").text(), booleans);
    var showOverlayLines = yNToTF($("div#showOverlayLines>span.active").text(), booleans);

    var axesVisible = $("div#axesVisible>span.active").text().toLowerCase();
    var xLineWidth = (axesVisible === "x-axis" || axesVisible === "both") ? 1 : 0,
      yLineWidth = (axesVisible === "y-axis" || axesVisible === "both") ? 1 : 0;

    var args = {
      title: settings.title,
      xTitle: settings.xTitle,
      yTitle: settings.yTitle,
      titleStyle: { fontSize: settings.titleFontSize+"px" },
      axisTitleStyle: { fontSize: settings.axisTitleFontSize+"px" },
      xLabelStyle: { fontSize: settings.xFontSize+"px" },
      yLabelStyle: { fontSize: settings.yFontSize+"px" },
      showLegend: showLegend,
      chartBorderWidth: showChartBorder,
      showGroupLabels: showGroupLabels,
      showOverlayLines: showOverlayLines,
      xLineWidth: xLineWidth,
      yLineWidth: yLineWidth,
      pointWidth: parseInt(settings.pointWidth),
      shadow: showShadow,
      borderWidth: showBorder
    };

    return args;
  };
  var showGroupSet = function() {
	  	if (currentGroupSetSize > 1)
	  	{
	  	  	//$("#groupset-options-display").show();
	  		$("#groupset-options").removeClass("disabled");
	  	}
	  	else
	  	{
	  	  	//$("#groupset-options-display").hide();
	  	  	$("#groupset-options").addClass("disabled");
	  	}
  };

  var refreshChart = function() {
    if (activeChartType === "histogram")
    {
      drawBarchart(boxResize||initOverlays);
      boxResize = false;
      initOverlays = false;
    }
    else if (activeChartType === "boxplot")
    {
      drawBox();
    }
  };

  var bRaphael = null, plotSettings = null;
  var pointSet = null, scatterSet = null, axisSet = null, groupLabelSet = null;
  var originalWidth = null, originalHeight = null;
  var eltIds = [0,1,2,3,4,5,6,7]; // bg, title, xAxis, xAxisMaxTick, xAxisMinTick, xAxisTitle, yAxis, yAxisTitle
  var tooltip = function(elt,text) {
    elt.qtip({
      content: {
        text: text
      },
      position: {
        my: "bottom left",
        at: "top center",
        viewport: $(window)
      },
      show: {
        solo: true,
        ready: true,
        delay: 0
      },
      style: {
        classes: 'ui-tooltip-tipsy'
      }
    });
  };
  var saveChart = function() {
    var svg = null;
    if ($.browser.msie && $.browser.version < 9) {
      svg = bRaphael.toSVG();
    } else {
      svg = $("div#signalChart").html();
    }
    $.post(getBase()+"/charts/saveImg", { svg: svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      var filename = "sampleset${sampleSet.id}_groupset"+currentGroupSetID+"_"+currentGeneSymbol;
      window.location.href = location.concat("?filename=").concat(filename);
    });
  };
  var exportLegend = function(divId) {
    // create svg
    var addTitle = divId === "overlay-legend-content";
    var legendR = Raphael(0,0,200,200);
    var mx = 0, y = 10;
    $("div#"+divId+" div.row").each(function(i,elt) {
      var it = $(elt);
      if (addTitle && it.prev().is(":not(div.row)"))
      {
        y += 5;
        var lt = legendR.text(8, y+10, it.prev("div").text()).attr({ fill:"#333", font:"14px Arial", "font-weight":"bold", "text-anchor":"start" });
        mx = Math.max(mx, lt.getBBox().width);
        y += 20;
      }
      var color = it.find(".swatch").css("background-color");
      var text = it.find(".span4").html();
      legendR.rect(10, y, 12, 12).attr({ fill:color, stroke:"none", "stroke-width":0 });
      var t = legendR.text(28, y+7, text).attr({ fill:"#333", font:"12px Arial", "text-anchor":"start" });
      mx = Math.max(mx, t.getBBox().width);
      y += 16;
    });
    legendR.rect(0,0,mx+45,y+10).attr({ fill:"#fff", stroke:"#333", "stroke-width":1 }).toBack();
    legendR.setSize(mx+47,y+12);
    var svg = legendR.toSVG();
    legendR.clear();
    delete legendR;
    $.post(getBase()+"/charts/saveImg", { svg: svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      var filename = "sampleset${sampleSet.id}_groupset"+currentGroupSetID+"_"+divId;
      window.location.href = location.concat("?filename=").concat(filename);
    });
  };
  var lastBar = null, lastScatterPoint = null, lastHighlightedSample = null;
  var barMouseover = function() {
    if (lastBar !== null)
    {
      barMouseout();
    }
    this.attr({ "fill-opacity":0.7 });
    lastBar = this;
    lastHighlightedSample = this.data("id");
    $("#currentSignal").html(this.data("value").toFixed(2));
    $("#currentSid").html(this.data("barcode"));
    updateInfoPanel(lastHighlightedSample);
  };
  var barMouseout = function() {
    if (numericalSet !== null)
    {
      lastBar.attr({ "fill-opacity":0.3 });
    }
    else
    {
      lastBar.attr({ "fill-opacity":1 });
    }
  };
  var updateChartSettings = function() {
    var settings = getUserSettings();

    // bg, title, yAxis, yAxisMaxTick, yAxisMinTick, yAxisTitle, xAxis, xAxisTitle
    // chart border
    var bgStroke = (settings.chartBorderWidth > 0) ? "#000" : "none";
    bRaphael.getById(eltIds[0]).attr({ stroke:bgStroke, "stroke-width":settings.chartBorderWidth });
    axisSet.attr({ font:settings.xLabelStyle.fontSize+" Helvetica" });
    bRaphael.getById(eltIds[1]).attr({ text:settings.title, font:settings.titleStyle.fontSize+" Helvetica" });
    bRaphael.getById(eltIds[5]).attr({ text:settings.yTitle, font:settings.axisTitleStyle.fontSize+" Helvetica" });
    bRaphael.getById(eltIds[7]).attr({ text:settings.xTitle, font:settings.axisTitleStyle.fontSize+" Helvetica" });
    bRaphael.getById(eltIds[3]).attr({ font:settings.yLabelStyle.fontSize+" Helvetica" });
    bRaphael.getById(eltIds[4]).attr({ font:settings.yLabelStyle.fontSize+" Helvetica" });
    bRaphael.getById(eltIds[2]).attr({ width:settings.yLineWidth });
    bRaphael.getById(eltIds[6]).attr({ height:settings.xLineWidth });

    if (settings.showGroupLabels) {
      groupLabelSet.show();
    } else {
      groupLabelSet.hide();
    }

    if (settings.showOverlayLines) {
      toggleNumericalLines(true);
    } else {
      toggleNumericalLines(false);
    }
  };
  var boxResize = false;
  var resizeChart = function() {
    if ($(".gxbChartType.active").attr("id") === "histogram")
    {
      drawBarchart(true);
    }
    else
    {
      boxResize = true;
      drawBox();
    }
  };
  var showLoading = function() {
    $("div#noSignal").hide();
    $("div#signalChart").hide();
    $("img#loading").show();
  };
  var drawBarchart = function(redrawOverlays) {
    if (currentProbeID !== "")
    {
      var args =
      {
        sampleSetId: currentSampleSetID,
        groupSetId: currentGroupSetID,
        probeId: currentProbeID,
	    geneSymbol: '${params.geneSymbol}',
        signalDataTable: signalDataTable,
        rankListId: currentRankList,
        rankListVisibility: "hidden"
      };
      showLoading();
      $.getJSON(getBase()+"/charts/groupSetHistogram", args, function(json) {
        $("img#loading").hide();
        if (json.error)
      	{
      		$("#noSignal").html(json.error.message);
      		$("#noSignal").show();
      	}
        else if (json.groups.length > 0)
        {
          var numGroups = json.groups.length, numPoints = 0;
          $("div#noSignal").hide();
          $("div#signalChart").show();
          var div = $("#signalChart");
          var width = div.width();
          var height = $(window).height() - div.position().top - 40;
          showOverlay();

          $.each(json.groups, function(i,g) {
            numPoints += g.points.length;
            if (drawChartLegend) {
              $("div#chart-legend-content").append("<div class='row'><span class='swatch' style='background-color:"+g.color+"'></span><div class='span4'>"+g.label+"</div></div>");
            }
          });
          drawChartLegend = false;
          var pSpace = 2, xAxisHeight = 60, yAxisWidth = 85, padding = [10,40,10,10]; // top,right,bottom,left
          var visibleWidth = width - yAxisWidth - padding[1] - 5;
          var visibleHeight = height - xAxisHeight - padding[0] - padding[2];
          var cWidth = visibleWidth;
          var pWidth = visibleWidth / (numPoints + numGroups - 1) - pSpace;
          if (pWidth < 8)
          {
            pWidth = 8;
            pSpace = 1;
            cWidth = (pWidth + pSpace) * (numPoints + numGroups);
            width = yAxisWidth + cWidth + padding[1] + padding[3];
          }

          plotSettings = {
            width: width,
            height: height,
            plotWidth: cWidth,
            plotHeight: visibleHeight,
            xAxisHeight: xAxisHeight,
            yAxisWidth: yAxisWidth,
            pointWidth: pWidth,
            pointSpace: pSpace,
            groupSpace: pWidth,
            padding: padding
          };

          Raphael.fn.barchart = function(json) {
            var options = $.extend({}, plotSettings);
            var paper = this;
            var topOffset = activeCategories * 10;
            var maxY = Math.round(Math.ceil(json.max));
            var offsetHeight = options.plotHeight - 40;
            var groupOffset = options.yAxisWidth;
            if (pointSet === null)
            {
              pointSet = bRaphael.set();
            }
            else
            {
              try { pointSet.remove(); } catch(e) {}
              pointSet.clear();
            }
            if (groupLabelSet === null) {
              groupLabelSet = bRaphael.set();
            }
            $.each(json.groups, function(i,g) {
              $.each(g.points, function(i,p) {
                var h = Math.max(2.0, (p.y / maxY) * offsetHeight);
                var x = groupOffset + p.x * (options.pointWidth + options.pointSpace);
                var y = padding[0] + options.plotHeight - h;
                var bar = paper.rect(x, y, options.pointWidth, h).attr({ fill:g.color, stroke:"none" });
                bar.data("value",p.y);
                $.each(p.data, function(k,v) {
                  bar.data(k,v);
                });
                bar.mouseover(barMouseover);
                if (p.data.id == lastHighlightedSample)
                {
                  //                bar.attr({ "fill-opacity":0.7 });
                  lastBar = bar;
                }
                pointSet.push(bar);
                if (i === g.points.length - 1)
                {
                  var tX = (x + options.pointWidth + groupOffset) / 2;
                  var gLabel = paper.text(tX, topOffset + padding[0] + options.plotHeight + 14, g.label).attr({ fill:g.color, font:"12px Helvetica", "font-weight":"bold" });
                  groupLabelSet.push(gLabel);
                  axisSet.push(gLabel);
                }
              });
              groupOffset += g.points.length * (options.pointWidth + options.pointSpace) + options.groupSpace;
            });
          };

          var topOffset = activeCategories * 10;
          if (bRaphael !== null)
          {
            bRaphael.setSize(width, topOffset + height);
          }
          else
          {
            bRaphael = Raphael("signalChart", width, topOffset + height);
          }

          bRaphael.customAttributes.moveUp = function(offset) {
            return { y:(this.attr("y")+offset) };
          };

          if (axisSet !== null) {
            try { axisSet.remove(); } catch(e) {}
            axisSet.clear();
            groupLabelSet.clear();
          }
          if (scatterSet !== null) {
            try { scatterSet.remove(); } catch(e) {}
            scatterSet.clear();
          }

          // draw title
          bRaphael.setStart();
          eltIds[0] = bRaphael.rect(0,0,width,topOffset+height).attr({ fill:"#fff", stroke:"none", "storke-width":0 }).id;
          eltIds[1] = bRaphael.text(plotSettings.width / 2, padding[0] + 10, "").attr({ fill:"#000", font:"18px Helvetica" }).id;

          // draw y-axis
          eltIds[2] = bRaphael.rect(plotSettings.yAxisWidth - 5, padding[0] + 40, 1, plotSettings.plotHeight - 40).attr({ fill:"#333", stroke:"none", "stroke-width":0 }).id;
          eltIds[3] = bRaphael.text(plotSettings.yAxisWidth - 10, padding[0] + 40, Math.round(Math.ceil(json.max))).attr("text-anchor","end").attr({ fill:"#333", font:"16px Helvetica" }).id;
          eltIds[4] = bRaphael.text(plotSettings.yAxisWidth - 10, padding[0] + plotSettings.plotHeight, 0).attr("text-anchor","end").attr({ fill:"#333", font:"16px Helvetica" }).id;
          eltIds[5] = bRaphael.text(plotSettings.yAxisWidth - 50, padding[0] - 35 + (plotSettings.plotHeight + 40) / 2, "Expression Values").attr({ fill:"#333", font:"16px Helvetica" }).rotate(-90, plotSettings.yAxisWidth - 40, (plotSettings.plotHeight + 40) / 2).id;

          // draw x-axis
          eltIds[6] = bRaphael.rect(plotSettings.yAxisWidth - 5, padding[0] + plotSettings.plotHeight, plotSettings.plotWidth + 10, 1).attr({ fill:"#333", stroke:"none", "stroke-width":0 }).id;
          eltIds[7] = bRaphael.text(plotSettings.yAxisWidth + (plotSettings.plotWidth / 2), topOffset + padding[0] + plotSettings.plotHeight + 50, "").attr({ fill:"#333", font:"16px Helvetica" }).id;
          axisSet = bRaphael.setFinish();

          bRaphael.barchart(json);
          updateChartSettings();
          if (redrawOverlays)
          {
            redrawOverlay();
          }
          else
          {
            if (numericalSet !== null) {
              pointSet.attr("fill-opacity", 0.3);
              numericalSet.toFront();
            }
            if (categoricalSets !== null)
            {
              $.each(categoricalSets, function(key,set) {
                set.toFront();
              });
            }
          }
          if (lastBar) {
            lastBar.attr({ "fill-opacity":0.7 });
          }
        }
        else
        {
          $("div#signalChart").hide();
          $("div#noSignal").show();
        }
      });
    }
  };
  var boxMouseover = function() {
    var boxText = "1st: " + this.data("quartiles").first + "<br/>";
    boxText += "2nd: " + this.data("quartiles").second + "<br/>";
    boxText += "3rd: " + this.data("quartiles").third + "<br/>";
    boxText += "min: " + this.data("min") + "<br/>";
    boxText += "max: " + this.data("max");
    tooltip($(this.node),boxText);
  };
  var scatterMouseover = function() {
    if (lastScatterPoint !== null)
    {
      scatterMouseout();
    }
    lastScatterPoint = this;
    lastHighlightedSample = this.data("id");
    var color = this.attr("fill");
    this.attr({ "r":5, fill:"#fff", stroke:color, "stroke-width":2 });
    $("#currentSignal").html(this.data("value").toFixed(2));
    $("#currentSid").html(this.data("barcode"));
    updateInfoPanel(lastHighlightedSample);
  };
  var scatterMouseout = function() {
    var color = lastScatterPoint.attr("stroke");
    lastScatterPoint.attr({ "r":3, fill:color, stroke:"none", "stroke-width":0 });
  };
  var drawBox = function()
  {
    if (currentProbeID !== "")
    {
      var args =
      {
        sampleSetId: currentSampleSetID,
        groupSetId: currentGroupSetID,
        probeId: currentProbeID,
        geneSymbol: '${params.geneSymbol}',
        signalDataTable: signalDataTable,
        rankListId: currentRankList,
        rankListVisibility: "hidden"
      };
      showLoading();
      $.getJSON(getBase()+"/charts/groupSetHistogram", args, function(json) {
        $("img#loading").hide();
        if (json.error)
      	{
      		$("#noSignal").html(json.error.message);
      		$("#noSignal").show();
      	}
        else if (json.groups.length > 0)
        {
          var numGroups = json.groups.length;
          $("div#signalChart").show();
          $("div#noSignal").hide();
          var div = $("#signalChart");
          var width = div.width();
          var height = $(window).height() - div.position().top - 40;
          hideOverlay();
          var xAxisHeight = 60, yAxisWidth = 85, padding = [10,40,10,10];
          var visibleWidth = width - yAxisWidth - padding[1] - 5;
          var visibleHeight = height - xAxisHeight - padding[0] - padding[2];
          var cWidth = visibleWidth;
          var gSpace = 20;
          var bWidth = visibleWidth / numGroups - gSpace;
          if (bWidth < 50)
          {
            bWidth = 50;
            cWidth = (bWidth + gSpace) * numGroups - gSpace;
            width = yAxisWidth + cWidth + padding[1] + padding[3];
          }
          else if (bWidth > 200)
          {
            bWidth = 200;
          }

          plotSettings = {
            width: width,
            height: height,
            plotWidth: cWidth,
            plotHeight: visibleHeight,
            xAxisHeight: xAxisHeight,
            yAxisWidth: yAxisWidth,
            pointWidth: bWidth,
            pointSpace: 0,
            groupSpace: gSpace
          };

          Raphael.fn.boxplot = function(json) {
            var options = $.extend({}, plotSettings);
            var paper = this;
            var maxY = Math.round(Math.ceil(json.max));
            var offsetHeight = options.plotHeight - 40;
            var groupOffset = options.yAxisWidth + options.groupSpace;
            if (pointSet === null)
            {
              pointSet = bRaphael.set();
            }
            else
            {
              try { pointSet.remove(); } catch(e) {}
              pointSet.clear();
            }
            if (groupLabelSet === null) {
              groupLabelSet = bRaphael.set();
            }
            if (scatterSet === null)
            {
              scatterSet = bRaphael.set();
            }
            else
            {
              try { scatterSet.remove(); } catch(e) {}
              scatterSet.clear();
            }
            $.each(json.groups, function(i,g) {
              var x = groupOffset + i * (options.pointWidth + options.groupSpace);
              var maxWhiskerY = padding[0] + options.plotHeight - (g.data.max / maxY) * offsetHeight;
              var minWhiskerY = padding[0] + options.plotHeight - (g.data.min / maxY) * offsetHeight;
              var firstY = padding[0] + options.plotHeight - (g.data.quartiles.first / maxY) * offsetHeight;
              var thirdY = padding[0] + options.plotHeight - (g.data.quartiles.third / maxY) * offsetHeight;
              var secondY = padding[0] + options.plotHeight - (g.data.quartiles.second / maxY) * offsetHeight;
              var midX = x + (options.pointWidth / 2);
              var quartX = x + (options.pointWidth / 4);
              var whiskerEndWidth = options.pointWidth / 2;
              var box = paper.rect(x, thirdY, options.pointWidth, firstY-thirdY).attr({ fill:"#fff", stroke:g.color, "stroke-width":2 });
              $.each(g.data, function(k,v) {
                box.data(k,v);
              });
              pointSet.push(box);
              pointSet.push(paper.rect(x, secondY, options.pointWidth, 2).attr({ fill:g.color, stroke:"none", "stroke-width":0 }));
              pointSet.push(paper.path("M"+midX+" "+maxWhiskerY+"L"+midX+" "+thirdY).attr({ stroke:g.color, "stroke-width":2, "stroke-dasharray":"- " }));
              pointSet.push(paper.rect(quartX, maxWhiskerY, whiskerEndWidth, 1.5).attr({ fill:g.color, stroke:"none", "stroke-width":0 }));
              pointSet.push(paper.path("M"+midX+" "+firstY+"L"+midX+" "+minWhiskerY).attr({ stroke:g.color, "stroke-width":2, "stroke-dasharray":"- " }));
              pointSet.push(paper.rect(quartX, minWhiskerY, whiskerEndWidth, 1.5).attr({ fill:g.color, stroke:"none", "stroke-width":0 }));
              var groupLabel = paper.text(midX, padding[0] + options.plotHeight + 18, g.label).attr({ fill:g.color, font:"12px Helvetica", "font-weight":"bold" });
              pointSet.push(groupLabel);
              groupLabelSet.push(groupLabel);

              $.each(g.points, function(i,p) {
                var y = padding[0] + options.plotHeight - (p.y / maxY) * offsetHeight;
                var px = x + Math.floor(Math.random() * (options.pointWidth - 1));
                var sp = paper.circle(px,y,3).attr({ fill:g.color, stroke:"none", "stroke-width":0 });
                sp.data("value",p.y);
                $.each(p.data, function(k,v) {
                  sp.data(k,v);
                });
                if (p.data.id == lastHighlightedSample)
                {
                  sp.attr({ "r":5, fill:"#fff", stroke:g.color, "stroke-width":2 });
                  lastScatterPoint = sp;
                }
                sp.mouseover(scatterMouseover);
                scatterSet.push(sp);
              });
            });
          };

          if (bRaphael !== null)
          {
            bRaphael.setSize(width, height);
          }
          else
          {
            bRaphael = Raphael("signalChart", width, height);
          }

          if (axisSet !== null) {
            try { axisSet.remove(); } catch(e) {}
            axisSet.clear();
            groupLabelSet.clear();
          }

          // draw title
          bRaphael.setStart();
          eltIds[0] = bRaphael.rect(0,0,width,height).attr({ fill:"#fff", stroke:"none", "storke-width":0 }).id;
          eltIds[1] = bRaphael.text(plotSettings.width / 2, padding[0] + 10, "").attr({ fill:"#000", font:"18px Helvetica" }).id;

          // draw y-axis
          eltIds[2] = bRaphael.rect(plotSettings.yAxisWidth - 5, padding[0] + 40, 1, plotSettings.plotHeight - 38).attr({ fill:"#333", stroke:"none", "stroke-width":0 }).id;
          eltIds[3] = bRaphael.text(plotSettings.yAxisWidth - 10, padding[0] + 40, Math.round(Math.ceil(json.max))).attr("text-anchor","end").attr({ fill:"#333", font:"12px Helvetica" }).id;
          eltIds[4] = bRaphael.text(plotSettings.yAxisWidth - 10, padding[0] + plotSettings.plotHeight + 2, 0).attr("text-anchor","end").attr({ fill:"#333", font:"12px Helvetica" }).id;
          eltIds[5] = bRaphael.text(plotSettings.yAxisWidth - 50, padding[0] - 35  + (plotSettings.plotHeight + 40) / 2, "Expression Value").attr({ fill:"#333", font:"14px Helvetica" }).rotate(-90, plotSettings.yAxisWidth - 40, (plotSettings.plotHeight + 40) / 2).id;

          // draw x-axis
          eltIds[6] = bRaphael.rect(plotSettings.yAxisWidth - 5, padding[0] + plotSettings.plotHeight + 2, plotSettings.plotWidth + 10, 1).attr({ fill:"#333", stroke:"none", "stroke-width":0 }).id;
          eltIds[7] = bRaphael.text(plotSettings.yAxisWidth + (plotSettings.plotWidth / 2), padding[0] + plotSettings.plotHeight + 55, "Group Set").attr({ fill:"#333", font:"14px Helvetica" }).id;
          axisSet = bRaphael.setFinish();

          bRaphael.boxplot(json);
          updateChartSettings();
        }
        else
        {
          $("div#signalChart").hide();
          $("div#noSignal").show();
        }
      });
    }
  };

  var initOverlays = ${params.initOverlays ?: false};
  var colorSchemes = [5,4,3,2,1,0];
  var categoricalSets = {}, categoryOrder = {}, categoricalColors = {};
  var activeCategories = 0, numericalSet = null, numericalLine = null, lastNumericalOverlay = "${params.numericalOverlay ?: 'none'}";
  <g:each in="${params.keySet()}" var="p">
    <g:if test="${p.startsWith('categorical_overlay')}">
    <g:if test="${!(params.get(p) instanceof GrailsParameterMap)}">
    <g:set var="v" value="${params.get(p).split(':')}"/>
      categoryOrder["${p.substring(20)}"] = ${v[0]};
      categoricalColors["${p.substring(20)}"] = ${v[1]};
    </g:if>
    </g:if>
  </g:each>

  var redrawOverlay = function() {
    var cats = new Array();
    $.each(categoryOrder, function(k,i) {
      cats[i] = k;
      if (categoricalSets[k] && categoricalSets[k] !== "") {
        categoricalSets[k].remove();
      }
    });
    categoricalSets = {};
    categoryOrder = {};
    if (numericalSet !== null) {
      numericalSet.remove();
      numericalSet = null;
      numericalLine = null;
    }
    activeCategories = 0;
    $("div.group-legend-key div#overlay-legend-content").html("");
    $.each(cats, function(i,k) {
      if (!initOverlays) {
        colorSchemes.push(categoricalColors[k]);
      }
      delete categoricalColors[k];
      drawOverlay("categorical",$("input[name='"+k+"']")[0]);
    });
    var line = $("input[name='lineOverlay']:checked").val();
    if (line !== "none") {
      drawOverlay("numerical", $("input[name='lineOverlay']:checked")[0]);
    }
  };
  var drawOverlay = function(datatype,inputField) {
    var overlayType = datatype;
    var draw = true, field = null;
    if (overlayType === "numerical") {
      field = $(inputField).val();
      if (!field || field === "none")
      {
      	if (numericalSet != null)
      	{
        	numericalSet.remove();
        	numericalSet = null;
        }
        numericalLine = null;
        pointSet.attr("fill-opacity", 1);
        draw = false;
        $("div.group-legend-key div[name='"+lastNumericalOverlay+"']").detach();
      }
    } else {
      field = inputField.id;
      if (!$(inputField).is(":checked")) {
        draw = false;
        removeCategoricalOverlay(field);
      }
      if (activeCategories >= 6) {
        draw = false;
      }
    }
    if (bRaphael !== null && pointSet !== null && draw)
    {
      var colorScheme = null
      if (overlayType === "categorical")
      {
        colorScheme = colorSchemes.pop();
      }
      var args =
      {
        sampleSetId: currentSampleSetID,
        field: field,
        colorScheme: colorScheme
      };
      $.ajax({
        url: getBase()+"/charts/groupSetOverlay",
        dataType: 'json',
        data: args,
        cache: false,
        async: false,
        success: function(json){
          if (overlayType === "numerical") {
            if (numericalSet === null)
            {
              numericalSet = bRaphael.set();
            }
            else
            {
              numericalSet.remove();
              numericalSet.clear();
            }
            drawNumericalOverlay(field,json,json.key,json.displayName);
          } else {
            drawCategoricalOverlay(field,json,json.key,json.displayName,colorScheme);
          }
        }
      });
    }
    else
    {
      if (field !== "none") {
        $(inputField).attr("checked",false);
      }
    }
  };
  var clearOverlays = function() {
    categoricalSets = {};
    categoricalColors = {};
    categoryOrder = {};
    colorSchemes = [5,4,3,2,1,0];
    activeCategories = 0;
    numericalSet = null;
    numericalLine = null;
    lastNumericalOverlay = "none";

    $("span#overlay-options ul.plot-options-options").html("");
    if (!$("span#overlay-options").hasClass("disabled")) {
      $("span#overlay-options").addClass("disabled");
    }
    $("span#overlay-legend div").html("");
    if (!$("span#overlay-legend").hasClass("disabled")) {
      $("span#overlay-legend").addClass("disabled");
    }
  };
  var removeCategoricalOverlay = function(field) {
    groupLabelSet.attr({ moveUp: -10});
    var bgBox = bRaphael.getById(eltIds[0]);
    var newHeight = bgBox.attr("height")-10;
    bgBox.attr({ width:bgBox.attr("width"), height:newHeight});
    bRaphael.getById(eltIds[7]).attr({ moveUp:-10 });
    bRaphael.setSize(bgBox.attr("width"), newHeight);

    categoricalSets[field].remove();
    delete categoricalSets[field];

    var order = categoryOrder[field];
    delete categoryOrder[field];

    colorSchemes.push(categoricalColors[field]);
    delete categoricalColors[field];

    $.each(categoryOrder, function(f,o) {
      if (o > order) {
        categoryOrder[f]--;
        categoricalSets[f].attr({ moveUp: -10});
      }
    });

    activeCategories--;

    // remove legend
    $("div[name='"+field+"']").detach();
    if (activeCategories === 0)
    {
      $("#overlay-legend").addClass("disabled");
      $("div.group-legend-key").hide();
    }

    if (activeCategories < 6) {
      $("#categorical-overlays input:not(:checked)").removeAttr("disabled");
    }
  };
  var hideOverlay = function() {
    if (numericalSet !== null)
    {
      numericalSet.hide();
    }
    $.each(categoricalSets, function(k,s) {
      s.hide();
    });
    $("#overlay-legend").addClass("disabled");
    $("span#overlay-options").addClass("disabled");
  };
  var showOverlay = function() {
    if (numericalSet !== null)
    {
      numericalSet.show();
    }
    $.each(categoricalSets, function(k,s) {
      s.show();
    });
    if (activeCategories > 0 || numericalSet !== null) {
      $("#overlay-legend").removeClass("disabled");
    }
    if (hasOverlays) {
      $("span#overlay-options").removeClass("disabled");
    }
  };
  var categoricalMouseover = function() {
    tooltip($(this.node),this.data("tooltip"));
  };
  var drawCategoricalOverlay = function(origField,json,field,header,color) {
    if ($("#overlay-legend").hasClass("disabled")) {
      $("#overlay-legend").removeClass("disabled");
    }
    groupLabelSet.attr({ moveUp: 10});
    var bgBox = bRaphael.getById(eltIds[0]);
    var newHeight = bgBox.attr("height")+10;
    bgBox.attr({ width:bgBox.attr("width"), height:newHeight });
    bRaphael.getById(eltIds[7]).attr({ moveUp:10 });
    bRaphael.setSize(bgBox.attr("width"), newHeight);

    var yOffset = activeCategories * 10;
    var categorySet = bRaphael.set();
    var categories = json.categories;
    var result = {};
    $.each(json.data, function(i,point) {
      result[point.sampleId] = point[field];
    });
    pointSet.forEach(function(el) {
      var value = result[el.data("id")];
      var tooltip = header.concat(": ").concat(value);
      if (value)
      {
        var color = categories[value];
        categorySet.push(bRaphael.rect(el.attr("x"), yOffset + el.attr("y") + el.attr("height") + 3, el.attr("width"), 8).attr({ fill:color, stroke:"none" }).data("tooltip",tooltip).mouseover(categoricalMouseover));
      }
    });
    categoricalSets[origField] = categorySet;
    categoryOrder[origField] = activeCategories;
    categoricalColors[origField] = color;
    activeCategories++;

    // add legend
    var legend = "<div name='"+origField+"'><div style='font-weight:bold;padding-top:2px;margin-left:-20px;'>"+header+"</div>";
    $.each(categories, function(cat,clr) {
      legend += "<div class='row'>";
      legend += "<span class='swatch' style='background-color:"+clr+"'></span><div class='span4'>"+cat+"</div>";
      legend += "</div>";
    });
    legend += "</div>";
    $("div.group-legend-key div#overlay-legend-content").append(legend);
  };
  var numericalMouseover = function() {
    this.attr({ stroke:"#f00", "stroke-width":2, fill:"#fff", r:5 });
    this.toFront();
    tooltip($(this.node),this.data("tooltip"));
  };
  var numericalMouseout = function() {
    this.attr({ stroke:"none", "stroke-width":0, fill:"#f00", r:3 });
  };
  var toggleNumericalLines = function(show) {
    if (numericalLine !== null) {
      if (show) {
        numericalLine.attr({ stroke:"#f00", "stroke-width":1 });
      } else {
        numericalLine.attr({ stroke:"#fff", "stroke-width":0 });
      }
    }
  };
  var drawNumericalOverlay = function(origField,json,field,header) {
    if (lastNumericalOverlay !== "none")
    {
      $("div.group-legend-key div[name='"+lastNumericalOverlay+"']").detach();
    }
    if ($("#overlay-legend").hasClass("disabled")) {
      $("#overlay-legend").removeClass("disabled");
    }
    var result = {};
    var max = 0, min = 0, absMax = 0;
    $.each(json.data, function(i,point) {
      result[point.sampleId] = point[field];
      max = Math.max(max, point[field]);
      min = Math.min(min, point[field]);
    });
    max = Math.round(Math.ceil(max));
    min = Math.round(Math.floor(min));
    absMax = max + Math.abs(min);

    var height = plotSettings.plotHeight - 40;
    var posH = max / absMax * height;
    var negH = height - posH;
    var path = "";
    var breakFound = false;
    pointSet.forEach(function(el,j) {
      var value = result[el.data("id")];
      var tooltip = header.concat(": ").concat(value);
      if (value)
      {
        var x = el.attr("x") + el.attr("width") / 2;
        var y = value < 0 ? value / min * negH + posH + 50 : posH - (value / max * posH) + 50;
        if (j === 0 || breakFound)
        {
          path += "M";
          breakFound = false;
        }
        else
        {
          path += "L";
        }
        path += x + " " + y;
        numericalSet.push(bRaphael.circle(x,y,3).attr({ fill:"#f00", stroke:"none" }).data("tooltip",tooltip).mouseover(numericalMouseover).mouseout(numericalMouseout));
      }
      else
      {
        breakFound = true;
      }
    });
    pointSet.attr("fill-opacity", 0.3);
    if (lastBar !== null)
    {
      lastBar.attr("fill-opacity", 0.7);
    }
    var x = plotSettings.yAxisWidth + plotSettings.plotWidth;
    var y = plotSettings.height - plotSettings.xAxisHeight;
    numericalLine = bRaphael.path(path).attr({ stroke:"#f00", "stroke-width":1 });
    numericalSet.push(numericalLine);
    numericalSet.push(bRaphael.rect(x + 5, 50, 1, plotSettings.plotHeight - 39).attr({ fill:"#f00", stroke:"none", "stroke-width":0 }));
    var maxNum = bRaphael.text(x + 10, 50, max).attr({ "text-anchor":"start", fill:"#f00", font:"16px Helvetica" });
    var mWidth = maxNum.getBBox().width;
    numericalSet.push(maxNum);
    numericalSet.push(bRaphael.text(x + 10, y - 10, min).attr({ "text-anchor":"start", fill:"#f00", font:"16px Helvetica" }));
    numericalSet.push(bRaphael.text(x, 25 + mWidth + (y / 2), header).attr({ fill:"#f00", font:"16px Helvetica" }).transform("r-90,"+(x + 10)+","+(10 + (y / 2))));
    var newWidth = plotSettings.width + mWidth + 25;
    bRaphael.setSize(newWidth, plotSettings.height);
    bRaphael.getById(eltIds[0]).attr({ width:newWidth });

    // add legend
    if (origField !== "none")
    {
      var legend = "<div name='"+origField+"'><div style='font-weight:bold;padding-top:2px;margin-left:-20px;'>"+header+"</div>";
      legend += "<div class='row'>";
      legend += "<span class='swatch' style='background-color:#f00'></span><div class='span4'>"+header+"</div>";
      legend += "</div></div>";
      $("div.group-legend-key div#overlay-legend-content").append(legend);
    }

    var usrArgs = getUserSettings();
    if (!usrArgs.showOverlayLines) {
      toggleNumericalLines(false);
    }

    lastNumericalOverlay = origField;
  };

  var selectSampleSet = function(sampleSetId, selection)
  {
    $("#projectResultList div.active").removeClass("active");
    $(selection).addClass("active");
    currentSampleSetID = sampleSetId;
    drawChartLegend = true;
    $("div#chart-legend-content").html("");
    lastBar = null, lastScatterPoint = null, lastHighlightedSample = null;
    clearOverlays();
    updateAnnotationLink();

    // clear display info
    var noInfo = '<span style="font-style:italic;">No information available.</span>';
    $("#groupInfo").html(noInfo);
    $("#qcInfo tbody").html("<tr><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td></tr>");
    $("#labkeySubject").html(noInfo);
    $("#labkeyClinical").html(noInfo);
    $("#tab-labresults").html(noInfo+'<table id="labkeyLabResults" class="centered-table"><thead><tr></tr></thead><tbody></tbody></table>');
    $("#tab-flow").html(noInfo+'<table id="labkeyFlow" class="zebra-striped"><thead><tr></tr></thead><tbody></tbody></table>');
    $("#labkeyAncillaryData").html(noInfo);
    $.ajax({
      url: getBase()+"/geneBrowser/getSignalTable",
      data: { sampleSetId: sampleSetId },
      datatype: "json",
      async: false,
      success: function(json) {
        signalDataTable = json.signalDataTable;
      }
    });
    $.getJSON(getBase()+"/geneBrowser/checkTg2Labkey", { sampleSetId:sampleSetId }, function(json) {
      hasTg2 = json.hasTg2;
      hasLabkey = json.hasLabkey;
      var hasCustomTabs = json.hasTabs;

      var qcTab = $("li.qctab");
      if (hasTg2) {
        qcTab.show();
      } else {
        if (qcTab.hasClass("active"))
        {
          qcTab.removeClass("active");
          $("li#geneInfoTab a").trigger("click");
        }
        qcTab.hide();
      }

      if (hasLabkey) {
        $("li.clinicaltabs").show();
        $("#pid-label").show();
        $("#sid-label").hide();
      } else {
        var activeClinical = $("li.clinicaltabs.active");
        if (activeClinical)
        {
          activeClinical.removeClass("active");
          $("li#geneInfoTab a").trigger("click");
        }
        $("li.clinicaltabs").hide();
        $("#sid-label").show();
        $("#pid-label").hide();
      }

      $(".custom-tab").detach();
      var trackingTab = $("li#tracking-tab");
      var trackingTabContent = $("div#tab-group");
      if (hasCustomTabs) {
        trackingTab.hide();
        if (trackingTabContent.hasClass("active")) {
          trackingTabContent.removeClass("active");
        }
        var tabHtml = "", tabContentHtml = "";
        $.each(json.tabs, function(i,tab) {
          tabHtml += '<li title="' + tab.name + '" class="custom-tab"><a href="#tab-' + tab.key + '">' + tab.name + '</a></li>';
          tabContentHtml += '<div id="tab-' + tab.key + '" class="tab custom-tab">';
          tabContentHtml += '<div id="' + tab.key + 'Info">';
          tabContentHtml += noInfo+'</div></div>';
        });
        $("ul.pills").append(tabHtml);
        $("div.info-scrollPanel").append(tabContentHtml);

        if (trackingTab.hasClass("active")) {
          $("li#geneInfoTab a").trigger("click");
        }
      }
      else
      {
        trackingTab.show();
        if (trackingTab.hasClass("active")) {
          if (!trackingTabContent.hasClass("active")) {
            trackingTabContent.addClass("active");
          }
        }
      }


    });
    // update study tab
    $.getJSON(getBase()+"/geneBrowser/getStudyInfo", { sampleSetId:sampleSetId }, function(json) {
      var html = "";
      $.each(json, function(label,value) {
        if (label === "Title")
        {
          $("h4#sampleSetTitle").html(value);
        }
        else
        {
          html += "<tr><td><strong>"+label+"</strong></td><td><p>"+value+"</p></td></tr>";
        }
      });
      $("div#tab-study table tbody").html(html);
    });
    
    // update HTML and Raphael Axes
    $.getJSON(getBase()+"/geneBrowser/scaleInfo", {sampleSetId: sampleSetId}, function(json) {
    	$.each(json, function(label, value) {
    		if (label === "htmlScale")
    		{
    			$("#signalType").html(value);
    			$("#currentSignal").html('--');
    		}
    		else if (label === "raphaelScale")
    		{
				$("#yTitle").val(value);
    		}
    	});
    });
    $.getJSON(getBase()+"/geneBrowser/getGroupSets", { sampleSetId:sampleSetId }, function(json) {
      currentGroupSetID = json.defaultGroupSet;
      var option = "";
      $.each(json.groupSets, function(i,gSet) {
        option += "<li><label class='nowidth'><input type='radio' id='groupSet' name='groupSet' onclick='changeGroupSet(this);' value='"+gSet.id+"'";
        if (currentGroupSetID === gSet.id) {
          option += " checked='checked'";
        }
        option += "> <span>"+gSet.name+"</span></label></li>";
      });
      currentGroupSetSize = json.groupSets.length;
      $("#groupset-options-options").html(option);
      refreshChart();
      showGroupSet();
      updateOverlayOptions();
    });
  };

  var changeGroupSet = function(inputField)
  {
  	currentGroupSetID = $(inputField).val();
    drawChartLegend = true;
    refreshChart();
  };

  var showHistogramSampleInfo = function(event)
  {
    $('#currentSignal').html(this.y.toFixed(2));
    updateInfoPanel(this.sid);
  };

  var updateInfoPanel = function(sid)
  {
    var width = $("div.tab.active").width();
    var numCols = Math.floor(width / 350);
    var args =
    {
      sid: sid,
      dsid: currentSampleSetID
    };
    if (hasTg2)
    {
      $.getJSON(getBase()+"/geneBrowser/qcSampleInfo", args, function(json) {
        var qcHtml = "";
        if (json.qcInfo.background)
        {
          var qcData = json.qcInfo;
          qcHtml = "<td>"+qcData.background+"</td>";
          qcHtml += "<td>"+qcData.biotin+"</td>";
          qcHtml += "<td>"+qcData.genes001+"</td>";
          qcHtml += "<td>"+qcData.genes005+"</td>";
          qcHtml += "<td>"+qcData.gp95+"</td>";
          qcHtml += "<td>"+qcData.housekeeping+"</td>";
          qcHtml += "<td>"+qcData.noise+"</td>";
        }
        else
        {
          qcHtml = "<td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td>";
        }
        $("table#qcInfo tbody tr").html(qcHtml);

        if (json.sampleInfo)
        {
          var sampleInfo = json.sampleInfo;
          $.each(sampleInfo, function(id,v) {
            if (!v) {
              v = "--";
            }
            $("span[id='tg2."+id+"']").html(v);
          });
        }
      });
    }
    if (hasLabkey)
    {
      $("li.clinicaltabs").show();
      $.getJSON(getBase()+"/geneBrowser/labkeySampleInfo", args, function(json) {
        if (json) {
          $.each(json, function(tab,info) {
            var labels = info.labels;
            var currentPid = null;
            var html = "";
            if (tab === "labkeyLabResults" || tab === "labkeyFlow")
            {
              var tabId = tab === "labkeyLabResults" ? "#tab-labresults" : "#tab-flow";
              var headerHtml = "";
              var headerArray = new Array();
              $.each(labels, function(labelKey,header) {
                if (labelKey !== "participant_id" && labelKey !== "visit" && labelKey !== "_id")
                {
                  headerHtml += "<th>"+header.displayName+"</th>";
                  headerArray[header.order] = "--";
                }
              });
              $.each(info.data, function(i,row) {
                var rowHtml = "<tr><td>";
                var rowArray = headerArray;
                $.each(row, function(field,value) {
                  if (field !== "participant_id" && field !== "visit" && field !== "_id") {
                    rowArray[labels[field].order] = value;
                  } else if (field === "participant_id" && !currentPid) {
                    currentPid = value;
                  }
                });
                rowHtml += rowArray.join("</td><td>");
                rowHtml += "</td></tr>";
                html += rowHtml;
              });
              $(tabId+" #lab-results-notfound").detach();
              $("table#"+tab).parent().find("span").detach();
              $("table#"+tab+" thead tr").html(headerHtml);
              $("table#"+tab+" tbody").html(html);
              if (html === "")
              {
                $(tabId).append("<p id='lab-results-notfound'>Not found in database.</p>");
              }
            }
            else if (tab === "labkeyAncillaryData")
            {
              var pid = info.data[0].participant_id;
              var pInfoLink = "https://www.itntrialshare.org/study/Studies/ITN029ST/Study%20Data/participant.view?participantId="+pid;
              var srLink = "https://www.itntrialshare.org/study-samples/Studies/ITN029ST/Study%20Data/typeParticipantReport.view?participantGroupFilter=&statusFilterName=ALL&baseCustomViewName=&typeLevel=Derivative&participantId="+pid+"&viewVialCount=on&_print=&excelExport=";
              html += "<p><a href='"+pInfoLink+"' target='_blank'>View Participant Information in TrialShare</a></p>";
              html += "<p><a href='"+srLink+"' target='_blank'>View Specimen Report in TrialShare</a></p>";
              $("div#"+tab).html(html);

              if (!currentPid) {
                currentPid = pid;
              }
            }
            else
            {
              var i = 0;
              var numRows = Math.ceil(info.numFields / numCols);
              var html = "<div class='row'><div class='span6'><dl>";
              $.each(info.data[0], function(field,value) {
                if (field !== "participant_id" && field !== "_id")
                {
                  if (value && value !== "null")
                  {
                    if (i < numRows) {
                      i++;
                    } else {
                      html += "</dl></div>";
                      html += "<div class='span6'><dl>";
                      i = 1;
                    }
                    html += "<dt>"+labels[field].displayName+"</dt><dd>"+value+"</dd>";
                  }
                }
                else if (field === "participant_id" && !currentPid)
                {
                  currentPid = value;
                }
              });
              html += "</dl></div></div>";
              $("div#"+tab).html(html);
            }
            $("span#currentPid").html(currentPid);
          });
        }
      });
    }
    $.getJSON(getBase()+"/geneBrowser/sampleGroupInfo", args, function(json) {
      if (json && json !== "")
      {
        $.each(json, function(tab,data) {
          var labels = data.labels;
          var numRows = Math.ceil(data.numFields / numCols);
          var html = "<div class='row'><div class='span6'><dl>";
          var row = 0;
          $.each(data.data, function(field,value) {
            if (labels[field] && value && value !== "null")
            {
              if (row < numRows) {
                row++;
              } else {
                html += "</dl></div>";
                html += "<div class='span6'><dl>";
                row = 1;
              }
              html += "<dt>"+labels[field].displayName+"</dt><dd>"+value+"</dd>";
            }
          });
          html += "</dl></div></div>";
          $("div[id='" + tab + "Info']").html(html);
        });
      }
      else
      {
        $("div#groupInfo").html('<span style="font-style:italic;">No information available.</span>');
      }
    });
  };

  var updateOverlayOptions = function() {
    var args = { sampleSetId: currentSampleSetID };
    $.ajax({
      url: getBase()+"/geneBrowser/overlayOptions",
      datatype: "json",
      data: args,
      async: false,
      cache: false,
      success: function(json) {
        if (json)
        {
          hasOverlays = true;
          var html = "";
          if (json.categorical)
          {
            html += "<div style=\"font-weight:bold;font-size:14px;\">Categorical</div>";
            html += "<form id=\"categorical-overlays\" name=\"categorical-overlays\" method=\"post\">";
            $.each(json.categorical, function(i,overlay) {
              var overlayKey = overlay.collection+"_"+overlay.key;
              var checked = typeof categoryOrder[overlayKey] !== "undefined" ? " checked=\"checked\"" : "";
              html += "<li><label class=\"nowidth\"><input type=\"checkbox\" id=\""+overlayKey+"\" onclick=\"drawOverlay('categorical',this)\" name=\""+overlayKey+"\""+checked+"> <span>"+overlay.displayName+"</span></label></li>";
            });
            html += "</form>";
          }
          if (json.numerical)
          {
            html += "<div style=\"font-weight:bold;font-size:14px;\">Continuous</div>";
            html += "<form id=\"numerical-overlays\" name=\"categorical-overlays\" method=\"post\">";
            html += "<li><label class=\"nowidth\"><input type=\"radio\" id=\"lineOverlay\" onclick=\"drawOverlay('numerical',this)\" value=\"none\" checked=\"checked\" name=\"lineOverlay\"> <span>None</span></label></li>";
            $.each(json.numerical, function(i,overlay) {
              var overlayKey = overlay.collection+"_"+overlay.key;
              var checked = lastNumericalOverlay == overlayKey ? " checked=\"checked\"" : "";
              html += "<li><label class=\"nowidth\"><input type=\"radio\" id=\"lineOverlay\" onclick=\"drawOverlay('numerical',this)\" value=\""+overlayKey+"\" name=\"lineOverlay\""+checked+"> <span>"+overlay.displayName+"</span></label></li>";
            });
            html += "</form>";
          }
          $("span#overlay-options").next("ul.plot-options-options").html(html);
          $("span#overlay-options").removeClass("disabled");
        }
        else
        {
          hasOverlays = false;
        }
      }
    });
  };

  var updateGeneNotes = function()
  {
    var reference = "GENESYMBOL:".concat(currentGeneSymbol);
    var notesArgs = { reference:reference, returnComments:false };
    $.getJSON(getBase()+"/notes/getNotes", notesArgs, function(json) {
      var notesHtml = "--";
      if (json) {
        notesHtml = "";
        $.each(json, function(i,note) {
          notesHtml += "<div class='pnote'>" + note.note + " - <span class='note-user'>" + note.user + "</span></div>";
        });
      }
      $("span#gene-usernotes").html(notesHtml);
    });
  };

  var updatePubmedLinks = function()
  {
    setInfoLoading();
    setInfoBrowserTitle("Pubmed Abstracts");
    $.getJSON(getBase()+"/geneInfo/queryGeneLinks", { geneID: currentGeneID, limit:25 }, function(json) {
      if (json != null && json.numArticles) {
        var infoHtml = "<p>Showing up to 25 articles related to the gene, <strong>" + currentGeneSymbol + "</strong></p>";
        infoHtml += "<table class='zebra-striped'>";
        infoHtml += "<thead><tr><th>Year</th><th>Title</th></tr></thead><tbody>";
        $.each(json.articles, function(i,article) {
          infoHtml += "<tr><td>";
          if (article.pubYear) {
            infoHtml += article.pubYear;
          }
          infoHtml += "</td><td><a href='http://www.ncbi.nlm.nih.gov/pubmed/" + article.pmid + "' target='_blank'>" + article.title + "</a></td></tr>";
        });
        infoHtml += "</tbody></table>";
        if (json.numArticles > 25) {
          infoHtml += "<p><a href='http://www.ncbi.nlm.nih.gov/gene?Db=pubmed&DbFrom=gene&Cmd=Link&LinkName=gene_pubmed&LinkReadableName=PubMed&IdsFromResult=" + currentGeneID + "' target='_blank'>More PubMed articles...</a></p>";
        }
        setInfoBrowserContent(infoHtml);
      }
    });
  };

  var submitQuery = function() {
    $("#topSearch").blur();
    var term = $("#topSearch").val();
    var args = { term:term };
    $.getJSON(getBase()+"/geneBrowser/sampleSetQuery", args, function(data) {
      var html = "";
      $.each(data, function(i,ss) {
        html += '<div id="sampleSetBox_'+ss.id+'" class="geneBox';
        if (i === 0) {
          html += ' active';
          currentSampleSetID = ss.id;
        }
        html += '" style="height:50px;overflow:hidden;" title="'+ss.text+'" onclick="selectSampleSet(\''+ss.id+'\',this);">';
        html += '<div style="font-size:13px;">'+ss.text+'</div></div>';
      });
      $("div#samplesets").html(html);
      $("div[id='sampleSetBox_" + currentSampleSetID + "']").trigger("click");
    });
  };

  $(document).ready(function() {
      jqxhr = $.getJSON(getBase()+"/geneInfo/queryGeneInfo", { geneID: currentGeneID, geneSymbol: currentGeneSymbol, ssid: currentSampleSetID }, function(json) {
        if (json.geneId == currentGeneID) {
          var data = json.entrez;
          $.each(data, function(key,value) {
//
//            else
//            {
              var span = $("span[id='gene."+key+"']");
              if (span) {
                span.text(value);
              }
//            }
          });
          if (json.pubmed)
          {
            setInfoBrowserTitle("Pubmed Abstracts");

            var pubmedData = json.pubmed;
            var numArticles = pubmedData.numArticles;
            if (numArticles != "0") {
              var pubmedLink = "<span class='pubmed-link' onclick='javascript:showPubmedLinks();'>" + numArticles + " Pubmed articles</span>";
              $("span#gene-pubmedlinks").html("There are " + pubmedLink + " linked to this gene.");

              var infoHtml = "<p>Showing up to 25 articles related to the gene, <strong>" + currentGeneSymbol + "</strong></p>";
              infoHtml += "<table class='zebra-striped'>";
              infoHtml += "<thead><tr><th>Year</th><th>Title</th></tr></thead><tbody>";
              $.each(pubmedData.articles, function(i,article) {
                infoHtml += "<tr><td>";
                if (article.pubYear) {
                  infoHtml += article.pubYear;
                }
                infoHtml += "</td><td><a href='http://www.ncbi.nlm.nih.gov/pubmed/" + article.pmid + "' target='_blank'>" + article.title + "</a></td></tr>";
              });
              infoHtml += "</tbody></table>";
              if (numArticles > 25) {
                infoHtml += "<p><a href='http://www.ncbi.nlm.nih.gov/gene?Db=pubmed&DbFrom=gene&Cmd=Link&LinkName=gene_pubmed&LinkReadableName=PubMed&IdsFromResult=" + currentGeneID + "' target='_blank'>More PubMed articles...</a></p>";
              }
              setInfoBrowserContent(infoHtml);
            } else {
              $("span#gene-pubmedlinks").html("There are no Pubmed articles linked to this gene.");
              setInfoBrowserContent("<p>There are no articles available</p>");
            }
          }
        }
        jqxhr = null;

    });

    //setTimeout(function() { updatePubmedLinks(); }, 0);
    setTimeout(function() { updateGeneNotes(); }, 0);

    $('#errorNotFound').hide();
	showGroupSet();
    // project list height adjustment
    var windowHeight = $(window).height() - 80;
    $("#projectList").height(windowHeight - 30);

    $("#"+activeChartType).addClass("active");

    // get the overlay options
    updateOverlayOptions();
    // draw the histogram
    refreshChart();

    $(".alert-message a.close").click(function() {
      $(this).closest(".alert-message").hide();
      clearSearch();
    });

    if (!hasTg2)
    {
      $("li.qctab").hide();
    }
    if (!hasLabkey)
    {
      $("li.clinicaltabs").hide();
      $("#pid-label").hide();
    }
    else
    {
      $("#sid-label").hide();
    }

    var resizing;
    $(window).resize(function(){
      clearTimeout(resizing);
      resizing = setTimeout(function() { resizeChart(); }, 500);
    });

  });
</script>

</g:else>
</div>
<g:render template="/common/bugReporter" model="[tool:'XProject']"/>
<g:render template="/common/addNote"/>
<g:render template="/common/infoBrowser"/>
</body>
</html>
