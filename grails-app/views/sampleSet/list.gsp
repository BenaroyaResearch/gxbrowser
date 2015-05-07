<%@ page import="org.sagres.sampleSet.SampleSetLink; org.sagres.sampleSet.component.LookupListDetail; common.ClinicalDataSource; org.sagres.sampleSet.annotation.SampleSetAnnotation; org.sagres.sampleSet.SampleSet; org.sagres.sampleSet.SampleSetRole" %>
<html>
<head>
  <meta name="layout" content="sampleSetMain"/>

  <title>Sample Set Annotation Tool: Main Listing</title>

  <g:javascript src="jquery.metadata.js"/>
  <g:javascript src="jquery.tablesorter.min.js"/>
  <g:javascript src="jquery.tablesorter.pager.js"/>
  <g:if test="${grailsApplication.config.target.audience.contains('BIIR')}">
    <g:javascript src="jquery.tablesorter.biir.js"/>
  </g:if>
  
  <g:javascript>
    var isIE = $.browser.msie;
    var filterClear = function() {
      	$("#analystSearch").val("");
      	$("input.fpCheckBox").removeAttr('checked');
      	execFilter();
    };
    var execFilter = function() {
      var sampleSetIds = $("a.sample-set").map(function() {
        return this.name;
      }).get();
      $("input#filteredSampleSetList").val(sampleSetIds);
      $("div.content-menu").addClass("filterEmpty").find("h2>small").detach();
      $("#filter-samplesets-form").submit();
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
      $("#analystSearch").autocomplete({
        source: function(request, response) {
          // request.gxb = true;
          $.getJSON(getBase()+"/sampleSet/analysts", request, function(data) {
            var items = [];
            $.each(data, function(key, val) {
              items.push({ label: val.text, value: val.text });
            });
            response(items);
          });
         },
         select: function(event, ui) { 
         	//alert(ui.item.label + " selected")
        	$("#analystSearch").val(ui.item.label);
        	//$("#filter-samplesets-form").submit();
        	execFilter();
       		 },  
        minLength : 0,
        delay     : 150
      });
      $("#analystClear").click(function() {
      	$("#analystSearch").val("");
      	$("#filter-samplesets-form").submit();
      });
       
      <!-- delegate all change events on fpCheckBoxes, to run execFilter -->
      $("#search-filter-panel").delegate("input.fpCheckBox", "change", execFilter);
      
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
<div class="sampleset-container">

  <div id="search-filter-panel" class="well">
    <div class="filter-panel-header"><span name="allClear" class="button tiny" onclick="filterClear();">Clear All Filters</span><span class="button tiny" onclick="toggleFilterPanel()">x</span></div>
    <g:form name="filter-samplesets-form" action="filteredSampleSets">
    <g:hiddenField name="filteredSampleSetList"/>
    <g:hiddenField name="filterPanelShow" value="${false}"/>
    <g:hiddenField name="sampleSetSearch" value="${params.sampleSetSearch}"/>
    <g:hiddenField name="controllerAction" value="list"/>
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
    <g:if test="${principalInvestigators.items}">
    <div class="filterPanel" id="principalInvestigatorFilterPanel">
      <h4>${principalInvestigators.uiLabel}</h4>
        <div class="fpFieldset">
          <g:each in="${principalInvestigators.items}" var="listItem">
            <span class="checkbox_item">
            <input type="checkbox" class="fpCheckBox" name="principalInvestigator" value="${listItem.dbId}" ${checkedBoxes?.principalInvestigator?.contains(listItem.dbId) ? "checked" : ""}/>
            <span>${listItem.uiLabel} <span class="cat_number">(<g:formatNumber number="${listItem.nItems}" type="number" maxFractionDigits="0"/>)</span></span><br/>
            </span>
          </g:each>
        </div>
    </div>
    </g:if>
    <div class="filterPanel" id="analystFilterPanel">
    	<h4>${analysts.uiLabel}</h4>
    	<div class="fpFieldset">
    		<!-- <g:set var="place" value="${params.analystSearch ? params.analystSearch : 'Search'}" /> -->
        	<input type="text" class="fpInput" name="analystSearch" id="analystSearch" placeholder="Search" value="${params.analystSearch}"/>
        	<g:if test="${params.analystSearch}">
        		<!-- <button style="margin: 5px 0px;" name="analystClear" class="button" value="clear">Clear</button> -->
   			    <button style="margin: 3px 5px 3px 0;" class="button medium" name="analystClear" id="analystClear" value="clear">Clear</button>
        	</g:if>
   		</div>
    </div>
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
  %{--<div style="text-align:center;"><button class="btn small close-filter" onclick="toggleFilterPanel();">Close Filter</button></div>--}%
  </div>
<div class="content-menu ${params.filterPanelShow ? "withFilter" : ""}">
    <div class="page-header">
    <h2>Sample Set Annotation Tool ${params.searchResults ? "<small style=\"color:#666666\">- Showing <i>"+sampleSetList.size()+"</i> result" + (sampleSetList.size() == 1 ? "" : "s") + " for <i><strong>"+abbreviate([maxLength: "50", value: params.searchResults])+"</strong></i></small>" : ""}</h2>
  </div>
    <g:if test="${!sampleSetList.isEmpty()}">
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
	      <g:if test="${params.showTech == '1'}">
	        <th>Technology</th>
		    </g:if>
        <th>Platform</th>
        %{--<th>Disease</th>--}%
        <th>Species</th>
        <th>Disease</th>
        <g:if test="${params.briInternal}">
          <th>Run Date</th>
        </g:if>
        <th>Sample Source</th>
        <th class="samplecount">Sample Count</th>
        %{--<th>Analyst</th>--}%
        <g:if test="${params.briInternal}">
          <th>Status</th>
        </g:if>
	      <th class="icons">&nbsp;</th>
      </tr>
      </thead>
      <tbody>
      <g:each in="${sampleSetList}" status="i" var="ss">
        <tr class=".limited-access">
          <td><g:link action="show" id="${ss.sampleSet.id}" class="sample-set" name="${ss.sampleSet.id}">${ss.sampleSet.name}</g:link></td>
          <g:if test="${params.briInternal}">
            <td><span title="${ss.tg2Info?.project}"><g:abbreviate maxLength="25" value="${ss.tg2Info?.project}"/></span></td>
          </g:if>
		      <g:if test="${params.showTech == '1'}">
			      <td><span title="${ss.sampleSet.sampleSetPlatformInfo?.platform}"><g:abbreviate maxLength="15" value="${ss.sampleSet.sampleSetPlatformInfo?.platform}"/></span></td>
		      </g:if>
          <td><g:iconizePlatform platform="${ss.sampleSet.sampleSetPlatformInfo?.platformOption}"/></td>
          <td><span title="${ss.sampleSet.sampleSetSampleInfo?.species?.latin}"><g:abbreviate maxLength="25" value="${ss.sampleSet.sampleSetSampleInfo?.species?.latin}"/></span></td>
          <td><span title="${ss.sampleSet.sampleSetSampleInfo?.disease?.name}"><g:abbreviate maxLength="25" value="${ss.sampleSet.sampleSetSampleInfo?.disease?.name}"/></span></td>
          <g:if test="${params.briInternal}">
            <td><g:formatDate date="${ss.tg2Info?.run_date}" format="yyyy-MM-dd"/></td>
          </g:if>
          %{--<td>${ss.tg2Info ? ss.tg2Info.cell_population : ss.sampleSet.sampleSetSampleInfo.sampleSource}</td>--}%
          <td>
            <g:if test="${ss.sampleSet.sampleSetSampleInfo}">
              <span title="<g:join in="${ss.sampleSet.sampleSetSampleInfo?.sampleSources?.name}" delimiter=", "/>"><g:abbreviate maxLength="25" value="${ss.sampleSet.sampleSetSampleInfo?.sampleSources?.name}"/></span>
            </g:if>
          </td>
          <td class="samplecount">${ss.numSamples ?: ""}</td>
          %{--<td>${ss.sampleSet.sampleSetAdminInfo.analyst}</td>--}%
          <g:if test="${params.briInternal}">
            <td>${ss.sampleSet.status}</td>
          </g:if>
	        <td class="icons">
            <span ${ss.genomicDs ? "title='"+ss.genomicDs.name+"'" : ''} class='ui-icon-${ss.genomicDs ? ss.genomicDs.iconName : "large-blank" }'></span>
            <g:if test="${ss.sampleSet.clinicalDataSource}">
              <g:set var="dataurl" value="${ss.sampleSet.clinicalDatasourceDataUrl ?: ss.sampleSet.clinicalDataSource.baseUrl}"/>
              <g:link url="${dataurl}">
                <span title="${dataurl}" class="ui-icon-${ss.sampleSet.clinicalDataSource.iconName}"></span>
              </g:link>
            </g:if>
            <g:else>
              <g:if test="${ss.sampleSet.clinicalDatasourceDataUrl}">
                <g:link url="${ss.sampleSet.clinicalDatasourceDataUrl}">
                  <span title="${ss.sampleSet.clinicalDatasourceDataUrl}" class="ui-icon-link"></span>
                </g:link>
              </g:if>
              <g:else>
                <span class="ui-icon-small-blank"></span>
              </g:else>
            </g:else>
            <g:if test="${ss.sampleSet.gxbPublished == 1}">
              <g:link controller="geneBrowser" action="show" id="${ss.sampleSet.id}" target="_blank">
                <span title="Gene expression browser" class="ui-icon-gxb"></span>
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
            %{--<span class='${ss.datasetType == "GEO" ? "ui-icon-blank-note" : "ui-icon-note" }'></span>--}%
            <span ${SampleSetRole.findBySampleSetId(ss.sampleSet.id) ? "title='Private'" : "" } class='${SampleSetRole.findBySampleSetId(ss.sampleSet.id) ? "ui-icon-private" : "ui-icon-small-blank" }'></span>
            <g:link controller="sampleSet" action="show" id="${ss.sampleSet.id}" params="[tab:'files']">
              <span ${ss.sampleSet.sampleSetFiles ? "title='Files available for this sample set'" : "" } class='${ss.sampleSet.sampleSetFiles ? "ui-icon-file" : "ui-icon-small-blank" }'></span>
            </g:link>
            %{--<sec:ifAllGranted roles="ROLE_ADMIN">--}%
              %{--<g:link controller="sampleSet" action="delete" id="${ss.sampleSet.id}"><span class="ui-icon-delete"></span></g:link>--}%
            %{--</sec:ifAllGranted>--}%
	        </td>
        </tr>
      </g:each>
      </tbody>
      <tfoot>
        <g:set var="spanColumns" value="${params.showTech ? 8 : 7}"/>
        <g:set var="spanColumns" value="${spanColumns += params.briInternal ? 3 : 0}"/>
        <tr><td colspan="${spanColumns}">
          <div id="pager" class="pager">
      <span class="pagination">
        <ul>
          <li class="prev"><a href="#">&laquo; Previous</a></li>
          <li class="next"><a href="#">Next &raquo;</a></li>
        </ul>
      </span>
       <div class="samplesets-pages">
           <span class="view-xy">You're viewing sample sets <strong><span class="startItem">x</span> - <span class="endItem">y</span></strong> of <strong> ${sampleSetList.size()}</strong>.</span>
           <span class="view-amount">
            View
          </span>
      <select class="pagesize small">
        <option value="5">5</option>
        <option value="10">10</option>
        <option selected="selected" value="20">20</option>
        <option value="50">50</option>
      </select>
         sample sets per page
      </div>
    </div></td></tr>
      </tfoot>
    </table>
    </g:if>
    <g:else>
      <div class="none-found">No sample sets found.</div>
    </g:else>

    <img src="../images/icons/loading_filter.gif" class="loadingIcon" alt="loading...">
   </div>
  </div>

  <sec:ifLoggedIn>
    <g:render template="saveFilter" model="['savedFilters':savedFilters]"/>
    <g:render template="loadFilter" model="['savedFilters':savedFilters]"/>
    <g:render template="saveCollection" model="['savedCollections':savedCollections]"/>
  </sec:ifLoggedIn>

</body>
</html>