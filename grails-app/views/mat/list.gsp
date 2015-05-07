<%@ page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'mat.css')}" type="text/css">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'gxb.css')}" type="text/css">
  <title>Module Analysis Tool: Main Listing</title>

  <g:javascript src="jquery-ui-min.js"/>
  <g:javascript src="common.js"/>
  <g:javascript src="jquery.tablesorter.min.js"/>
  <g:javascript src="jquery.tablesorter.pager.js"/>
   <g:javascript src="bootstrap_modal.js"/>

  <g:javascript>
    var filterClear = function() {
      	$("input.fpCheckBox").removeAttr('checked');
      	$("#filter-samplesets-form").submit();
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
    $(document).ready(function() {
      var textExtractor = function(node) {
        var childNode = node.childNodes[0];
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
        return node.textContent;
      };
      $("table#samplesets").tablesorter({
        sortList: [[0,0]],
        textExtraction: textExtractor
      })
      .tablesorterPager({container: $("#pager"), size: 20});

      $("#sampleSetSearch").autocomplete({
        source: function(request, response) {
          request.gxb = true;
          $.getJSON(getBase()+"/mat/titles", request, function(data) {
            var items = [];
            $.each(data, function(key, val) {
              items.push({ label: val.text, value: val.url });
            });
            response(items);
          });
        },
        select: function(event, ui) {
          $("#sampleSetSearch").val(ui.item.label);
          location.href = getBase()+ui.item.value;
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
        $("#filter-samplesets-form").submit();
      });

    });
  </g:javascript>
  <style type="text/css">
    table#samplesets tr td:last-child {
      min-width: 150px;
    }
    a#show-filters {
      display: inline;
    }
  </style>

</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">
    <div class="sampleset-container">
        <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

			<h3><a href="${createLink(controller: 'mat')}" style="display:inline;"><strong>MAT</strong></a></h3>
      <g:form name="search-samplesets-form" action="list" >
        <g:set var="place" value="${params.sampleSetSearch ? params.sampleSetSearch : 'Search'}" />
        <input name="sampleSetSearch" id="sampleSetSearch" type="text" placeholder="${place}" />
        <a id="clear-search" href="#" class="btn small primary topbar-clear" onclick="searchClear();">Clear Search</a>
        <a id="show-filters" href="#" class="btn small primary topbar-btn">Show Filters</a>
      </g:form>

      <ul class="nav secondary-nav">

         <li class="dropdown" data-dropdown="dropdown">
          <a href="#" class="dropdown-toggle">Tools</a>
          <ul class="dropdown-menu skin-dropdown">
               <g:if test="${!grailsApplication.config.target.audience.contains('ITN')}">
                   <li><g:link controller="sampleSet" action="list" target="_blank">Annotation Tool</g:link></li>
               </g:if>
                   <li><g:link controller="geneBrowser" action="list" target="_blank">Gene Expression Browser</g:link></li>
                   %{--<li><g:link controller="MATWizard" action="intro" target="_blank">Module Analysis Wizard</g:link></li>--}%
              <li class="divider"></li>
                 <li><g:link controller="MATWizard" action="intro" target="_blank">Create Analysis</g:link></li>
              <li class="divider"></li>
              <li><a href="#" onclick="reportBug(true);">Send Feedback</a></li>
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

  <div class="content-menu mat-content-menu ${params.filterPanelShow ? "withFilter" : ""}">
  <div class="page-header">
    <h2 style="height:36px;"><span style="display:block;float:left;">MAT ${params.searchResults ? "<small style=\"color:#666666\">- Showing <i>"+matInfo.size()+"</i> results for <i><strong>"+abbreviate([maxLength: "50", value: params.searchResults])+"</strong></i></small>" : ""}</span>
     %{--<a class="btn primary small" href="${createLink(controller: 'MATWizard', action: 'intro')}">Create Analysis in the Wizard</a>--}%
    </h2>
  </div>
    <g:if test="${matInfo}">
    <table id="samplesets" class="zebra-striped pretty-table">
      <thead>
      <tr>
        %{--<th>Module Dataset</th>--}%
        <th>Analysis</th>
        <th>Group Set</th>
        <th>Run Date</th>
        <th>Generation</th>
        <th>Platform</th>
      </tr>
      </thead>
      <tbody>
      <g:each in="${matInfo}" status="i" var="mat">
        <tr>
          <td><g:link url="${createLink(controller: 'analysis', action: 'show', id:mat.id)}" title="${mat.analysis_name}: ${mat.sample_set_name}">${mat.analysis_name}</g:link></td>
          %{--<td><g:link controller="sampleSet" action="show" id="${mat.sample_set_id}">${mat.sample_set_name}</g:link></td>--}%
          <td>${mat.group_set_name}</td>
          <td><g:formatDate date="${mat.run_date}" format="yyyy-MM-dd"/></td>
          <td>${mat.mod_generation.encodeAsOrdinal()}</td>
          <td>${sampleSetInfo.get(mat.sample_set_id)?.platform}</td>
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
                    <span class="view-xy">You're viewing sample sets <strong><span class="startItem">x</span> - <span class="endItem">y</span></strong> of <strong> ${matInfo?.size()}</strong>.</span>

                    <span class="view-amount">View</span>
                <select class="pagesize small">
                  <option value="5">5</option>
                  <option value="10">10</option>
                  <option selected="selected" value="20">20</option>
                  <option value="50">50</option>
                </select>
                datasets per page
              </div>
            </div>
          </td>
        </tr>
      </tfoot>
    </table>
    </g:if>
    <g:else>
      <div class="none-found">No MAT datasets found.</div>
    </g:else>
    <img src="../images/icons/loading_filter.gif" class="loadingIcon" alt="loading...">
  </div>
  </div>

<g:render template="/common/bugReporter" model="[tool:'MAT']"/>

</body>
</html>