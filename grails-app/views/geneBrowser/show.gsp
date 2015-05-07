<%@ page import="org.sagres.sampleSet.DatasetGroupSet; org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils; org.sagres.geneList.GeneListCategory; org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap; org.sagres.sampleSet.SampleSet; org.codehaus.groovy.grails.plugins.converters.codecs.JSONCodec; org.codehaus.groovy.grails.web.json.JSONWriter; grails.converters.JSON; org.sagres.sampleSet.component.OverviewComponent" %>
<html>
<head>
  <g:set var="entityName" value="${message(code: 'geneInfo.label', default: 'GXB: Gene Info')}"/>
  <meta name="layout" content="gxbmain"/>
  <title>GXB: ${sampleSet.name}</title>
  
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
    toggleRankListScore(true);  // force an expose to start
  });
  </g:javascript>

  <!-- bootstrap js is in twice; once in this file, once is gxbmain. it won't load right from gxbmain-->
  <g:javascript src="bootstrap-dropdown.js"/>
  <g:javascript src="bootstrap-modal.js"/>
</head>

<body itemscope itemtype="http://schema.org/Article">

<div class="topbar">
  <div class="topbar-inner fill">
    %{--<div class="topbar-inner bri-fill">--}%
    <div class="gxb-container">
      <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

      <h3><g:link controller="geneBrowser" action="list"><strong> GXB</strong></g:link></h3>

      <form action="" onsubmit="submitQuery('top'); return false;">
        <input id="topSearch" type="text" placeholder="Search: Gene Symbols" />
        <a href="#" class="btn small primary topbar-clear" onclick="clearSearch();">Clear Search</a>
      </form>
      <g:javascript>
        var autoReq = null;
        var currentSearchTerm = null;
        $("#topSearch").autocomplete({
          source: function(request, response) {
            request.sampleSetID = ${sampleSet.id};
            request.rankList = currentRankList,
            request.geneListId = $('#geneListSelect').val(),
            request.module = "${params.module}",
            request.analysis = "${params.analysisId}",
            request.modVersionName = "${params.modVersionName}",
            request.signalDataTable = signalDataTable,
            request.limit = 10;
            currentSearchTerm = request.term;
            if (autoReq !== null) {
              autoReq.abort();
            }
            autoReq = $.getJSON(getBase()+"/geneBrowser/queryAllGenes", request, function(json) {
              if (json && json.term === currentSearchTerm) {
                var items = [];
                $.each(json.gl, function(key, val) {
                  items.push({ list:val.list, symbol:val.symbol });
                });
                response(items);
              } else {
                response(null);
              }
              autoReq = null;
            });
            %{--autoReq = $.getJSON(getBase()+"/geneBrowser/getGeneList", request, function(data) {--}%
              %{--if (data.term === currentSearchTerm)--}%
              %{--{--}%
                %{--var items = [];--}%
                %{--$.each(data.gl, function(key, val) {--}%
                  %{--items.push({ list: "Gene list", symbol: val.symbol });--}%
                %{--});--}%
                %{--response(items);--}%
                %{--autoReq = null;--}%
              %{--}--}%
            %{--});--}%
          },
          select: function(event, ui) {
            $("#topSearch").val(ui.item.symbol);
            submitQuery("top");
            return false;
          },
          focus: function(event, ui) {
            $("#topSearch").val(ui.item.symbol);
            return false;
          },
          delay     : 10
        }).data("autocomplete")._renderItem = function( ul, item ) {
          return $("<li></li>")
				  .data("item.autocomplete", item)
				  .append("<a>" + item.symbol + " <span class='list-source'>in " + item.list + "</span></a>")
				  .appendTo(ul);
		    };
      </g:javascript>
      <ul class="nav secondary-nav">
      	<g:if test="${grailsApplication.config.googleplus.on}">
      	   <li><g:link onclick="googlePlusLink(); return false;" class="gplus noshade" title="Post this link to your google+ circles"></g:link></li>
      	</g:if>
        <g:if test="${grailsApplication.config.send.email.on}">
          <li><g:link onclick="generateLink(false,true); return false;" class="email-link noshade" title="Email Link"></g:link></li>
        </g:if>
        <sec:ifLoggedIn>
          <li><g:link onclick="javascript:newGeneNote();return false;" class="note noshade" title="Add Note"></g:link></li>
        </sec:ifLoggedIn>
		        <g:if test="${labKey != null}">
			        <li class="dropdown" data-dropdown="dropdown">
				        <a href="#" class="dropdown-toggle">TrialShare</a>
				        <ul class="dropdown-menu itn-dropdown">
					        <li><a href="https://www.itntrialshare.org/project/Studies/ITN029ST/Study%20Data/begin.view?" target="_gxbLabKey">TrialShare Main</a></li>
					        <li><a href="https://www.itntrialshare.org/study-redesign/Studies/ITN029ST/Study%20Data/page.view?pageName=DATA_ANALYSIS" target="_gxbLabKey">TrialShare Data Analysis</a></li>
					        <li class="divider"></li>
					        <li><a href="https://www.itntrialshare.org/reports/Studies/ITN029ST/Study%20Data/runReport.view?reportId=db%3A51&redirectUrl=%2Fstudy%2FStudies%2FITN029ST%2FStudy%2520Data%2FbrowseData.view%3F_dc%3D1320427340466%26pageId%3D68065C04-9480-102E-A71C-16253D09614C%26index%3D4%26page%3D1%26start%3D0%26limit%3D100%26group%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D%26sort%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D" target="_gxbLabKey">Clustering Heatmap: Top 205 Genes at Baseline</a></li>
					        <li><a href="https://www.itntrialshare.org/reports/Studies/ITN029ST/Study%20Data/runReport.view?reportId=db%3A62&redirectUrl=%2Fstudy%2FStudies%2FITN029ST%2FStudy%2520Data%2FbrowseData.view%3F_dc%3D1320427487090%26pageId%3D68065C04-9480-102E-A71C-16253D09614C%26index%3D4%26page%3D1%26start%3D0%26limit%3D100%26group%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D%26sort%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D" target="_gxbLabKey">Clustering Heatmap: Top 31 Genes at Baseline</a></li>
					        <li><a href="https://www.itntrialshare.org/reports/Studies/ITN029ST/Study%20Data/runReport.view?reportId=db%3A101&redirectUrl=%2Fstudy%2FStudies%2FITN029ST%2FStudy%2520Data%2FbrowseData.view%3F_dc%3D1320427711746%26pageId%3D68065C04-9480-102E-A71C-16253D09614C%26index%3D4%26page%3D1%26start%3D0%26limit%3D100%26group%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D%26sort%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D" target="_gxbLabKey">Feng Alberto Newell Biomarker Comparison</a></li>
%{--
					        <li><a href="https://www.itntrialshare.org/visualization/Studies/ITN029ST/Study%20Data/timeChartWizard.view?edit=false&queryName=Master%20Result&schemaName=study&name=Gene%20Expression%20Longitudinal" target="_gxbLabKey">Gene Expr Longitudinal</a></li>
					        <li><a href="https://www.itntrialshare.org/reports/Studies/ITN029ST/Study%20Data/runReport.view?reportId=db%3A67&redirectUrl=%2Fstudy%2FStudies%2FITN029ST%2FStudy%2520Data%2FbrowseData.view%3F_dc%3D1320427848488%26pageId%3D68065C04-9480-102E-A71C-16253D09614C%26index%3D4%26page%3D1%26start%3D0%26limit%3D100%26group%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D%26sort%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D" target="_gxbLabKey">Line Plot</a></li>
--}%
					        <li><a href="https://www.itntrialshare.org/reports/Studies/ITN029ST/Study%20Data/runReport.view?reportId=db%3A100&redirectUrl=%2Fstudy%2FStudies%2FITN029ST%2FStudy%2520Data%2FbrowseData.view%3F_dc%3D1320427864594%26pageId%3D68065C04-9480-102E-A71C-16253D09614C%26index%3D4%26page%3D1%26start%3D0%26limit%3D100%26group%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D%26sort%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D" target="_gxbLabKey">2011-07-08 Analysis Report</a></li>
					        <li><a target="_gxbLabKey" href="https://www.itntrialshare.org/reports/Studies/ITN029ST/Study%20Data/runReport.view?reportId=db%3A146&redirectUrl=%2Fstudy%2FStudies%2FITN029ST%2FStudy%2520Data%2FbrowseData.view%3F_dc%3D1327363479520%26pageId%3Dstudy.DATA_ANALYSIS%26index%3D1%26page%3D1%26start%3D0%26limit%3D100%26group%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D%26sort%3D%255B%257B%2522property%2522%253A%2522category%2522%252C%2522direction%2522%253A%2522ASC%2522%257D%255D">Heatmap: Top 20 Genes at Baseline</a></li>
					        <li class="divider"></li>
					        <li><a target="_gxbLabKey" href="https://www.itntrialshare.org/study-reports/Studies/ITN029ST/Study%20Data/runRReport.view?Dataset.reportId=db%3A28">Flow: B Cell Immunophenotyping at Baseline</a></li>
					        <li><a target="_gxbLabKey" href="https://www.itntrialshare.org/visualization/Studies/ITN029ST/Study%20Data/timeChartWizard.view?edit=false&queryName=Flow%20Analysis&schemaName=study&name=Flow%20populations%20of%20interest">Flow Populations of Interest</a></li>
					        <li><a href=""></a></li>
				        </ul>
			        </li>
		        </g:if>


        <li class="dropdown" data-dropdown="dropdown">
          <a href="#" class="dropdown-toggle">Tools</a>
          <ul class="dropdown-menu skin-dropdown">
            %{--<li><a href="#" id="advancedQuery">Gene List Options</a></li>--}%
            %{--<li><g:link controller="geneList" action="create" target="_blank"><span id="geneListImporter">Import Gene List</span></g:link></li>--}%
               <g:if test="${moduleLink != null}">
          <li><a href="${moduleLink}" target="_gxb_modules">Modules</a></li>

        </g:if>
            <li><g:link controller="sampleSet" action="show" id="${sampleSet.id}" target="_annotationWindow">Annotation</g:link></li>
              <li class="divider"></li>
            <li><g:link controller="geneList" action="list" target="_blank">Gene Lists</g:link></li>
            <li><g:link name="crossProjectLink" action="crossProject" target="_blank"><span id="viewCrossProject">Cross Project View</span></g:link> </li>
            <li class="divider"></li>
            <li><g:link onclick="toggleShowProbeID(); return false;"><span id="showProbeIDMenu">Show Probe ID</span></g:link></li>
            <li><g:link onclick="toggleRankListScore(false); return false;"><span id="showRankListScoreMenu">Show Rank List Score</span></g:link></li>
            <li><a href="#" id="rankListOptions" onclick="showRankListPanel();">Change Rank List</a></li>
            <li class="divider"></li>
            %{--<g:if test="${grailsApplication.config.send.email.on}">--}%
              %{--<li><g:link onclick="generateLink(true); return false;"><span id="generateLinkMenu">Email Link</span></g:link></li>--}%
            %{--</g:if>--}%
            <li><g:link onclick="generateLink(false); return false;"><span id="generateLinkMenu">Copy Link</span></g:link></li>
            <li class="divider"></li>
            <li><a href="#" id="chartOptions">Chart Options</a></li>
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
                <g:link controller="secUser" action="forgotPassword" class="forgot-password">Forgot password</g:link>
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

  <div id="geneList" class="geneList">
  	<img id="alertMsg" src="${resource(dir:'images/icons', file:'loading_filter.gif')}" alt="Updating..." style="margin:20px auto 0 auto; display:block;">
    <%-- <div id="alertMsg" class="alert-message block-message info"><strong>Updating...</strong></div> --%>
    <div id="errorNotFound" class="alert-message block-message error" style="display: none;"><a class="close" href="#" title="Clear the search">&times;</a><strong><span id="notFoundMsg">No Records Found</span></strong></div>
    <div id="resultList">
    </div>
  </div>
  <sec:ifLoggedIn>
  	<img src="../../images/icons/loading_ajax.gif" id="ajaxLoading" alt="loading..." style="margin: 5px 5px 5px 5px;">
  </sec:ifLoggedIn>
</div>
<div class="gxb-content">

<div class="page-header">
  <h4 itemprop="name">${sampleSet.name}</h4>
  <img itemprop="image" id="googleImage" style="display:none" src="">
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
    <div class="span4">Gene Symbol: <span id="currentGeneSymbol">--</span></div>
    <g:if test="${sampleSet.defaultSignalDisplayType?.id == 2}">
    	<div class="span3"><span id="signalType">Log<sub>2</sub> Signal:</span> <span id="currentSignal">--</span></div>
    </g:if>
	<g:elseif test="${sampleSet.defaultSignalDisplayType?.id == 6}">
    	<div class="span3"><span id="signalType">Fold Change:</span> <span id="currentSignal">--</span></div>
    </g:elseif>
    <g:else>
       	<div class="span3"><span id="signalType">Signal:</span> <span id="currentSignal">--</span></div>
    </g:else>
    <g:if test="${hasLabkeyData}">
      <div class="span4">Participant ID: <span id="currentPid">--</span></div>
    </g:if>
    <g:else>
      <div class="span4">Sample ID: <span id="currentSid">--</span></div>
    </g:else>
  </div>
</div>

<div id="tab-content">
  <ul class="pills">
    <li class="active" title="Gene information"><a href="#tab-gene" style="line-height:26px;">Gene</a></li>
    <li title="Study information"><a href="#tab-study">Study</a></li>
    <g:if test="${isTg2}">
      <li title="Quality control information"><a href="#tab-qc">QC</a></li>
    </g:if>
    <g:if test="${hasLabkeyData}">
      <li title="Subject / patient information" class="clinicaltabs"><a href="#tab-subject">Subject</a></li>
      <li title="Clinical data" class="clinicaltabs"><a href="#tab-clinical">Clinical</a></li>
      <li title="Lab results" class="clinicaltabs"><a href="#tab-labresults">Lab Results</a></li>
      <li title="Flow data" class="clinicaltabs"><a href="#tab-flow">Flow Data</a></li>
      <li title="Ancillary data" class="clinicaltabs"><a href="#tab-ancillary">Ancillary</a></li>
    </g:if>
    <g:if test="${hasTabs}">
      <g:each in="${tabs}" var="t">
        <li title="${t.name}"><a href="#tab-${t.name.encodeAsKeyCase()}">${t.name}</a></li>
      </g:each>
    </g:if>
    <g:else>
      <li title="Sample information"><a href="#tab-group">Sample</a></li>
    </g:else>
    <li title="Download files"><a href="#tab-downloads">Downloads</a></li>
  </ul>
  <g:javascript>
    $(document).ready(function() {
      $(".pills li a").click(function(e) {
        $(".pills li.active").removeClass("active");
        $(this).closest("li").addClass("active");
        var tab = $(this).attr("href").substring(1);
        $("div.tab.active").removeClass("active");
        $("div[id=\""+tab+"\"]").addClass("active");
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
              <span id="gene.ncbi_link"><a href="#" target="_blank"><span class="icon_ncbi"></span></a></span>
              <span id="gene.wolfram_link"><a href="#" target="_blank"><span class="icon_wa"></span></a></span>
              <span id="gene.wiki_link"><a href="#" target="_blank"><span class="icon_wikipedia"></span></a></span>
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
      
      <!-- start Study information -->
      <div id="tab-study" class="tab">
        <table class="tab-table">
          <tbody>
          <tr>
            <td><strong>Description</strong></td>
            <td>${sampleSet.description.replaceAll(/<\/?(?i:td)(.|\n)*?>/,"").decodeHTML()}</td>
          </tr>
          <!-- description is separate from the overview components, this is where we put GEO and PubMedLinks -->
            <tr id="pubmedData" style="display: none;"></tr>
            <tr id="geoData" style="display: none;"></tr>
<%--
          <g:if test="${sampleSetLinkComponents?.pubmedData}">
            <tr>
          	  <td><strong>PubMed Reference</strong></td>
	          <td><a href="${sampleSetLinkComponents.pubmedUrl}" target="_blank">${sampleSetLinkComponents.pubmedData.title}</a></td>
	        </tr>
          </g:if>
--%>
          <g:each in="${sampleSetOverviewComponents}" var="ssComponent">
            <g:set var="initValue" value="${fieldValue(bean:sampleSet.sampleSetAnnotation, field:OverviewComponent.get(ssComponent.componentId).annotationName)}"/>
            <g:if test="${initValue && !initValue.isAllWhitespace()}">
              <tr>
                <td><strong>${OverviewComponent.get(ssComponent.componentId).name}</strong></td>
                <td>${initValue.decodeHTML()}</td>
              </tr>
            </g:if>
          </g:each>
          <g:if test="${sampleSetPlatformInfo?.platformOption}">
	        <tr>
          	  <td><strong>Platform</strong></td>
          	  <td>${sampleSetPlatformInfo?.platformOption}</td>
          	</tr>
          </g:if>
          </tbody>
        </table>
      </div>
	 <!-- start Sample information tabs (could be more than one)-->
      <g:if test="${hasTabs}">
        <g:each in="${tabs}" var="t">
          <div id="tab-${t.name.encodeAsKeyCase()}" class="tab">
            <div id="${t.name.encodeAsKeyCase()}Info">
              <span style="font-style:italic;">No information available.</span>
            </div>
          </div>
        </g:each>
      </g:if>
      <g:else>
      <div id="tab-group" class="tab">
        <div id="groupInfo">
          <span style="font-style:italic;">No information available.</span>
        </div>
      </div>
      </g:else>
	  <!-- Start TG2 information tabs -->
      <g:if test="${isTg2}">
        <div id="tab-qc" class="tab">
          <table id="qcInfo" class="centered-table">
            <thead><tr><th>Background</th><th>Biotin</th><th>Genes001</th><th>Genes005</th><th>Gp95</th><th>Housekeeping</th><th>Noise</th></tr></thead>
            <tbody><tr><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td><td>--</td></tr></tbody>
          </table>
          <div id="tg2Info">
            <div class="row"><div class="span2"><strong>Sample Name</strong></div><div class="span4"><span id="tg2.sample_name">--</span></div><div class="span2" style="text-align:right;"><strong>Cell Population</strong></div><div class="span4"><span id="tg2.cell_population">--</span></div></div>
            <div class="row"><div class="span2"><strong>Donor ID</strong></div><div class="span4"><span id="tg2.donor_id">--</span></div><div class="span2" style="text-align:right;"><strong>Tissue Type</strong></div><div class="span4"><span id="tg2.tissue_type">--</span></div></div>
            <div class="row"><div class="span2"><strong>Collection Date</strong></div><div class="span4"><span id="tg2.collection_date">--</span></div><div class="span2" style="text-align:right;"><strong>Notes</strong></div><div class="span4"><span id="tg2.tg2_notes">--</span></div></div>
          </div>
        </div>
      </g:if>

      <g:if test="${hasLabkeyData}">
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
      </g:if>

      <div id="tab-downloads" class="tab">
        <div class="row">
        <div class="span8">
          <h5>Default files:</h5>
          <g:if test="${sampleSet.chipsLoaded?.filename}">
          <p><g:link controller="sampleSetFile" action="download" params="[ sampleSetId:sampleSet.id, signalFile:sampleSet.chipsLoaded.filename ]">Signal Data File</g:link></p>
          </g:if>

          <div class="row"><span class="span4 likelabel">Download Annotations for Group Set:</span> <g:select name="groupSetDownload" from="${sampleSet.groupSets}" optionKey="id" optionValue="name" onchange="downloadGroupSet(this);" noSelection="['null':'--select a group set--']"/></div>
          <div class="row"><span class="span4 likelabel">Download Annotations for Group:</span> <g:select name="groupDownload" from="${DatasetGroupSet.findById(defaultGroupSetID).groups}" optionKey="id" optionValue="name" onchange="downloadGroup(this);" noSelection="['null':'--select a group--']"/></div>
        </div>

        <div class="span8">
          <h5>Additional Files:</h5>
          <g:if test="${!grailsApplication.config.dm3.authenticate.labkey}">
          <p><em>(Uploaded through the Files tab in the Annotation Tool)</em></p>
          </g:if>

          <g:if test="${sampleSet.sampleSetFiles}">
	        <g:each in="${sampleSet.sampleSetFiles}" var="ssFile">
	        <%-- Look for filter in the controller, don't inspect tag --%>
<%--             <g:if test="${ssFile.tag?.tag != 'Chip Files'}"> --%>
              <p><g:link controller="sampleSetFile" action="download" id="${ssFile.id}">${ssFile.filename}</g:link></p>
<%--             </g:if> --%>
            </g:each>
          </g:if>
        </div>
      </div>

      </div>

      <g:javascript>
        var downloadGroupSet = function(elt) {
          var groupSetId = $(elt).val();
          if (groupSetId !== "null") {
            location.href = getBase()+"/sampleSet/exportSpreadsheet/${sampleSet.id}?groupSetId="+groupSetId;
          }
        };
        var downloadGroup = function(elt) {
          var groupId = $(elt).val();
          if (groupId !== "null") {
            location.href = getBase()+"/sampleSet/exportSpreadsheet/${sampleSet.id}?groupId="+groupId;
          }
        };
      </g:javascript>

    </div>
  </div>
</div>
<g:javascript>
  var showPubmedLinks = function() {
    showInfoBrowser();
  };
</g:javascript>
<g:render template="/common/emailLink"/>
<g:if test="${rankLists}">
  <div id="rankListPanel" class="pullout" style="width:500px; height:325px;">
    <div class="page-header">
      <h4 style="max-width:100%">Change Rank List</h4>
    </div>
    <div class="ui-icons" style="float: left;">
       <span class="ui-icon-min tiny" onclick="minMax(this,'advancedRanklistPanel');"></span>
       <span id="advTitle" style="vertical-align: top;">Advanced</span>
    </div><!--end ui icons-->
    <div id="advancedRanklistPanel" style="display:none; padding: 5px;">
      <table style="border: none;">
      <tr>
      <td>
      	<div style="vertical-align: top; text-align: left; display: block; width: 150px;">
      	<input type="radio" id="range" name="rankType" value="range"${currentRankListType == 'range' ? " checked='true'" : ""}/> Range
      	<br />      	
      	<input type="radio" id="fc" name="rankType" value="fc"${currentRankListType == 'fc' ? " checked='true'" : ""}/> Fold Change
      	<br />
      	<input type="radio" id="diff" name="rankType" value="diff"${currentRankListType == 'diff' ? " checked='true'" : ""}/> Difference
      	<br />
      	<input type="radio" id="diffFc" name="rankType" value="diffFc"${currentRankListType == 'diffFc' ? " checked='true'" : ""}/> Both
<%--
		<br />
      	<input type="radio" id="overall" name="rankType" value="overall"${currentRankListType == 'overall' ? " checked='true'" : ""}/> Overall
--%>      	
      	</div>
      </td>
      <td style="display: none;">
        <div style="vertical-align: top; text-align: left; display: block; width: 75px;">
      	<input type="radio" id="All" name="rankClass" value="All"${currentRankListClass == 'All' ? " checked='true'" : ""}/> All
      	<br />
      	<input type="radio" id="PALO" name="rankClass" value="PALO"${currentRankListClass == 'PALO' ? " checked='true'" : ""}/> PALO
      	</div>
      </td>
      <td>
      	<div style="text-align: left; width: 125px;">
      	<g:checkBox name="showProbeIds" onclick="toggleShowProbeID();"/> Show ProbeIDs
      	<br/>
      	<g:checkBox name="showScores" onclick="toggleRankListScore(false);" checked='true'/> Show Scores
      	<br/>
      	<g:checkBox name="showDescriptions" onclick="toggleSymbolDetails();" checked='true'/> Show Details
    	</div>
      </td>
      </tr>
      </table>
      </div>
      
      <table style="border: none;">
      <tr>
      <td>
      <div style="padding:10px;">
	    <label style="padding:5px;"><strong>Rank Lists</strong> </label>
      	<select id="rankListSelect" name="rankListSelect" class="large" onchange="changeRankList();">
	        <option value="-1">-select a rank list-</option>
      		<g:each in="${rankLists}" var="rl">
	      		<g:if test="${(rl.rankListType.abbrev == currentRankListType && rl.description.startsWith(currentRankListClass)) || rl.id == defaultRankList}"> 
	        		<option id="${rl.rankListType.abbrev}" value="${rl.id}"${defaultRankList == rl.id ? " selected='selected'" : ""}>${(defaultRankList == rl.id ? "+" : "") + (rl.description - ~/^\w+\s/)}</option>
	        	</g:if>
      		</g:each>
      	</select>
      	</div>

      <form action="" onsubmit="submitQuery('panel'); return false;">
        <div style="padding:10px;">
        	<label style="padding:5px;"><strong>Gene List Category</strong> </label>
        	<g:select id="geneListCategorySelect" name="geneListCategorySelect" class="large" from="${GeneListCategory.list()}" optionKey="id" optionValue="name"
          		noSelection="[null:'-select a category-']"/>
        </div>
        <div style="padding:10px;">
        	<label style="padding:5px;"><strong>Gene List</strong> </label>
        	<div id="targetGeneListSelect">
        		<!--  this is really just a placeholder until the geneListCategorySelect is used -->
        		<g:select id="geneListSelect" name="geneListSelect" class="large" from="${[]}" optionKey="id" optionValue="name" onchange="changeGeneList();"
          			noSelection="[null:'-']"/>
        	</div>
        </div>
    </form>
    </td>
    </tr>
    </table>
    <div style="padding:10px;">
      <button class="btn primary" onclick="resetRankList();">Reset Rank List</button>
      <button class="btn primary" onclick="ignoreRankList();">Ignore Rank List</button>
      <button class="btn" onclick="closeRankListPanel();">Close</button>
    </div>
  </div>
</g:if>
<g:else>
  <div id="rankListPanel" class="pullout">
    <div class="page-header">
      <h4 style="max-width:100%">Change Rank List</h4>
    </div>
    <div style="padding:10px;">
      <span style="padding-right:5px;"><strong>No Rank Lists Available</strong></span>
    </div>
    <div class="button-actions" style="clear: both;">
      <button class="btn" onclick="closeRankListPanel();">Close</button>
    </div>
  </div>
</g:else>
  <g:javascript>
    var minMax = function(elt,divId) {
      var minimize = $(elt).hasClass("ui-icon-min");
      var newHeight = $("#rankListPanel").height();
      if (minimize) {
        $("div#"+divId).show();
        newHeight += 100;
      } else {
        $("div#"+divId).hide();
        newHeight -= 100;
      }
      $("#rankListPanel").height(newHeight);
      $(elt).toggleClass("ui-icon-min");
      $(elt).toggleClass("ui-icon-max");
    };
    var showRankListPanel = function() {
      $("div#rankListPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
    };
    var closeRankListPanel = function() {
      $("div#rankListPanel").hide().css({ top:0, left:0 });
    };
  </g:javascript>

<div id="userSettings" class="tab-container pullout">
  <div class="page-header">
    <h4 style="max-width:100%">Chart Options</h4>
  </div>
  <div class="button-group">
    <a href="#tab-titles" class="chart-tab yesNo button pill active">Titles</a>
    <a href="#tab-fonts" class="chart-tab yesNo button pill">Fonts</a>
    <a href="#tab-borders" class="chart-tab yesNo button pill">Borders &amp; Axes</a>
    <a href="#tab-points" class="chart-tab yesNo button pill">Points</a>
  </div>
  <div id="tab-titles" class="option-content active">
    <form>
      <fieldset>
        <div class="clearfix">
          <label for="title">Title</label>
          <div class="input">
            <g:textField name="title" class="setting" value="${params.title}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="xTitle">X-Axis Title</label>
          <div class="input">
            <g:textField name="xTitle" class="setting" value="${params.xTitle}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="yTitle">Y-Axis Title</label>
          <div class="input">
              <g:textField name="yTitle" value="${params.yTitle ?: params.defYAxisLabel}" class="setting"/>
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
            <input id="titleFontSize" name="titleFontSize" type="number" value="18" class="small setting" value="${params['titleStyle[fontSize]']}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="axisTitleFontSize">Axis Title Font Size</label>
          <div class="input">
            <input id="axisTitleFontSize" name="axisTitleFontSize" type="number" value="16" class="small setting" value="${params['axisTitleStyle[fontSize]']}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="xFontSize">X-Axis Font Size</label>
          <div class="input">
            <input id="xFontSize" name="xFontSize" type="number" value="16" class="small setting" value="${params['xLabelStyle[fontSize]']}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="yFontSize">Y-Axis Font Size</label>
          <div class="input">
            <input id="yFontSize" name="yFontSize" type="number" value="16" class="small setting" value="${params['yLabelStyle[fontSize]']}"/>
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
              <span class="yesNo button${params.chartBorderWidth == '1' ? ' active' : ''}">Yes</span>
              <span class="yesNo button${params.chartBorderWidth == '1' ? '' : ' active'}">No</span>
            </div>
          </div>
        </div>
        <div class="clearfix">
          <label>Show Group Labels</label>
          <div class="input">
            <div id="showGroupLabels" class="button-group"> <!-- Default is Yes/True -->
              <span class="yesNo button${!params.showGroupLabels || params.showGroupLabels.toBoolean() ? ' active' : ''}">Yes</span>
              <span class="yesNo button${!params.showGroupLabels || params.showGroupLabels.toBoolean() ? '' : ' active'}">No</span>
            </div>
          </div>
        </div>
        <div class="clearfix">
        <label>Axes Visible</label>
        <div class="input">
          <div id="axesVisible" class="button-group">
            <g:set var="lineWidthsExist" value="${params.xLineWidth && params.yLineWidth}"/>
            <span class="yesNo button${params.xLineWidth == '1' && params.yLineWidth == '0' ? ' active' : ''}">X-axis</span>
            <span class="yesNo button${params.yLineWidth == '1' && params.xLineWidth == '0' ? ' active' : ''}">Y-axis</span>
            <span class="yesNo button${!lineWidthsExist || (params.xLineWidth == '1' && params.yLineWidth == '1') ? ' active' : ''}">Both</span>
            <span class="yesNo button${lineWidthsExist && params.xLineWidth == '0' && params.yLineWidth == '0' ? ' active' : ''}">None</span>
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
            <div id="showOverlayLines" class="button-group"> <!-- Default is No/False -->
              <span class="yesNo button${params.showOverlayLines && params.showOverlayLines.toBoolean() ? ' active' : ''}">Yes</span>
              <span class="yesNo button${params.showOverlayLines && params.showOverlayLines.toBoolean() ? '' : ' active'}">No</span>
            </div>
          </div>
        </div>
        <div class="clearfix">
          <label for="pointWidth">Size</label>
          <div class="input">
            <g:select from="[0:'Auto',8:8,9:9,10:10,11:11,12:12,13:13,14:14,15:15]" name="pointWidth" class="small setting" value="${params.pointWidth ?: 0}" optionKey="key" optionValue="value"/>
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
  	<div id="ranklist-options-display" class="plot-options-wrapper">
 		<span id="ranklist-options" class="btn primary plot-options disabled" title="Rank Lists" onclick="toggleRankLists(this);" style="width:80px;">
			Rank Lists
			<span class="button-arrow"></span>
		</span>
  	</div>
	<div id="groupset-options-display" class="plot-options-wrapper">
<%--	<form  action="" onsubmit="submitQuery('panel'); return false;"> --%>
		<span id="groupset-options" class="btn primary plot-options disabled" title="Group Sets" onclick="toggleGroupSets(this);" style="width:80px;">
			Group Set
			<span class="button-arrow"></span>
		</span>
		<ul id="groupset-options-options" class="plot-options-options">
		<g:each in="${groupSets}" var="gs">
	        <li><label class="nowidth"><input type="radio" id="groupSet" name="groupSet" onclick="changeGroupSet(this)" value="${gs.key}"${defaultGroupSetID == gs.key ? " checked='checked'" : ""}> <span>${gs.value}</span></label></li>
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
    	<span id="sort-options" class="btn primary plot-options disabled" title="Sorting Options" onclick="togglePlotOptions(this);" style="width:80px;">
    		Sort By
    		<span class="button-arrow"></span>
    	</span>
    	<ul id="sort-options-options" class="plot-options-options"></ul>
  	</div>

   	<div class="plot-options-wrapper">
       <span id="plot-types" class="btn primary plot-options plot-type" onclick="togglePlotOptions(this);"  title="Plot Type" >
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
    <span class="btn key-options chart-options" onclick="toggleKeyOptions(this);" title="Plot Key" >
    <span class="icon"></span> <span class="button-arrow"></span>
    </span>
    <div class="legend-dropdown chart-legend-key" style="">
      <div class="icons"><span class="icon_download-small" onclick="exportLegend('chart-legend-content');"></span></div>
      %{--<div class="icon_download" id="chartLegendExport" title="Download Legend as Image (.png)" onclick="saveLegend();return false;" style="float:none;text-align:right;"></div>--}%
      <div id="chart-legend-content"></div>
    </div>
  </div>

      <div class="plot-options-wrapper">
      <span id="overlay-options" class="btn primary plot-options disabled" title="Overlay options" onclick="togglePlotOptions(this);" style="width:100px;">
    <span class="icon"></span><span class="text">Overlays</span>
    <span class="button-arrow"></span>
     </span>
      <ul id="overlay-options-options" class="plot-options-options"></ul>
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
  var toggleRankLists = function(elt) {
//   if (!$(elt).hasClass("disabled")) {
//      var visible = $(elt).parent().find("ul.plot-options-options").is(":visible");
//      $("ul.plot-options-options").hide();
//      if (!visible) {
//        $(elt).parent().find("ul.plot-options-options").show();
//      }
      showRankListPanel();
//    }
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
		<div id="signalChart" style="overflow-x:auto;overflow-y:hidden;padding-top:10px;"></div>
		<div id="noSignal" style="display:none"><h4>No signal data found for this sample set.</h4></div>
		<img id="loading" src="${resource(dir:'images/icons', file:'loading_filter.gif')}" class="loadingIcon" alt="Updating..." style="margin:25px auto 0 auto; display:block;">
	</div>
  </div>
</div>
%{--template for node in gene list--}%
<div style="display:none;"  class='template-geneBox geneBox'>
  <span id="gene-info-span" style="display:none;"><span class='probeLabel'></span><span class='rlScoreLabel'></span><br/></span>
  <span class='geneSymbol'>A1BG</span><span class='icon-note'>&nbsp;</span><br/>
  <span id='gene-detail-span' class='geneDetail'>gene detail</span>
</div>
<g:javascript>
  var newGeneNote = function() {
    showNotePanel(updateGeneNotes);
  };
</g:javascript>


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

  var isTg2 = ${isTg2};
  var hasLabkeyData = ${hasLabkeyData};
  var hasOverlays = false;
  var chartWidth = $("#theChart").width();
  var currentGeneID  = ${params.geneID ?: -1};
  var currentGeneSymbol = "${params.geneSymbol}";
  var currentGeneDiv = "";
  var currentQuery = "";
  var currentSignalData = [];
  var currentSampleSetID = ${sampleSet?.id};
  var currentSampleSetName = "${sampleSet?.name}";
  var currentChipTypeID = ${sampleSet?.chipType?.id};
  var currentProbeID = "${params.probeID}";
  var currentGroupSetID = "${defaultGroupSetID}";
  var defaultRankList = ${defaultRankList ?: -1};
  var currentRankList = ${currentRankList ?: -1};
  var currentRankListType = "${currentRankListType ?: ''}";
  var currentRankListClass = "${currentRankListClass ?: ''}";
  var defaultRankListType = "${currentRankListType ?: ''}";	 // comes in from controller as current...
  var defaultRankListClass = "${currentRankListClass ?: ''}";// comes in from controller as current...
  var rankListTypeFilter = "range"; //diffFc
  var rankListClassFilter = "All";
  var crossProjectLink = "${createLink(controller:'geneBrowser', action:'crossProject')}";
  var module = "${params.module ?: ''}";
  var modVersionName = "${params.modVersionName ?: ''}";
  var analysisId = ${params.analysisId ?: -1};
  var signalDataTable = "${signalDataTable}";
  var drawChartLegend = true;
  var currentSort = "none";
  var currentSortType = "none";
  var maxGeneListScroll = 0;
  var geneNavChunk = 250;
  var geneNavOffset = 0;
  var showEmptyValues = false;
  var activeChartType = "${params.chartType ?: 'histogram'}";

  var clearSearch = function() {
	  $("#topSearch").val("");
	  submitQuery();  
  };
  
  var updateCrossProjectLink = function() {
    var link = crossProjectLink.concat("?probeID=").concat(currentProbeID);
    link = link.concat("&geneSymbol=").concat(currentGeneSymbol);
    link = link.concat("&geneID=").concat(currentGeneID);
    $("a[name='crossProjectLink']").attr("href", link);
  };

  var generateArgs = function() {
    var initOverlays = lastNumericalOverlay !== "none" || activeCategories > 0;
    var args =
    {
      _controller: "geneBrowser",
      _action: "show",
      _id: currentSampleSetID,
      defaultGroupSetId: currentGroupSetID,
      probeID: currentProbeID,
      geneSymbol: currentGeneSymbol,
      geneID: currentGeneID,
      currentQuery: $("#topSearch").val(),
      rankList: currentRankList,
      geneList: $("#geneListSelect").val(),
      chartType: activeChartType,
      currentSort: currentSort,
      currentSortType: currentSortType,
      numericalOverlay: lastNumericalOverlay,
      initOverlays: initOverlays
    };
    $.each(categoryOrder, function(k,order) {
      var color = categoricalColors[k];
      args["categorical_overlay:".concat(k)] = order + ":" + color;
    });
    if (analysisId !== -1) {
      args["analysisId"] = analysisId;
      args["module"] = module;
    }
    if (modVersionName !== '') {
        args["modVersionName"] = modVersionName;
        args["module"] = module;
    }
    $.extend(args, getUserSettings());
    return args;
  };

  var generateLink = function(showEmail,isClient)
  {
    var args = generateArgs();
    $.getJSON(getBase()+"/miniURL/create", args, function(json) {
      if (showEmail || isClient) {
        var subject = "Gene Expression Browser Link for Gene Symbol " + currentGeneSymbol;
        var text = "Hello,\r\n\tI thought you would be interested in looking at the " + currentSampleSetName + " sample set in the Gene Expression Browser.\r\nTo view it, click this link: " + json.link;
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
      pointWidth: parseInt(settings.pointWidth)
    };

    return args;
  };

  var legendSet = null;
  var refreshChart = function() {
    updateCrossProjectLink();
    if (activeChartType === "histogram")
    {
      showOverlay();
      showSort();
      drawBarchart(boxResize||initOverlays);
      boxResize = false;
      initOverlays = false;
    }
    else if (activeChartType === "boxplot")
    {
      hideOverlay();
      hideSort();
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
      hide: {
         inactive: 2000
      },

      style: {
        classes: 'ui-tooltip-tipsy'
      },
      hide: {
          inactive: 1500
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
    legendR.rect(0,0,mx+55,y+10).attr({ fill:"#fff", stroke:"#333", "stroke-width":1 }).toBack();
    legendR.setSize(mx+57,y+12);
    var svg = legendR.toSVG();
    legendR.clear();
    delete legendR;
    $.post(getBase()+"/charts/saveImg", { svg: svg }, function(id) {
      var location = getBase()+"/charts/downloadImg/"+id;
      var filename = "sampleset${sampleSet.id}_groupset"+currentGroupSetID+"_"+divId;
      window.location.href = location.concat("?filename=").concat(filename);
    });
  };
  var lastPointWidth = 0;
  var lastBar = null, lastScatterPoint = null, lastHighlightedSample = null;
  var barMouseover = function() {
    if (lastBar !== null)
    {
      barMouseout();
    }
    this.attr({ "fill-opacity":0.7 });
    lastBar = this;
    lastHighlightedSample = this.data("id");
//	  if (${sampleSet.defaultSignalDisplayType?.id} == 6) {
//   	$("#currentSignal").html(this.data("value").toFixed(4));
//    } else {
    	$("#currentSignal").html(this.data("value").toFixed(2));
//    }
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
  var denomMouseover = function() {
	var downText = "The samples of the '" + this.data("groupDName") + "' group comprise the " + this.data("groupDEnd") + " of the " + this.data("currentRankListType") + " rank list: '" + this.data("rankListDescription") + "'";
	tooltip($(this.node), downText);
  };
  var numerMouseover = function() {
  	var upText   = "The samples of the '" + this.data("groupNName") + "' group comprise the " + this.data("groupNEnd") + " of the " + this.data("currentRankListType") + " rank list: '" + this.data("rankListDescription") + "'";
  	tooltip($(this.node), upText);
  };
  var updateChartSettings = function() {
    var settings = getUserSettings();
    if (settings.pointWidth !== lastPointWidth) {
      lastPointWidth = settings.pointWidth;
      resizeChart();
    } else {
      // bg, title, yAxis, yAxisMaxTick, yAxisMinTick, yAxisTitle, xAxis, xAxisTitle
      // chart border
      var bgStroke = (settings.chartBorderWidth > 0) ? "#000" : "none";
      bRaphael.getById(eltIds[0]).attr({ stroke:bgStroke, "stroke-width":settings.chartBorderWidth });
      groupLabelSet.attr({ font:settings.xLabelStyle.fontSize+" Helvetica" });
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
  var drawBarchart = function(redrawOverlays) {
    if (currentProbeID !== "")
    {
      var div = $("#signalChart");
      var width = div.width();
      var height = $(window).height() - div.position().top - 40;
      var args =
      {
        sampleSetId: currentSampleSetID,
        groupSetId: currentGroupSetID,
        probeId: currentProbeID.replace('__', '/'),
        signalDataTable: signalDataTable,
        sort: currentSort,
        varType: currentSortType,
        rankListId: currentRankList,
        rankListVisibility: $('.rlScoreLabel').css("visibility")
      };
      $.getJSON(getBase()+"/charts/groupSetHistogram", args, function(json) {
      	$("img#loading").hide();
      	if (json.error)
      	{
      		$("#noSignal").html(json.error.message);
      		$("#noSignal").show();
      	}
      	else if (json.probeId.replace('/', '__') == currentProbeID)
        {
          var numGroups = json.groups.length, numPoints = 0;
          if (numGroups > 0)
          {
            $("#loading").hide();
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
            var usrArgs = getUserSettings();
            var pWidth = null;
            if (usrArgs.pointWidth > 0) {
              pWidth = usrArgs.pointWidth;
              pSpace = pWidth < 8 ? 1 : 2;
              cWidth = (pWidth + pSpace) * (numPoints + numGroups);
              width = yAxisWidth + cWidth + padding[1] + padding[3];
            } else {
              pWidth = visibleWidth / (numPoints + numGroups - 1) - pSpace;
              if (pWidth < 8)
              {
                pWidth = 8;
                pSpace = 1;
                cWidth = (pWidth + pSpace) * (numPoints + numGroups);
                width = yAxisWidth + cWidth + padding[1] + padding[3];
              }
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

			Raphael.fn.triangle = function(p) {
				var tString = "M" + p[0] + "," + p[1] + "L," + p[2] + "," + p[3] + "," + p[4] + "," + p[5] + "z";
				return this.path(tString);
			};

            Raphael.fn.barchart = function(json) {
              var options = $.extend({}, plotSettings);
              var paper = this;
              var topOffset = activeCategories * 10;
              var maxY = Math.round(Math.ceil(json.max));
              var offsetHeight = options.plotHeight - 40;
              var groupOffset = options.yAxisWidth;
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
                    lastBar = bar;
                  }
                  pointSet.push(bar);
                  if (i === g.points.length - 1)
                  {
                    var tX = (x + options.pointWidth + groupOffset) / 2;
                    var tY = topOffset + padding[0] + options.plotHeight + 14;
                    var groupLabel = paper.text(tX, tY, g.label).attr({ fill:g.color, font:"12px Helvetica", "font-weight":"bold"})
                    // Math for triangle, replaced by circle Boo!
					var offsetY = 13;
					var cRadius = 5;
                    var startX = Math.floor(tX - groupLabel.getBBox().width/1.67 - (cRadius * 2));
					var startY = Math.floor(topOffset + padding[0] + options.plotHeight + offsetY);
					var circle;
					if (g.up || g.down) {
					 	circle = paper.circle(startX, startY, cRadius);
//						console.log("up/down: " + g.up + "/" + g.down + " startX:" + startX + " startY:" + startY);
            			groupLabelSet.push(circle);
            			groupLabelSet.push(groupLabel);
            			if (g.up) {
   			  	 			groupLabelSet.data("groupNName", g.label);
   			  	 		} else {
   			  	 			groupLabelSet.data("groupDName", g.label);
   			  	 		}
   			  	  		groupLabelSet.data("rankListDescription", g.rankListDescription);
   			  	  		if (g.rankListAbbrev == "fc" || g.rankListAbbrev == "diffFc") {
	   			  	  			groupLabelSet.data("groupNEnd", "numerator");
	   			  	    		groupLabelSet.data("groupDEnd", "denominator");
	   			  	    } else {
	   			  	  			groupLabelSet.data("groupNEnd", "minuend");
	   			  	    		groupLabelSet.data("groupDEnd", "subtrahend");
						}
   			  	  		groupLabelSet.data("currentRankListType", g.rankListType);
            			if (g.up) {
            				circle.attr({fill: "#DB2929"});
            				circle.mouseover(numerMouseover);
            			} else { // g.down
            				circle.attr({fill: "#33A1DE"});
            				circle.mouseover(denomMouseover);
                		}
            		} else {
            			groupLabelSet.push(groupLabel);
                	}
                	pointSet.push(groupLabelSet);
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

            if (pointSet === null)
            {
              pointSet = bRaphael.set();
            }
            else
            {
              try { pointSet.remove(); } catch (e) {}
              pointSet.clear();
            }
            if (groupLabelSet === null) {
              groupLabelSet = bRaphael.set();
            }
            if (axisSet !== null) {
              try { axisSet.remove(); } catch (e) {}
              axisSet.clear();
              groupLabelSet.clear();
            }
            if (scatterSet !== null) {
              try { scatterSet.remove(); } catch (e) {}
              scatterSet.clear();
            }

            bRaphael.customAttributes.moveUp = function(offset) {
            	if (this.attr('cy')) {
              		return { cy:(this.attr("cy")+offset)  };
				} else if (this.attr('y')) {
            		return { y:(this.attr("y")+offset)  };
              	}
            };

            // draw title
            bRaphael.setStart();
            eltIds[0] = bRaphael.rect(0,0,width,topOffset+height).attr({ fill:"#fff", stroke:"none", "stroke-width":0 }).id;
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

  //          $.each(axisSet, function(i,ax) {
  //            var bb = ax.getBBox();
  //            var axY2 = bb.y + bb.height + 100;
  //            height = Math.max(height, axY2);
  //          });
  //          bRaphael.setSize(width,height);

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
            if (bRaphael !== null)
            {
              bRaphael.clear();
            }

            if (axisSet !== null) {
              axisSet = null;
              groupLabelSet = null;
            }
          }
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
      var div = $("#signalChart");
      var width = div.width();
      var height = $(window).height() - div.position().top - 40;
      var args =
      {
        sampleSetId: currentSampleSetID,
        groupSetId: currentGroupSetID,
        probeId: currentProbeID.replace('__', '/'),
        signalDataTable: signalDataTable,
        rankListId: currentRankList,
        rankListVisibility: $('.rlScoreLabel').css("visibility")
      };
      $.getJSON(getBase()+"/charts/groupSetHistogram", args, function(json) {
      	$("img#loading").hide();
       	if (json.error)
      	{
      		$("#noSignal").html(json.error.message);
      		$("#noSignal").show();
      	}
		else
		{      
        var numGroups = json.groups.length;
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
        var usrArgs = getUserSettings();

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
            try { pointSet.remove(); } catch (e) {}
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
            try { scatterSet.remove(); } catch (e) {}
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
			var cRadius = 5;
			var offsetY = 17;
			var startX = midX - groupLabel.getBBox().width/1.67 - (cRadius * 2);
			var startY = padding[0] + options.plotHeight + offsetY;
			if (g.up || g.down) {
            	var circle = paper.circle(startX, startY, cRadius);
            	groupLabelSet.push(circle);
            	groupLabelSet.push(groupLabel);
        		if (g.up) {
   			  		groupLabelSet.data("groupNName", g.label);
   			  	} else {
   			  		groupLabelSet.data("groupDName", g.label);
   			  	}
   			    groupLabelSet.data("rankListDescription", g.rankListDescription);
   			  	if (g.rankListAbbrev == "fc" || g.rankListAbbrev == "diffFc") {
	   			  	  	groupLabelSet.data("groupNEnd", "numerator");
	   			  	    groupLabelSet.data("groupDEnd", "denominator");
	   			 } else {
	   			  	  	groupLabelSet.data("groupNEnd", "minuend");
	   			  	    groupLabelSet.data("groupDEnd", "subtrahend");
				}
   			  	groupLabelSet.data("currentRankListType", g.rankListType);
            	if (g.up) {
            		circle.attr({fill: "#DB2929"});
            		circle.mouseover(numerMouseover);
            	} else { // g.down
            		circle.attr({fill: "#33A1DE"});
            		circle.mouseover(denomMouseover);
                }
            } else {	
            	groupLabelSet.push(groupLabel);
            }
            pointSet.push(groupLabel);
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
              sp.mouseover(scatterMouseover);//.mouseout(scatterMouseout)
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
          try { axisSet.remove(); } catch (e) {}
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
        eltIds[5] = bRaphael.text(plotSettings.yAxisWidth - 50, padding[0] - 35  + (plotSettings.plotHeight + 40) / 2, "Expression Values").attr({ fill:"#333", font:"14px Helvetica" }).rotate(-90, plotSettings.yAxisWidth - 40, (plotSettings.plotHeight + 40) / 2).id;

        // draw x-axis
        eltIds[6] = bRaphael.rect(plotSettings.yAxisWidth - 5, padding[0] + plotSettings.plotHeight + 2, plotSettings.plotWidth + 10, 1).attr({ fill:"#333", stroke:"none", "stroke-width":0 }).id;
        eltIds[7] = bRaphael.text(plotSettings.yAxisWidth + (plotSettings.plotWidth / 2), padding[0] + plotSettings.plotHeight + 55, "Group Set").attr({ fill:"#333", font:"14px Helvetica" }).id;
        axisSet = bRaphael.setFinish();

        bRaphael.boxplot(json);
        updateChartSettings();
        }
      });
    }
  };

  var sort = function(vartype,inputField) {
    var field = $(inputField).val();
    currentSort = field;
    currentSortType = vartype;
    initOverlays = true;
    refreshChart();
  };
  var showSort = function() {
    if (hasOverlays) {
      $("#sort-options").removeClass("disabled");
    }
  };
  var hideSort = function() {
    $("#sort-options").addClass("disabled");
  };
  var showGroupSet = function() {
    var nSets = "${groupSets.size()}";
  	if (nSets > 1)
  	{
  	  	//$("#groupset-options-display").show();
  		$("#groupset-options").removeClass("disabled");
  	}
  	else
  	{
  	  	//$("#groupset-options-display").hide();
  	  	$("#groupset-options").addClass("disabled");
  	}
  }
  var showRankList = function() {
//    var nSets = "${rankLists.size()}";
//  	if (nSets >= 0) // Always show because of buried options.
//  	{
  		$("#ranklist-options").removeClass("disabled");
//  	}
//  	else
//  	{
//  	  	$("#ranklist-options").addClass("disabled");
//  	}
  }
  
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
      colorSchemes.push(categoricalColors[k]);
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
      	if (numericalSet !== null) {
        	numericalSet.remove();
        	numericalSet = null;
        }
        numericalLine = null;
        pointSet.attr("fill-opacity", 1);
        draw = false;
        $("div.group-legend-key div[name='"+lastNumericalOverlay+"']").detach();
        lastNumericalOverlay = "none";
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
        success: function(json) {
          if (overlayType === "numerical") {
            if (numericalSet === null)
            {
              numericalSet = bRaphael.set();
            }
            else
            {
              try { numericalSet.remove(); } catch(e) {}
              numericalSet.clear();
            }
            drawNumericalOverlay(field,json,json.key,json.displayName);
          } else {
            drawCategoricalOverlay(field,json,json.key,json.displayName,colorScheme);
            if (activeCategories > 5) {
              $("#categorical-overlays input:not(:checked)").attr("disabled","disabled");
            }
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
      legend += "<div class='row'><span class='swatch' style='background-color:"+clr+"'></span><div class='span4'>"+cat+"</div></div>";
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
        numericalLine.attr({ stroke:"none", "stroke-width":0 });
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

    var usrArgs = getUserSettings();
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

    if (!usrArgs.showOverlayLines) {
      toggleNumericalLines(false);
    }

    lastNumericalOverlay = origField;
  };

  var jqxhr = null;
  var selectGene = function(geneID, symbol, probeID)
  {
    var divID = "#geneBox_" + probeID;

    $(currentGeneDiv).css('background-color', 'white');
    $(currentGeneDiv).css('color', 'black');
    $(divID).css('background-color', '#0064CD');
    $(divID).css('color', 'white');
    currentGeneDiv = divID;
    currentGeneSymbol = symbol;
    currentGeneID = geneID;
    currentProbeID = probeID;  / current probe ID will have __ instead of /

	setTimeout(function() { updateGeneNotes(); }, 0);
	
    if ($(divID).find("span.icon-note").is(":visible") && $("div#tab-gene").hasClass("active"))
    {
      var height = $("div.info-scrollPanel")[0].scrollHeight;
      $("div.info-scrollPanel").scrollTop(height);
    }

    refreshChart();
    $('#currentGeneSymbol').html(symbol);
    $("span#gene-usernotes").html("--");

    if (currentGeneID >= 0 && currentGeneID !== null)
    {
      $("span[id='gene.ncbi_link']>a").attr("href", "http://www.ncbi.nlm.nih.gov/gene/"+currentGeneID);
      $("span[id='gene.wolfram_link']>a").attr("href", "http://www.wolframalpha.com/input/?i=gene+"+currentGeneSymbol.toUpperCase());
      $("span[id='gene.wiki_link']>a").attr("href", "http://en.wikipedia.org/wiki/"+currentGeneSymbol.toUpperCase());
      if (jqxhr !== null) {
        jqxhr.abort();
      }
      jqxhr = $.getJSON(getBase()+"/geneInfo/queryGeneInfo", { geneID: currentGeneID, geneSymbol: currentGeneSymbol, ssid: currentSampleSetID }, function(json) {
        if (json.geneId == currentGeneID) {
        	if (json.entrez) {
          		var data = json.entrez;
          		$.each(data, function(key,value) {
              		var span = $("span[id='gene."+key+"']");
              		if (span) {
              			span.text(value);
              		}
          		});
          	}
         if (json.pubmed)
          {
            setInfoBrowserTitle("Pubmed Abstracts");

            var pubmedData = json.pubmed;
            var numArticles = pubmedData.numArticles;
            if (numArticles != 0) {
              var pubmedLink = "<span class='pubmed-link' onclick='javascript:showPubmedLinks();'>" + numArticles + " Pubmed articles</span>";
              $("span#gene-pubmedlinks").html("There are " + pubmedLink + " linked to this gene.");
              var infoHtml = "<p>Showing up to 25 articles related to the gene, <strong>" + currentGeneSymbol + "</strong></p>";
              infoHtml += "<table class='zebra-striped'>";
              infoHtml += "<thead><tr><th>Date</th><th>Title</th></tr></thead><tbody>";
              $.each(pubmedData.articles, function(i,article) {
                infoHtml += "<tr><td>";
                if (article.PubDate) {
                  infoHtml += article.PubDate;
                }
                infoHtml += "</td><td><a href='http://www.ncbi.nlm.nih.gov/pubmed/" + article.pmid + "' target='_blank'>" + article.Title + "</a></td></tr>";
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
//      setTimeout(function() { updateGeneNotes(); }, 0);
	// Only grab sampleSet Links if it hasn't already been done.
	if ($("#pubmedData").html() == "" || $("#geoData").html() == "") {
		updateSampleSetLinks();
	}
//      setTimeout(function() { updatePubmedLinks(); }, 100);
    }

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
      if (json && json.numArticles) {
        var pubmedLink = "<span class='pubmed-link' onclick='javascript:showPubmedLinks();'>" + json.numArticles + " Pubmed articles</span>";
		$("span#gene-pubmedlinks").html("There are " + pubmedLink + " linked to this gene.");
      	var infoHtml = "<p>Showing up to 25 articles related to the gene, <strong>" + currentGeneSymbol + "</strong></p>";
        infoHtml += "<table class='zebra-striped'>";
        infoHtml += "<thead><tr><th>Year</th><th>Title</th></tr></thead><tbody>";
        $.each(json.articles, function(i,article) {
          infoHtml += "<tr><td>";
          if (article.PubDate) {
            infoHtml += article.PubDate;
          }
          infoHtml += "</td><td><a href='http://www.ncbi.nlm.nih.gov/pubmed/" + article.pmid + "' target='_blank'>" + article.Title + "</a></td></tr>";
        });
        infoHtml += "</tbody></table>";
        if (json.numArticles > 25) {
          infoHtml += "<p><a href='http://www.ncbi.nlm.nih.gov/gene?Db=pubmed&DbFrom=gene&Cmd=Link&LinkName=gene_pubmed&LinkReadableName=PubMed&IdsFromResult=" + currentGeneID + "' target='_blank'>More PubMed articles...</a></p>";
        }
        setInfoBrowserContent(infoHtml);
      }
    });
  };

  var updateSampleSetLinks = function ()
  {
  	$.getJSON(getBase()+"/geneBrowser/querySampleSetInfo", { ssid: currentSampleSetID }, function(json) {
		if (json) {
			var pubmedHtml = "";
			if (json.pubmedData) {
				pubmedHtml = "<td><strong>Pubmed Entry</strong></td><td>";
				if (json.pubmedUrl) {
					pubmedHtml += "<a href=\"" + json.pubmedUrl + "\" target=\"_blank\">" + json.pubmedData + "</a>";
				}
				if (json.pubmedDOI) {
					var link = "http://dx.doi.org/" + json.pubmedDOI
					var text = "Journal Article";
					if (! json.pubmedUrl) {
						text = json.pubmedData;
					}
					pubmedHtml += " | <a href=\"" + link + "\" target=\"_blank\">" + text + "</a>";
				}
				pubmedHtml += "</td>";
				$("tr#pubmedData").html(pubmedHtml);
				$("tr#pubmedData").show();
			}
			var geoHtml = "";
			if (json.geoData && json.geoUrl) {
				geoHtml = "<td><strong>GEO Entry</strong></td><td><a href=\"" + json.geoUrl + "\" target=\"_blank\">" + json.geoData + "</a></td>";
				$("tr#geoData").html(geoHtml);
				$("tr#geoData").show();
			}
		}
	});
  
  };
  var runQuery = function(queryEntry, queryText, chunk, offset)
  {
    // clear existing info
    $("#resultList").hide();
    $("#errorNotFound").hide();
    $("#alertMsg").show();

	// grab defaults if missing.
    chunk = chunk ? chunk : geneNavChunk; 
    offset = offset ? offset : geneNavOffset;
    
    var args =
      {
        queryString: queryEntry,
        rankList: currentRankList,//$('#rankListSelect').val(),
        sampleSetID: currentSampleSetID,
        rt: "span",  // return type = span
        module: "${params.module}",
        modVersionName: "${params.modVersionName}",
        analysis: "${params.analysisId}",
        geneListId: $("#geneListSelect").val(),
        signalDataTable: signalDataTable,
        returnNotes: true,
        limit: chunk,
        offset: offset
      };

      $.getJSON("../getGeneList", args, function(json)
      {
        $("#alertMsg").hide();
  //			console.log('json gene list returned');
  //			console.log('array size: ' + json.gl.length);

        $('#resultList').replaceWith("<div id='resultList'> </div>");
        if (json.gl != "")
        {
          var scoreLabel = currentRankListType;
          if (scoreLabel === "other") {
            scoreLabel = "Score";
          }
          for (var j = 0; j < json.gl.length; j++)
          {
            var geneName = json.gl[j].name;
            var title = json.gl[j].symbol;
            if (geneName !== null) {
              title = title.concat(" - ").concat(geneName);
            } else {
              geneName = "--";
            }
            var geneId = json.gl[j].geneid;
            if (!geneId) {
              geneId = json.gl[j].alt_geneid;
            }
            $newItem = $('.template-geneBox').clone();
            var newID = 'geneBox_' + json.gl[j].probe.replace('/', '__');  // no slashes in HTML id
            //console.log("newID: " + newID);
            $newItem.attr('id', newID);
            $newItem.attr('class', 'geneBox');
            $newItem.attr('title', title);
            $newItem.find('.geneSymbol').text(json.gl[j].symbol);
            $newItem.find('.geneDetail').text(geneName);
            if (json.gl[j].notes === 0) {
              $newItem.find('.icon-note').hide();
            } else {
              $newItem.find('.icon-note').attr('title', 'There are ' + json.gl[j].notes + ' notes about this gene');
            }
            if (scoreLabel && scoreLabel !== "" && json.gl[j].val) { // diffFc ranks don't have a value, so this is left off.
              var newScore = '<div class="rlscore">'+scoreLabel+': <span class="' + json.gl[j].upDown + '"';
              if (json.gl[j].pvalue) { // not all ranks have p-values, add if not 0/Null
              	newScore += ' title="p-value: ' + json.gl[j].pvalue.toPrecision(2) + '"';
              }
              newScore += '>' + json.gl[j].val + '</span></div>';
              $newItem.find('.rlScoreLabel').html(newScore);
              //$newItem.find('.rlScoreLabel').html('<div class="rlscore">'+scoreLabel+': <span class="' + json.gl[j].upDown + '" title="p-value: ' + json.gl[j].pvalue.toPrecision(2) + '">' + json.gl[j].val + '</span></div>');
            }
  			$newItem.find('.probeLabel').text('probe: ' + json.gl[j].probe.replace('/', '__'));
            var onclick = "selectGene('" + geneId + "','" + json.gl[j].symbol + "','" + json.gl[j].probe.replace('/', '__') + "');";
            $newItem.attr('onclick', onclick);
            $('#resultList').append($newItem.show());
          }
          // Pagination for the gene list.
          var container = "<div style='text-align: center; margin-bottom: 5px; padding: 7px;'><span class='geneListNav'>";
          var closer = "</span></div>";
          
          var revString = "Prev"
          var fwdString = "Next";

          var fwdOffset = offset + chunk;
          var revOffset = offset - chunk;
          var isFirst = (revOffset == 0);
          var needRev = (revOffset >= 0);
		  var needFwd = (json.count >= chunk);

          if (needRev)
          {
	        revString = "<a class=\"btn mini primary\" href='javascript:runQuery(" + (queryEntry ? '"' + queryEntry +'"' : '""') + "," + (queryText ? '"' + queryText +'"' : '""') + "," + chunk + "," + 0 + ")'>First " + chunk.toString() + "</a>";
			if (! isFirst)
            {
			  revString += " | ";
              revString += "<a class=\"btn mini primary\" href='javascript:runQuery(" + (queryEntry ? '"' + queryEntry +'"' : '""') + "," + (queryText ? '"' + queryText +'"' : '""') + "," + chunk + "," + revOffset + ")'>Prev " + chunk.toString() + "</a>";
            }
          	$('#resultList').prepend(container + revString + closer);
          }
          
	      if (needFwd)
          {
          	fwdString = "<a class=\"btn mini primary\" href='javascript:runQuery(" + (queryEntry ? '"' + queryEntry +'"' : '""') + "," + (queryText ? '"' + queryText +'"' : '""') + "," + chunk + "," + fwdOffset + ")'>Next " + chunk.toString() + "</a>";
          	$('#resultList').append(container + fwdString + closer);
          }
          
          var firstGene = json.gl[0].geneid;
          if (!firstGene) {
            firstGene = json.gl[0].alt_geneid;
          }
          selectGene(firstGene, json.gl[0].symbol, json.gl[0].probe.replace('/', '__'));
        }
        else
        {
          $('#notFoundMsg').html("<span id='notFoundMsg'>No records found for: " + (queryText ? queryText: queryEntry) + "</span>");
          $('#errorNotFound').show();
        }
        maxGeneListScroll = $("div#geneList")[0].scrollHeight - $("div#geneList").height();
      });

  };

  var submitQuery = function(qt)
  {
    $("#topSearch").blur();
    $("#topSearch").autocomplete("close");
    var queryEntry = "";
    if (qt == "top")
      queryEntry = $("#topSearch").val()
    else
      queryEntry = $('#queryEntry').val();
    runQuery(queryEntry);
    return false;
  };

  var toggleRankListScore = function(force)
  {
    var currentVal = $('.rlScoreLabel').css("visibility");
//    console.log(currentVal);
    if (currentVal == "hidden" || force)
    {
      $("span#gene-info-span").show();
      $('.rlScoreLabel').css("visibility", "visible");
      $('#showRankListScoreMenu').html("Hide Rank List Score");
    }
    else
    {
      $('.rlScoreLabel').css("visibility", "hidden");
      if ($(".probeLabel").css("visibility") === "hidden") {
        $("span#gene-info-span").hide();
      }
      $('#showRankListScoreMenu').html("Show Rank List Score");
    }
    resizeChart();
  };

  var toggleShowProbeID = function()
  {
    var currentVal = $('.probeLabel').css("visibility");
//    console.log(currentVal);
    if (currentVal == "hidden")
    {
      $("span#gene-info-span").show();
      $('.probeLabel').css("visibility", "visible");
      $('#showProbeIDMenu').html("Hide Probe ID");
    }
    else
    {
      $('.probeLabel').css("visibility", "hidden");
      if ($(".rlScoreLabel").css("visibility") === "hidden") {
        $("span#gene-info-span").hide();
      }
      $('#showProbeIDMenu').html("Show Probe ID");
    }
  };
  var toggleSymbolDetails = function()
  {
    var currentVal = $('.geneDetail').css("visibility");
    if (currentVal == "hidden")
    {
      $('.geneDetail').css("visibility", "visible");
      $("span#gene-detail-span").show();
    }
    else
    {
      $('.geneDetail').css("visibility", "hidden");
      $("span#gene-detail-span").hide();
    }
  };

  var toggleShowEmptyValues = function()
  {
    if (showEmptyValues)
    {
      showEmptyValues = false;
      $('#showEmptyValuesMenu').html("Show Empty Sample Info");
    }
    else
    {
      showEmptyValues = true;
      $('#showEmptyValuesMenu').html("Hide Empty Sample Info");
    }
  };

  var changeGroupSet = function(inputField)
  {
    $("#resultList").hide();
    $("#alertMsg").show();
    $("div#chart-legend-content").html("");
    drawChartLegend = true;

    currentGroupSetID = $(inputField).val();

    // update groups for export
    $.getJSON(getBase()+"/datasetGroupSet/getGroups", { id:currentGroupSetID } , function(json) {
      if (json) {
        var optionHtml = '';
        $.each(json, function(i,grp) {
          optionHtml += '<option value="'+grp.id+'">'+grp.name+'</option>'
        });
        $("select#groupDownload").html(optionHtml);
      }
    });

    var args = { sampleSetId:currentSampleSetID, groupSetId:currentGroupSetID };
    updateRankListSelector(args, null, null);  // override the current settings by taking the defaults type and class from the results of the group set lookup.

    var queryEntry = $("#topSearch").val()
    runQuery(queryEntry);

  };

  var changeRankList = function()
  {
    if (!$("#ignoreRankList").is(":checked")) {
      currentRankList = $("#rankListSelect").val();
      currentRankListType = $("#rankListSelect option[value='"+currentRankList+"']").attr("id");
      var queryEntry = $("#topSearch").val()
      runQuery(queryEntry);
    }
  };

//  var ignoreRankList = function() {
//    if ($("#ignoreRankList").is(":checked")) {
//      currentRankList = -1;
//      currentRankListType = null;
//      var queryEntry = $("#topSearch").val()
//      runQuery(queryEntry);
//    } else {
//      changeRankList();
//    }
//  };

  var ignoreRankList = function() {
      currentRankList = -1;
      currentRankListType = null;
      $("#rankListSelect").val(currentRankList);
      var queryEntry = $("#topSearch").val()
      runQuery(queryEntry);
  };

  var resetRankList = function() {
    $("#ignoreRankList").attr("checked", false);
    currentRankList = defaultRankList;
    currentRankListType = defaultRankListType;
    $("#" + defaultRankListType).prop('checked', true);
    $("#" + defaultRankListClass).prop('checked', true);
    var args = { sampleSetId:currentSampleSetID, groupSetId:currentGroupSetID };
    updateRankListSelector(args, defaultRankListType, defaultRankListClass);
    $("#rankListSelect").val(defaultRankList);
    var queryEntry = $("#topSearch").val()
    runQuery(queryEntry);
  };

  var changeGeneList = function() {
    var queryEntry = $("#topSearch").val()
    var queryText = $("#geneListSelect option:selected").text();
    runQuery(queryEntry, queryText);
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
    // clear display info
    var args =
    {
      sid: sid,
      dsid: currentSampleSetID
    };

    if (isTg2)
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
    if (hasLabkeyData)
    {
      $.getJSON(getBase()+"/geneBrowser/labkeySampleInfo", args, function(json) {
        if (json)
        {
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
                  } else {
                    if (field === "participant_id" && !currentPid) {
                      currentPid = value;
                    }
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
              html = "";
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
                else if (!currentPid)
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
    $.getJSON(getBase()+"/geneBrowser/overlayOptions", args, function(json) {
      if (json)
      {
        hasOverlays = true;
        var html = "";
        var sortHtml = "<li><label class=\"nowidth\"><input type=\"radio\" id=\"lineOverlay\" onclick=\"sort('none',this)\" value=\"none\" checked=\"checked\" name=\"sortBy\"> <span>None</span></label></li>";
        if (json.categorical && json.categorical.length > 0)
        {
          sortHtml += "<div style=\"font-weight:bold;font-size:14px;\">Categorical</div>";
          html += "<div style=\"font-weight:bold;font-size:14px;\">Categorical</div>";
          html += "<form id=\"categorical-overlays\" name=\"categorical-overlays\" method=\"post\">";
          $.each(json.categorical, function(i,overlay) {
            var overlayKey = overlay.collection+"_"+overlay.key;
            var checked = typeof categoryOrder[overlayKey] !== "undefined" ? " checked=\"checked\"" : "";
            html += "<li><label class=\"nowidth\"><input type=\"checkbox\" id=\""+overlayKey+"\" onclick=\"drawOverlay('categorical',this)\" name=\""+overlayKey+"\""+checked+"> <span>"+overlay.displayName+"</span></label></li>";
            sortHtml += "<li><label class=\"nowidth\"><input type=\"radio\" onclick=\"sort('categorical',this)\" value=\""+overlayKey+"\" name=\"sortBy\""+checked+"> <span>"+overlay.displayName+"</span></label></li>";
          });
          html += "</form>";
        }
        if (json.numerical && json.numerical.length > 0)
        {
          sortHtml += "<div style=\"font-weight:bold;font-size:14px;\">Continuous</div>";
          html += "<div style=\"font-weight:bold;font-size:14px;\">Continuous</div>";
          html += "<form id=\"numerical-overlays\" name=\"categorical-overlays\" method=\"post\">";
          html += "<li><label class=\"nowidth\"><input type=\"radio\" id=\"lineOverlay\" onclick=\"drawOverlay('numerical',this)\" value=\"none\" checked=\"checked\" name=\"lineOverlay\"> <span>None</span></label></li>";
          $.each(json.numerical, function(i,overlay) {
            var overlayKey = overlay.collection+"_"+overlay.key;
            var checked = lastNumericalOverlay == overlayKey ? " checked=\"checked\"" : "";
            html += "<li><label class=\"nowidth\"><input type=\"radio\" id=\"lineOverlay\" onclick=\"drawOverlay('numerical',this)\" value=\""+overlayKey+"\" name=\"lineOverlay\""+checked+"> <span>"+overlay.displayName+"</span></label></li>";
            sortHtml += "<li><label class=\"nowidth\"><input type=\"radio\" onclick=\"sort('numerical',this)\" value=\""+overlayKey+"\" name=\"sortBy\""+checked+"> <span>"+overlay.displayName+"</span></label></li>";
          });
          html += "</form>";
        }
        $("span#overlay-options").next("ul#overlay-options-options").html(html);
        $("span#overlay-options").removeClass("disabled");
        $("span#sort-options").next("ul#sort-options-options").html(sortHtml);
        $("span#sort-options").removeClass("disabled");
      }
    });
  };
  
  function changeRankSelector(evt) {
    currentRankListType = $("input[name=rankType]:checked").val();
    currentRankListClass = $("input[name=rankClass]:checked").val();
    var args = { sampleSetId:currentSampleSetID, groupSetId:currentGroupSetID };
    updateRankListSelector(args, currentRankListType, currentRankListClass);
  }

  var updateRankListSelector = function(args, rlType, rlClass) {    // update the rank lists
  	// Walk through current list of ranks and filter, or get qualifying rankList via ajax?
	// Need to make sure this finishes (e.g async: false), and sets currentRankList before we request the new geneList.
// 	$.getJSON(getBase()+"/geneBrowser/rankLists", args, function(json) {
     $.ajax({
        url: getBase()+"/geneBrowser/rankLists",
        dataType: 'json',
        data: args,
        cache: false,
        async: false,
        success: function(json) {
      defaultRankList = json.defaultRankList ? json.defaultRankList : -1;
      currentRankList = json.defaultRankList ? json.defaultRankList : -1;
      defaultRankListType = json.defaultRankListType;
      defaultRankListClass = json.defaultRankListClass;
      if (rlType == null)
      {
      	if (json.defaultRankListType)
      	{
      		rlType = json.defaultRankListType;
      	}
      	else
      	{
      	    rlType = rankListTypeFilter;
      	}
      	currentRankListType = rlType;
      	$("#" + rlType).prop('checked', true);
      }
      if (rlClass == null)
      {
      	if (json.defaultRankListClass)
      	{
      		rlClass = json.defaultRankListClass;
      	}
      	else
      	{
      		rlClass = rankListClassFilter;
      	}
      	currentRankListClass = rlClass;
      	$("#" + rlClass).prop('checked', true);
      }
	  //console.log("curr rl: " + currentRankList + " new type: " + rlType + " new class: " + rlClass);
      // update rank list chooser
      if (json.ranklists)
      {
        var html = '<option value="-1">-select a rank list-</option>';
        $.each(json.ranklists, function(i,rl) {
          if (rl.rankListType == rlType && (rl.description.startsWith(rlClass) || rlType == 'range')) {
	          html += '<option id="'+rl.rankListType+'" value="'+rl.id+'"';
	          if (rl.id == defaultRankList)
	          {
	            html += ' selected="selected">+';
	          }
	          else
	          {
	          	html += '>';
	          }
	          if (rlType == 'overall')
	          {
	            html += 'Overall</option>';
	          }
	          else
	          {
	          	html += rl.description.substr(rl.description.indexOf(" ") + 1) + '</option>';
	          }
          }
        });
        $("#rankListSelect").html(html);
      }
      showRankList();
      }
  	});
  };
  
  $(document).ready(function() {
  	$("input[name=rankType]:radio").change(changeRankSelector);
  	$("input[name=rankClass]:radio").change(changeRankSelector);
  	
    $('#queryButton').click(submitQuery);
    $('#errorNotFound').hide();

    // adjust for topbar, other stuff
    var windowHeight = $(window).height() - 80;
    $('#geneList').height(windowHeight - 30);

    // if starting from a saved link try to restore the previous state, otherwise just the defaults
	//alert("set:" + '${params.geneID}' + "|" + '${params.geneSymbol}' + "|" + '${params.probeID}');
	//alert("currentQuery: " + '${params.currentQuery}');
	
	if ("${params?.currentSort}" != "none")
	{
		initOverlays = true;
		currentSort = "${params?.currentSort ?: 'none'}";
		currentSortType = "${params?.currentSortType ?: 'none'}";
	}
		
    if ("${params.currentQuery}" != "")
    {
      $('#topSearch').val('${params.currentQuery}');
      submitQuery("top");

    }
    else
    {
      submitQuery();
    }
    $("#"+activeChartType).addClass("active");

	$(".alert-message a.close").click(function() {
      $(this).closest(".alert-message").hide();
      clearSearch();
    });

    var windowWidth = $(window).width(), windowHeight = $(window).height();
    var resizing;
    $(window).resize(function() {
      if (windowWidth !== $(window).width() && windowHeight !== $(window).height()) {
        windowWidth = $(window).width();
        windowHeight = $(window).height();
        clearTimeout(resizing)
        resizing = setTimeout(function() { resizeChart(); }, 100);
      }
      $('#geneList').height(windowHeight - 110);
    });

    updateOverlayOptions();
    showGroupSet();
	showRankList();
    
    $("#geneList").scroll(function() {
      if ($("#geneList").scrollTop() >= maxGeneListScroll) {
        // add more to gene list

      }
    });
    
    $(document).ajaxStart(function() {
    	$("#ajaxLoading").show();
    });
    
	$(document).ajaxStop(function() {
    	if ('${params.geneID}' != '' && '${params.geneSymbol}' != '' && '${params.probeID}' != '')
    	{
    		//alert("using selectGene");
        	selectGene('${params.geneID}', '${params.geneSymbol}', '${params.probeID}'.replace('/', '__'));
	        var container = $('#geneList'), scrollTo = $('#geneBox_' + '${params.probeID}'.replace('/', '__'));
        	//container.scrollTop(scrollTo.offset().top - container.offset().top + container.scrollTop());
        	container.animate({scrollTop: (scrollTo.offset().top - container.offset().top + container.scrollTop())}, 1000);
        }
		$(this).unbind('ajaxStop');
		$("#ajaxLoading").hide();
    });

  });
</script>

</g:else>
</div>
<g:render template="/common/bugReporter" model="[tool:'GXB']"/>
<g:render template="/common/addNote"/>
<g:render template="/common/infoBrowser"/>

</body>
</html>
