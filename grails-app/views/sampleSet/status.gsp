<%@ page import="org.sagres.sampleSet.annotation.SampleSetAdminInfo; org.sagres.sampleSet.SampleSetLink; org.sagres.sampleSet.annotation.SampleSetFile; org.sagres.sampleSet.component.LookupListDetail; common.ClinicalDataSource; org.sagres.sampleSet.annotation.SampleSetAnnotation; org.sagres.sampleSet.SampleSet" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>

  <title>Sample Set Status Dashboard</title>

  <g:javascript src="jquery.qtip.min.js"/>
  <g:javascript src="jquery.tablesorter.min.js"/>
  <g:javascript src="jquery.tablesorter.pager.js"/>
  
  <g:javascript>
  	var isIE = $.browser.msie;
  	var filterClear = function() {
  		$("#sliderMin").val("0");
  		$("#sliderMax").val("100");
      	$("#analystSearch").val("");
      	$("#dateSearch").val("");
      	$("input.fpCheckBox").removeAttr('checked');
      	execFilter();
    };
  	var execFilter = function() {
      var sampleSetIds = $("a.sample-set").map(function() {
        return this.name;
      }).get();
      $("input#filteredSampleSetList").val(sampleSetIds);
      $("div.content-menu").addClass("filterEmpty").find("h2>small").detach();
      $("#filter-statussets-form").submit();
    };
    var textExtractor = function(node) {
        // var childNode = node.childNodes[0];
        var childNode = node.getElementsByTagName("span")[0];
        if (! childNode)
        {
        	childNode = node.getElementsByTagName("a")[0];
        }
        if (childNode)
        {
          var nodeName = childNode.nodeName;
          if (nodeName === "SPAN")
          {
            return childNode.title;
          }
           else if (nodeName === "A")
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
    function sliderFilter(min) {
    	execFilter();
    };
    
    $(document).ready(function() {
      $("table#samplesets").tablesorter({
        sortList: [[0,0]],
        textExtraction: textExtractor, widthFixed: true
      }).tablesorterPager({container: $("#pager"), size: 20});
      
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
        	execFilter();
        	//$("#filter-statussets-form").submit();
       		 },  
        minLength : 0,
        delay     : 150
      });
      $("#analystClear").click(function() {
      	$("#analystSearch").val("");
      	execFilter();
      });
	  $("#dateClear").click(function() {
	 	$("#dateSearch").val("");
      	execFilter();
	  });
	  <!-- need to use class input-append to capture the CSS as well -->
      $(".ui-icon-date").click(function() {
        var textField = $(this).closest(".input-append").find("input:text");
        if (!textField.is(":disabled"))
       {
        textField.datepicker("show");
        textField.datepicker("widget").position({
          my: "left top",
          at: "left bottom",
          of: textField
        });
      }
    });
    $('.datepicker').datepicker({
      changeMonth: true,
      changeYear: true,
      dateFormat: 'yy-mm-dd'
     }).focus(function() {
      $('.datepicker').datepicker("widget").position({
        my: "left top",
        at: "left bottom",
        of: this
      });
    });
    <!-- need to use slider-range-max to capture the CSS as well -->
    $("#slider-range-max").slider({
       range: true,
       min: 0,
       max: 100,
       values: [${params.sliderMin ?: 0}, ${params.sliderMax ?: 100}],
       slide: function(event, ui) {
              $("span#floor-value").html(ui.values[0]);
              $("span#ceiling-value").html(ui.values[1]);
       },
       stop: function(event, ui) {
           $("input#sliderMin").val(ui.values[0]);
           $("input#sliderMax").val(ui.values[1]);
           execFilter();
       }
    });
    <!-- This modifies the whole class of complete checkboxes -->
    //$(".completeInput").change(function() {
    //$("table").delegate("input.completeInput", "click", function(){ alert("Goodbye!"); });
    
    $("table").delegate("input.completeInput", "change", function() {
    	// alert("completeInput Change!");
    	var boxState = 0;
    	if($(this).is(":checked")) {
    		boxState = 1;
     	}
        $.ajax({
            url: getBase() + '/sampleSet/toggleCompleted',
            type: 'POST',
            data: { ssId: $(this).attr("id"), state: boxState },
            success: function() {
                var lastPart = window.location.href.split("/").pop();
                if (lastPart.substring(0, 18) == "filteredStatusSets") {
                   	$("#filter-statussets-form").submit();
                    // execFilter();
                } else {
                    window.location.reload();
				}                     
       		}
        });
    });
    <!-- delegate all change events on fpCheckBoxes, to run execFilter -->
    $("#search-filter-panel").delegate("input.fpCheckBox", "change", execFilter);
  });
  </g:javascript>
  <style type="text/css">
    a#show-filters {
      display: inline;
    }
    th.header {
    	white-space: nowrap;
    	/* background-position: center right; /* - doesn't work */
    }
   	td {
    	text-align: center; /* adjust the sampleSet column via inline */
    }
  </style>

</head>

<body>

<div class="sampleset-container">

  <!--  Filter Panel -->
  <div id="search-filter-panel" style="${params.filterPanelShow ? "" : "display:none;"}" class="well">
    <div class="filter-panel-header"><span name="allClear" class="button tiny" onclick="filterClear();">Clear All Filters</span><span class="button tiny close-filter" onclick="toggleFilterPanel()">x</span></div>
    <g:form name="filter-statussets-form" action="filteredStatusSets">
    <g:hiddenField name="filteredSampleSetList"/>
    <g:hiddenField name="filterPanelShow" value="${false}"/>
    <g:hiddenField name="sampleSetSearch" value="${params.sampleSetSearch}"/>
    <g:hiddenField name="controllerAction" value="status"/>

	<div class="filterPanel" id="sliderFilterPanel"> 
		<h4>Annotations Completed</h4>
		<div class="fpFieldset">
			<div class="indView" id="filter-slider">
      			<div class="filter-slider-caption">Completeness between <span id="floor-value">${params.sliderMin ?: 0}</span>% and <span id="ceiling-value">${params.sliderMax ?: 100}</span>%</div>
      			<div class="max-slider min-max-slider">
            		<div id="slider-range-max" class="slider-range-max"></div>
      			</div>
      			<g:hiddenField name="sliderMin" id="sliderMin" value="${params.sliderMin}"/>
      			<g:hiddenField name="sliderMax" id="sliderMax" value="${params.sliderMax}"/>
    		</div>
    	</div>
    </div>
	
	<!-- see jQuery datepicker above, uses datepicker/date_input to update load date field -->
	<div class="filterPanel" id="loadDateFilterPanel">
		<h4>Load Date</h4>
		<div class="fpFieldset">
		  <div class="input-append">
		     <!-- <g:set var="dplace" value="${params.dateSearch ?: 'Date'}" /> -->
		     <!-- <input type="text" name="dateSearch" class="datepicker small" id="dateSearch" value="" placeholder="${dplace}"/> -->
   		     <input type="text" name="dateSearch" class="datepicker small" style="width:180px;" id="dateSearch" placeholder="Date" value="${params.dateSearch}"/>
		     <label class="add-on">
               <span class="ui-icon-date"></span>
             </label>
		  </div>
        	 <g:if test="${params.dateSearch}">
        	 	<div>
        			<!--  <button style="margin: 2px 5px;" name="dateClear" class="button medium" id="dateClear" value="clear">Clear</button> -->
       			    <button style="margin: 3px 5px 3px 0;" class="button medium" name="dateClear" id="dateClear" value="clear">Clear</button>
        	 	</div>
        	 </g:if>
		</div>
	</div>
	
     <!-- exec takes place in autocomplete submit -->
    <div class="filterPanel" id="analystFilterPanel">
    	<h4>${analysts.uiLabel}</h4>
    	<div class="fpFieldset">
    		<!-- <g:set var="aplace" value="${params.analystSearch ?: 'Search'}" /> --?
        	<!-- <input name="analystSearch" id="analystSearch" type="text" placeholder="${aplace}"/> -->
        	<input type="text" name="analystSearch" id="analystSearch" placeholder="Search" value="${params.analystSearch}" onclick="this.value =''"/>
        	<g:if test="${params.analystSearch}">
        		<!--  <button style="margin: 5px 0px;" name="analystClear" id="analystClear" class="button medium" value="clear">Clear</button> -->
        		<button style="margin: 5px 0px;" class="button medium" name="analystClear" id="analystClear" value="clear">Clear</button>
        	</g:if>
   		</div>
    </div>

    
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
  <!--  Beginning of main page -->
<div class="content-menu withFilter">
    <div class="page-header">
    <h2>Sample Set Status ${params.searchResults ? "<small style=\"color:#666666\">- Showing <i>"+sampleSetList.size()+"</i> result" + (sampleSetList.size() == 1 ? "" : "s") + " for <i><strong>"+abbreviate([maxLength: "50", value: params.searchResults])+"</strong></i></small>" : ""}</h2>
  </div>
    <g:if test="${!sampleSetList.isEmpty()}">
    <table id="samplesets" class="zebra-striped tablesorter pretty-table">
      <thead>
      <tr>
		<th>Sample Set</th>
		<th>Load Date</th>
		<th>Analyst</th>
		<th>Name</th>
		<th>Design</th>
		<th>Info</th>
		<th>Admin</th>
		<th>Files</th>
		<th>Group Sets</th>
		<th>Rank List</th>
		<th>Analyses</th>
		<th>Modules</th>
		<th>GXB</th>
		<th>Status</th>
		<th>Completed</th>
      </tr>
      </thead>
      <!-- span titles are used in jquery dynamic table sorting -->
      <tbody>
      <g:each in="${sampleSetList}" status="i" var="ss">
      <g:set var="counter" value="${0}" />
        <tr>
          <!-- Sample Set:String -->
          <td style="text-align: left;"><g:link action="show" id="${ss.sampleSet.id}" class="sample-set" name="${ss.sampleSet.id}">${ss.sampleSet.name}</g:link></td>
          <!-- Date Loaded, not counted  -->
          <td><g:formatDate date="${ss.statusDetails?.dateEnded.get(0)}" format="yyyy-MM-dd"/></td>
          <!--  Contact:String -->
          <td><g:if test="${ss.sampleSet?.sampleSetAdminInfo?.analyst}">
          		<span title="${ss.sampleSet?.sampleSetAdminInfo?.analyst}">${ss.sampleSet?.sampleSetAdminInfo?.analyst}</span>
          	  </g:if>
          	  <g:else>
          		<span title="">- -</span>
          	  </g:else>
          </td>
          <!-- Name:check/dash -->
          <td><g:if test="${ss.statusDetails.statusName.get(0) == 1}">
          		<span class="check" title="${1}"></span>
          	  </g:if>
          	  <g:else>
          	  	<span title="${0}">- -</span>
          	  </g:else>
          </td>
          <!-- Design:check/percentage -->
          <td>
           	<g:set var="dvalue" value="${ss.statusDetails.statusDesign.get(0)}"/>
          	<g:if test="${dvalue == 1}">
			    <span class="check" title="${dvalue}"></span>
          	</g:if>
          	<g:else>
           		<span title="${dvalue}"><g:formatNumber number="${dvalue}" type="percent" maxFractionDigits="0" /></span>
            </g:else>
		  </td>
          <!-- Info:check/percentage -->
          <td>
          	<g:set var="ivalue" value="${ss.statusDetails.statusInfo.get(0)}" />
           	<g:if test="${ivalue == 1}">
			    <span class="check" title="${ivalue}"></span>
          	</g:if>
          	<g:else>
           		<span title="${ivalue}"><g:formatNumber number="${ivalue}" type="percent" maxFractionDigits="0" /></span>
            </g:else>
          </td>
          <!-- Admin check/dash -->
          <td>
            <g:set var="avalue" value="${ss.statusDetails.statusAdmin.get(0)}" />
           	<g:if test="${avalue == 1}">
			    <span class="check" title="${avalue}"></span>
          	</g:if>
          	<g:else>
           		<span title="${avalue}"><g:formatNumber number="${avalue}" type="percent" maxFractionDigits="0" /></span>
            </g:else>
          </td>
          <!-- Files check/dash -->
          <td>
          	  <g:if test="${ss.statusDetails.statusFile.get(0) == 1}">
          		<span class="check" title="${1}"></span>
          	  </g:if>
          	  <g:else>
          		<span title="${0}">- -</span>
          	  </g:else>

          </td>
         <!-- GroupSets:check/dash -->
          <td><g:if test="${ss.statusDetails.statusGroup.get(0) == 1}">
          		<span class="check" title="${1}"></span>
          	  </g:if>
          	  <g:else>
          		<span title="${0}">- -</span>
          	  </g:else>
          </td>
          <!-- RankList:check/dash -->
          <td><g:if test="${ss.statusDetails.statusRank.get(0) == 1}">
          		<span class="check" title="${1}"></span>
          	  </g:if>
          	  <g:else>
          		<span title="${0}">- -</span>
          	  </g:else>
          </td>
          <!--  Analyses -->
          <td>${ss.statusDetails.analyses.get(0)}</td>
          <!-- Modules:check/dash -->
          <td><g:if test="${ss.statusDetails.statusAnalysis.get(0) == 1}">
                <span class="check" title="${1}"></span>
              </g:if>
              <g:else>
                <span title="${0}">- -</span>
              </g:else>
          </td>
          <!-- GXB:check/dash -->
          <td><g:if test="${ss.statusDetails.statusBrowser.get(0) == 1}">
          		<span class="check" title="${1}"></span>
          	  </g:if>
          	  <g:else>
          		<span title="${0}">- -</span>
              </g:else>
          </td>          
          <!-- Status:percentage -->
          <td><g:set var="fraction" value="${ss.statusDetails.fractionComplete.get(0)}" />
              <g:set var="level" value="important" />
          	  <g:if test="${fraction > 0.3}">
          	  	<g:set var="level" value="warning" />
          	  </g:if>
          	  <g:if test="${fraction > 0.75}">
          	  	<g:set var="level" value="success-light" />
          	  </g:if>
              <g:if test="${fraction > 0.99}">
          	  	<g:set var="level" value="success" />
          	  </g:if>
              <span class="label ${level}" title="${fraction}"><g:formatNumber number="${fraction}" type="percent" maxFractionDigits="0" /></span>
          </td>
          <!-- Completed/Approved indicator -->
          <td>
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SETAPPROVAL">
            <span class="checkbox_item" title="${ss.sampleSet.approved}"> <!--  title for sorting -->
            	<!-- See jQuery for complete_input that uses ajax to toggle completed bit in database -->
            	<g:checkBox class="completeInput" name="complete${ss.sampleSet.id}" id="${ss.sampleSet.id}" value="${ss.sampleSet.approved}" />
            </span>
            </sec:ifAnyGranted>
            <sec:ifNotGranted roles="ROLE_ADMIN,ROLE_SETAPPROVAL">
			  <g:if test="${ss.sampleSet.approved == 1}">
          		<span class="completed" title="${1}"></span>
          	  </g:if>
          	  <g:else>
          		<span title="${0}">- -</span>
              </g:else>
            </sec:ifNotGranted>
          </td>
        </tr>
      </g:each>
      </tbody>
      <tfoot>
        <g:set var="spanColumns" value="15"/>
        <tr><td colspan="${spanColumns}">
          <div id="pager" class="pager">
      <span class="pagination">
        <ul>
          <li class="prev"><a href="#">&laquo; Previous</a></li>
          <li class="next"><a href="#">Next &raquo;</a></li>
        </ul>
      </span>
      <div style="padding-top: 5px;">
        <span style="padding-left: 10px;">You're viewing sample sets <strong><span class="startItem">x</span> - <span class="endItem">y</span></strong> of <strong> ${sampleSetList.size()}</strong>.</span>
        <span style="padding-left: 10px;">
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