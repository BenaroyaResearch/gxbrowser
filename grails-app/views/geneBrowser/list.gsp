<%@ page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils; org.sagres.sampleSet.SampleSetLink; org.sagres.sampleSet.SampleSetRole" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

  <title>GXB: Published Sample Sets</title>
  <g:javascript src="jquery-ui-min.js"/>
  <g:javascript src="jquery.metadata.js"/>
  <g:javascript src="jquery.qtip.min.js"/>
  <g:javascript src="common.js"/>
  <g:if test="${grailsApplication.config.target.audience.contains('BIIR')}">
    <g:javascript src="jquery.tablesorter.biir.js"/>
  </g:if>
  <g:javascript src="jquery.tablesorter.min.js"/>
  <g:javascript src="jquery.tablesorter.pager.js"/>
  <g:javascript>
  	var isIE = $.browser.msie;
    var execFilter = function() {
      $("#filter-samplesets-form").submit();
    };
    var filterClear = function() {
      	$("input.fpCheckBox").removeAttr('checked');
      	$("#significantGeneSearch").val("");
      	$("#foldChangeMin").val("2.0")
      	execFilter();
    };
    var searchClear = function() {
    	$("#sampleSetSearch").val("");
    	$("#search-samplesets-form").submit();
    };
    var toggleFilterPanel = function() {
      var elt = $("a#show-filters");
      if (elt.html() === "Show Filters")
      {
        elt.html("Hide Filters");
      }
      else
      {
        elt.html("Show Filters");
      }
      $("#search-filter-panel").toggle("slide");
      $(".content-menu").toggleClass("withFilter");
      $("#search-filter-panel>form>#filterPanelShow").val($("#search-filter-panel").is(":visible"));
    };
    var generateRequest = function(showEmail,isClient)
  	{
	    var subject = "GXB: Request to load a public GEO sample set";
        var text = "Hello Development Team,\r\n\tI would like to request that you load public sample set into the Gene Expression Browser. Please load {put GSE# here}\r\n\r\n";
	    if (isClient) {
          var encodedText = encodeURIComponent(text);
          var encodedTo = encodeURIComponent("${grailsApplication.config.importer.defaultEMailFrom}");
          window.location = "mailto:?to=" + encodedTo + "&subject=" + subject + "&body=" + encodedText;
        }
        if (showEmail) {
          $("form#emailLinkForm #subject").val(subject);
          $("form#emailLinkForm #message").val(text);
          showEmailPanel();
        }
    };
    var textExtractor = function(node) {
      var childNode = node.getElementsByTagName("span")[0];
      if (!childNode)
      {
      	childNode = node.getElementsByTagName("a")[0];
      }
      if (childNode)
      {
        var nodeName = childNode.nodeName.toLowerCase();
        if (nodeName === "span")
        {
          return childNode.title;
        }
        else if (nodeName === "a")
        {
          return childNode.innerHTML;
        }
      }
      if (isIE) {
        return node.innerText;
      } else {
        return node.textContent;
      }
    };
    $(document).ready(function() {
      $("table#samplesets").tablesorter({
       	sortList: [[0,0]],
	    textExtraction: textExtractor,
	    widthFixed: true,
      }).tablesorterPager({
      	container: $("#pager"),
      	size: 20
      });
      $("#significantGeneSearch").autocomplete({
        source: function(request, response) {
          // request.gxb = true;
          $.getJSON(getBase()+"/geneBrowser/getAllSymbols", request, function(data) {
            var items = [];
            $.each(data, function(key, val) {
              items.push({ label: val.text, value: val.text });
            });
            response(items);
          });
         },
         select: function(event, ui) { 
         	//alert(ui.item.label + " selected")
        	$("#signficantGeneSearch").val(ui.item.label);
        	execFilter();
       	},  
        minLength : 0,
        delay     : 150
      });
      $("#slider-range-max").slider({
        range: "max",
        min: 1.0,
        max: 4.0,
        step: 0.1,
        value: ${params.foldChangeMin},
        slide: function(event, ui) {
             $("span#floor-value").html(ui.value);
        },
        stop: function(event, ui) {
             $("input#foldChangeMin").val(ui.value);
         	execFilter();
        }
      });
      $("#significantGeneClear").click(function() {
      	$("#significantGeneSearch").val("");
      	$("#foldChangeMin").val("2.0")
        execFilter();
      });

      $("#sampleSetSearch").autocomplete({
        source: function(request, response) {
          request.gxb = true;
          $.getJSON(getBase()+"/sampleSet/titles", request, function(data) {
            var items = [];
            $.each(data, function(key, val) {
				items.push({label: val.text, value: request.term, gbxUrl: val.gxbUrl });
           });
            response(items);
          });
        },
        select: function(event, ui) {
          $("#sampleSetSearch").val(ui.item.value);
          location.href = getBase()+ui.item.gbxUrl;
          return false;
        },
        minLength : 0,
        delay     : 150
      });

      $("a#show-filters").click(function() {
        toggleFilterPanel();
      });

      $(".close-filter").click(function() {
        toggleFilterPanel();
      });

      $("#search-filter-panel").delegate("input.fpCheckBox", "change", function() {
        $("div.content-menu").addClass("filterEmpty").find("h2>small").detach();
        execFilter();
      });

    });
  </g:javascript>
  <style type="text/css">
    table#samplesets tr td:last-child {
      width: 200px;
    }
    a#show-filters {
      display: inline;
    }
  </style>

</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">
	%{--<div class="topbar-inner bri-fill">--}%
    <div class="sampleset-container">
        <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

        <h3><g:link controller="geneBrowser" action="list"><strong>GXB</strong></g:link></h3>

      <g:form name="search-samplesets-form" controller="geneBrowser" action="list" >
        <g:set var="place" value="${params.sampleSetSearch ? params.sampleSetSearch : 'Search'}" />
        <input name="sampleSetSearch" id="sampleSetSearch" type="text" placeholder="${place}" />
         <a id="clear-search" href="#" class="btn small primary topbar-clear" onclick="searchClear();">Clear Search</a>
         <a id="show-filters" href="#" class="btn small primary topbar-btn">Show Filters</a>

      </g:form>
      <ul class="nav secondary-nav">
        <g:if test="${grailsApplication.config.send.email.on}">
          <li><g:link onclick="generateRequest(false,true); return false;" class="request-link noshade" title="Request to load a SampleSet"></g:link></li>
        </g:if>

         <li class="dropdown" data-dropdown="dropdown">
          <a href="#" class="dropdown-toggle">Tools</a>
          <ul class="dropdown-menu skin-dropdown">
              <g:if test="${!grailsApplication.config.target.audience.contains('ITN')}">
                   <li><g:link controller="sampleSet" action="list" target="_blank">Annotation Tool</g:link></li>
               </g:if>
               <g:if test="${grailsApplication.config.tools.available.contains('mat') }">
                   <li><g:link controller="mat" action="list" target="_blank">Module Analysis Tool</g:link></li>
                    %{--<li><g:link controller="MATWizard" action="intro" target="_blank">Module Analysis Wizard</g:link></li>--}%
               </g:if>
               <g:if test="${grailsApplication.config.tools.available.contains('metacat') }">
                   <li><g:link controller="metaCat" action="list" target="_blank">MetaCat Tool</g:link></li>
                    %{--<li><g:link controller="MATWizard" action="intro" target="_blank">Module Analysis Wizard</g:link></li>--}%
               </g:if>
                     <li class="divider"></li>
               <li><a href="" onclick="reportBug(true);return false;">Send Feedback</a></li>
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

<div class="sampleset-container">

    <div id="search-filter-panel" style="${params.filterPanelShow ? "" : "display:none;"}" class="well">
    <div class="filter-panel-header"><span name="allClear" class="button tiny" onclick="filterClear();">Clear All Filters</span><span class="button tiny close-filter">x</span></div>
    <g:form name="filter-samplesets-form" action="filteredSampleSets">
    <g:hiddenField name="filterPanelShow" value="${false}"/>
    <g:hiddenField name="sampleSetSearch" value="${params.sampleSetSearch}"/>
    <g:hiddenField name="controllerAction" value="list"/>
    <div class="filterPanel" id="significantGenesFilterPanel">
    	<h4>${significantGenes.uiLabel}</h4>
    	<div class="fpFieldset">
    		<!-- <g:set var="place" value="${params.significantGeneSearch ? params.significantGeneSearch : 'Search'}" /> -->
        	<input type="text" class="fpInput" name="significantGeneSearch" id="significantGeneSearch" value="${params.significantGeneSearch}" placeholder="Gene Symbol"/>
        	<br/>
        	<div class="indView" id="filter-slider">
      			<div class="filter-slider-caption"><span id="floor-value">${params.foldChangeMin} Fold Change</span></div>
      			<div class="max-slider">
            		<div class="slider-range-max" id="slider-range-max"></div>
      			</div>
      			<g:hiddenField name="foldChangeMin" id="foldChangeMin" value="${params.foldChangeMin}"/>
    		</div>
    		
        	<g:if test="${params.significantGeneSearch}">
        		<button style="margin: 5px 0px;" class="button medium" name="significantGeneClear" id="significantGeneClear" value="clear">Clear</button>
        	</g:if>
    	</div>
    </div>
    <g:if test="${diseases.items}">
    <div class="filterPanel" id="diseaseFilterPanel">
      <h4>${diseases.uiLabel}</h4>
        <div class="fpFieldset">
          <g:each in="${diseases.items}" var="listItem">
            <span class="checkbox_item">
            <input type="checkbox" class="fpCheckBox" name="disease" value="${listItem.dbId}" ${checkedBoxes?.disease?.contains(listItem.dbId) ? "checked" : ""}/>
            <span>${listItem.uiLabel} <span class="cat_number">(<g:formatNumber number="${listItem.nItems}" type="number" maxFractionDigits="0"/>)</span></span><br/>
            </span>
          </g:each>
        </div>
    </div>
    </g:if>
    <g:if test="${sampleSources.items}">
    <div class="filterPanel" id="sampleSourceFilterPanel">
      <h4>${sampleSources.uiLabel}</h4>
        <div class="fpFieldset">
          <g:each in="${sampleSources.items}" var="listItem">
            <span class="checkbox_item">
            <input type="checkbox" class="fpCheckBox" name="sampleSource" value="${listItem.dbId}" ${checkedBoxes?.sampleSource?.contains(listItem.dbId) ? "checked" : ""}/>
            <span>${listItem.uiLabel} <span class="cat_number">(<g:formatNumber number="${listItem.nItems}" type="number" maxFractionDigits="0"/>)</span></span><br/>
            </span>
          </g:each>
        </div>
    </div>
    </g:if>
    <g:if test="${species.items}">
    <div class="filterPanel" id="speciesFilterPanel">
      <h4>${species.uiLabel}</h4>
        <div class="fpFieldset">
          <g:each in="${species.items}" var="listItem">
            <span class="checkbox_item">
            <input type="checkbox" class="fpCheckBox" name="species" value="${listItem.dbId}" ${checkedBoxes?.species?.contains(listItem.dbId) ? "checked" : ""}/>
            <span>${listItem.uiLabel} <span class="cat_number">(<g:formatNumber number="${listItem.nItems}" type="number" maxFractionDigits="0"/>)</span></span><br/>
            </span>
          </g:each>
        </div>
    </div>
    </g:if>    
    <g:if test="${grailsApplication.config.target.audience != 'ITN'}">
    <g:if test="${principalInvestigators.items}">
    <div class="filterPanel" id="principalInvestigatorFilterPanel">
      <h4>${principalInvestigators.uiLabel}</h4>
        <div class="fpFieldset">
          <g:each in="${principalInvestigators.items}" var="listItem">
            <span class="checkbox_item">
            <input type="checkbox" class="fpCheckBox" name="principalInvestigator" value="${listItem.dbId}" ${checkedBoxes?.principalInvestigator?.contains(listItem.dbId) ? "checked" : ""} onclick="execFilter();"/>
            <span>${listItem.uiLabel} <span class="cat_number">(<g:formatNumber number="${listItem.nItems}" type="number" maxFractionDigits="0"/>)</span></span><br/>
            </span>
          </g:each>
        </div>
    </div>
    </g:if>
    </g:if>
    <g:if test="${platforms.items}">
    <div class="filterPanel" id="platformFilterPanel">
      <h4>${platforms.uiLabel}</h4>
        <div class="fpFieldset">
          <g:each in="${platforms.items}" var="listItem">
          <span class="checkbox_item">
            <input type="checkbox" class="fpCheckBox" name="platform" value="${listItem.dbLabel}" ${checkedBoxes?.platform?.contains(listItem.dbLabel) ? "checked" : ""}/>
            <span>${listItem.uiLabel} <span class="cat_number">(<g:formatNumber number="${listItem.nItems}" type="number" maxFractionDigits="0"/>)</span></span><br/>
          </span>
          </g:each>
        </div>
    </div>
    </g:if>
    <g:if test="${institutions.items}">
    <div class="filterPanel" id="institutionFilterPanel">
      <h4>${institutions.uiLabel}</h4>
        <div class="fpFieldset">
          <g:each in="${institutions.items}" var="listItem">
            <span class="checkbox_item">
            <input type="checkbox" class="fpCheckBox" name="institution" value="${listItem.dbId}" ${checkedBoxes?.institution?.contains(listItem.dbId) ? "checked" : ""}/>
            <span>${listItem.uiLabel} <span class="cat_number">(<g:formatNumber number="${listItem.nItems}" type="number" maxFractionDigits="0"/>)</span></span><br/>
            </span>
          </g:each>
        </div>
    </div>
    </g:if>
  </g:form>
  </div>

  <div class="content-menu ${params.filterPanelShow ? "withFilter" : ""}">
  <div class="page-header">
  <g:if test="${grailsApplication.config.grails.serverURL.contains('immuneprofiling')}"><h2>HIPC Innate Immune Profiling Compendium</h2></g:if>
  <g:else>
    <h2>Gene Expression Browser ${params.searchResults ? "<small style=\"color:#666666\">- Showing <i>"+sampleSetList.size()+"</i> result" + (sampleSetList.size() == 1 ? "" : "s") + " for <i><strong>"+abbreviate([maxLength: "50", value: params.searchResults])+"</strong></i></small>" : ""}</h2>
  </g:else>
  </div>
    <g:if test="${sampleSetList}">
    <table id="samplesets" class="zebra-striped pretty-table">
      <thead>
      <tr>
      	<g:if test="${grailsApplication.config.target.audience.contains('BIIR')}">
	      <th class="{'sorter':'biirTitleSort'}">Sample Set</th>
	    </g:if>
	    <g:else>
	      <th>Sample Set</th>
	    </g:else>
        <g:if test="${params.briInternal}">
          <th>Project</th>
        </g:if>
        <th>Platform</th>
        <th>Species</th>
        <th>Disease</th>
        <g:if test="${params.briInternal}">
          <th>Run Date</th>
        </g:if>
        <th>Sample Source</th>
        <th>Sample Count</th>
        <g:if test="${params.briInternal}">
          <th>Status</th>
        </g:if>
        <th class="icons">&nbsp;</th>
      </tr>
      </thead>
      <tbody>
      <g:each in="${sampleSetList}" status="i" var="ss">
        <tr>
          <td>
          		<g:if test="${dataResults?.get(ss.sampleSet.id)}">
          			<g:link action="show" id="${ss.sampleSet.id}" params="[rankList: dataResults.get(ss.sampleSet.id).rlid, currentQuery: params.significantGeneSearch]">${ss.sampleSet.name}</g:link>
          			<br/>
          			<span style="font-size: x-small;">${dataResults.get(ss.sampleSet.id).rlname}; value: ${dataResults.get(ss.sampleSet.id).value}</span>
          		</g:if>
          		<g:else>
          		<g:link action="show" id="${ss.sampleSet.id}">${ss.sampleSet.name}</g:link>
          		</g:else>
          </td>
          <g:if test="${params.briInternal}">
            <td><span title="${ss.tg2Info?.project}"><g:abbreviate value="${ss.tg2Info?.project}" maxLength="25"/></span></td>
          </g:if>
          <td><g:iconizePlatform platform="${ss.sampleSet.sampleSetPlatformInfo?.platformOption}"/></td>
          <td><span title="${ss.sampleSet.sampleSetSampleInfo?.species?.latin}">
            <g:abbreviate value="${ss.sampleSet.sampleSetSampleInfo?.species?.latin}" maxLength="25"/></span></td>
          <td><span title="${ss.sampleSet.sampleSetSampleInfo?.disease?.name}"><g:abbreviate maxLength="25" value="${ss.sampleSet.sampleSetSampleInfo?.disease?.name}"/></span></td>
          <g:if test="${params.briInternal}">
            <td><g:formatDate date="${ss.tg2Info?.run_date}" format="yyyy-MM-dd"/></td>
          </g:if>
          %{--<td>${sampleSet.sampleSetSampleInfo?.sampleSources}</td>--}%
          <td><span title="<g:join in="${ss.sampleSet.sampleSetSampleInfo?.sampleSources?.name}" delimiter=", "/>">
            <g:abbreviate maxLength="25" value="${ss.sampleSet.sampleSetSampleInfo?.sampleSources?.name}"/></span></td>
          <td>${ss.numSamples ?: ""}</td>
          <g:if test="${params.briInternal}">
            <td>${ss.sampleSet.status}</td>
          </g:if>
          <td class="icons">
            <span
              ${ss.genomicDs ? "title='"+ss.genomicDs.name+"'" : "" }
              class='ui-icon-${ss.genomicDs ? ss.genomicDs.iconName : "large-blank" }'></span>
            <g:if test="${ss.sampleSet.clinicalDataSource}">
              <g:link url="${ss.sampleSet.clinicalDataSource.baseUrl}">
                <span title="${ss.sampleSet.clinicalDataSource.baseUrl}" class="ui-icon-${ss.sampleSet.clinicalDataSource.iconName}"></span>
              </g:link>
            </g:if>
            <g:else>
              <span class="ui-icon-small-blank"></span>
            </g:else>
            <g:each in="${SampleSetLink.findAllByVisible(1)}" var="eLink">
              <g:set var="eDataUrl" value="${ss.sampleSet.links?.find { it.linkType == eLink }?.dataUrl}"/>
              <g:if test="${eDataUrl}">
                <g:link url="${eDataUrl}" target="_blank">
                <span class="ui-icon-${eLink.icon}" title="${eLink.displayName}"></span>
                </g:link>
              </g:if>
              <g:else>
                <span class="ui-icon-small-blank"></span>
              </g:else>
            </g:each>
            <span ${SampleSetRole.findBySampleSetId(ss.sampleSet.id) ? "title='Private'" : "" } class='${SampleSetRole.findBySampleSetId(ss.sampleSet.id) ? "ui-icon-private" : "ui-icon-small-blank" }'></span>
            <g:link controller="sampleSet" action="show" id="${ss.sampleSet.id}" params="[tab:'files']" target="_blank">
              <span ${ss.sampleSet.sampleSetFiles ? "title='Files available for this sample set'" : "" } class='${ss.sampleSet.sampleSetFiles ? "ui-icon-file" : "ui-icon-small-blank" }'></span>
            </g:link>
	        </td>
        </tr>
      </g:each>
      </tbody>
      <tfoot>
        <tr>
          <td colspan="7">
            <div id="pager" class="pager">
              <span class="pagination">
                <ul>
                  <li class="prev"><a href="#">&laquo; Previous</a></li>
                  <li class="next"><a href="#">Next &raquo;</a></li>
                </ul>
              </span>
              <div class="samplesets-pages">
                  <span class="view-xy">You're viewing sample sets <strong><span class="startItem"></span> - <span class="endItem"></span></strong> of <strong> ${sampleSetList.size()}</strong>.</span>

                  <span class="view-amount">View</span>
                    <select class="pagesize small">
                        <option value="5">5</option>
                        <option value="10">10</option>
                        <option selected="selected" value="20">20</option>
                        <option value="50">50</option>
                    </select>
                    sample sets per page
              </div>
            </div>
          </td>
        </tr>
      </tfoot>
    </table>
    </g:if>
    <g:else>
      <div class="none-found">No published sample sets found.</div>
    </g:else>
    
    <img src="../images/icons/loading_filter.gif" class="loadingIcon" alt="loading...">
  </div>
  </div>

<g:render template="/common/bugReporter" model="[tool:'GXB']"/>
</body>
</html>