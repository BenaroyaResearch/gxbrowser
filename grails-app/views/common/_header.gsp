<%@ page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils" %>
<div class="topbar">
  %{--<div class="topbar-inner bri-fill">--}%
  <div class="topbar-inner itn-fill">
    <div class="sampleset-container">
        <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

      <h3><g:link controller="sampleSet" action="list">Sample Set Annotation Tool</g:link></h3>
    %{--<ul class="nav">--}%
    %{--<li><a href="${createLink(uri: '/')}">Home</a></li>--}%
    %{--</ul>--}%
      <!-- respond with the action we were queried from (now both list and status actions under sampleSet) -->
      <g:form name="search-samplesets-form" controller="sampleSet" action="${params.action in ['list', 'status'] ? params.action : 'list'}">
      	<g:set var="place" value="${params.sampleSetSearch ? params.sampleSetSearch : 'Search'}" />
        <input name="sampleSetSearch" id="sampleSetSearch" type="text" placeholder="${place}"/>
        <a id="clear-search" href="#" class="btn small primary topbar-clear" onclick="searchClear();">Clear Search</a>
        <a id="show-filters" href="#" class="btn small primary topbar-btn" onclick="toggleFilterPanel();">Hide Filters</a>
      </g:form>
      <ul class="nav secondary-nav">


          <li class="dropdown" data-dropdown="dropdown">
            <a href="#" class="dropdown-toggle">Tools</a>
            <ul class="dropdown-menu skin-dropdown">
               	<sec:ifLoggedIn>
            	  	<sec:ifAllGranted roles="ROLE_USER">
		                <li><g:link controller="secUser" action="account">My Account</g:link></li>
		                <li class="divider"/>
		                <li><g:link controller="chipsLoaded" action="list" target="_blank">Chips Loaded</g:link></li>
		            </sec:ifAllGranted>
		        </sec:ifLoggedIn>
				<li><g:link controller="geneBrowser" action="list" target="_blank">Gene Expr. Browser</g:link></li>
				<g:if test="${grailsApplication.config.tools.available.contains('mat') }">
                	<li><g:link controller="mat" action="list" target="_blank">Module Analysis Tool</g:link></li>
					%{--<li><g:link controller="MATWizard" action="intro" target="_blank">Module Analysis Wizard</g:link></li>--}%
				</g:if>
				<g:if test="${grailsApplication.config.tools.available.contains('metacat') }">
	                <li><g:link controller="metaCat" action="list" target="_blank">MetaCat Tool</g:link></li>
	            </g:if>
                
                <sec:ifLoggedIn>
              		<sec:ifAllGranted roles="ROLE_USER">
                   		<li><g:link controller="sampleSet" action="status">Sample Set Status</g:link></li>
              		</sec:ifAllGranted>
              	</sec:ifLoggedIn>

              	<sec:ifLoggedIn>
              	<sec:ifAllGranted roles="ROLE_USER">
              	<li class="divider"/>
                <g:if test="${params.controller == 'sampleSet' && (params.action == 'list' || params.action == 'status' ||
        						params.action == 'filteredSampleSets' || params.action == 'filteredStatusSets')}">
                <li><a href="#" data-controls-modal="loadfilter-modal" data-backdrop="static" data-keyboard="true">Load Filter</a></li>
                <li><a href="#" data-controls-modal="savefilter-modal" data-backdrop="static" data-keyboard="true">Save Filter</a></li>
                </g:if>
                <li class="divider"/>
                <li><g:link controller="datasetCollection" action="list">My Collections</g:link></li>
                <g:if test="${params.controller == 'sampleSet' && (params.action == 'list' || params.action == 'status' ||
                				params.action == 'filteredSampleSets' || params.action == 'filteredStatusSets')}">
                  <li><a href="#" data-controls-modal="savecollection-modal" data-backdrop="static" data-keyboard="true">Save As Collection</a></li>
                </g:if>
                <g:if test="${params.controller == 'sampleSet' && params.action == 'show'}">
                  <li><a href="#" data-controls-modal="addtocollection-modal" data-backdrop="static" data-keyboard="true">Add To Collection</a></li>
                </g:if>
              </sec:ifAllGranted>
              <sec:ifAllGranted roles="ROLE_DATA_ADMIN">
                <li class="divider"></li>
                <li><g:link controller="sampleSetLink" action="list">Links</g:link></li>
                <li><g:link controller="sampleSetLink" action="create">New Link</g:link></li>
                <li class="divider"></li>
                <li><g:link controller="lookupList" action="list">Lookup Lists</g:link></li>
                <li><g:link controller="lookupList" action="create">New Lookup List</g:link></li>
                <li class="divider"></li>
                <li><g:link controller="fileTag" action="browse">File Tags</g:link></li>
              </sec:ifAllGranted>
              <sec:ifAllGranted roles="ROLE_ADMIN">
                <li class="divider"></li>
                <li><g:link controller="secUser" action="list">Users</g:link></li>
                <li><g:link controller="secUser" action="create">New User</g:link></li>
              </sec:ifAllGranted>
              </sec:ifLoggedIn>
                <li class="divider"/>
                <li> <li><a href="#" onclick="reportBug(true);">Send Feedback</a></li></li>
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
                <g:hiddenField name="${SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter}" value="${request.requestURL}"/>
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
  </div>
</div>
 <g:if test="${grailsApplication.config.area.login}">
<div class="loggedInArea">
    <div class="sampleset-container">
        <div class="text">
      <sec:ifLoggedIn>
        You're logged in as <span class="loggedInUser"><sec:username/></span> | <g:link controller="logout" params="[ 'lastVisitedPage':(request.forwardURI - request.contextPath) ]">Logout</g:link>
      </sec:ifLoggedIn>
      <sec:ifNotLoggedIn>
        <em>You are not logged in. | <a href="${createLink(controller:'login', params:[ 'lastVisitedPage':(request.forwardURI - request.contextPath) ])}">Log in</a></em>
    </sec:ifNotLoggedIn>
    </div>
    </div>
</div>
</g:if>
<g:javascript>
  $(document).ready(function() {
    $("#sampleSetSearch").autocomplete({
      source: function(request, response) {
        $.getJSON(getBase()+"/sampleSet/titles", request, function(data) {
          var items = [];
          $.each(data, function(key, val) {
            // items.push({ label: val.text, value: val.url });
            items.push({label: val.text, value: request.term, url: val.url });
          });
          response(items);
        });
      },
      select: function(event, ui) {
        $("#sampleSetSearch").val(ui.item.label);
		location.href = getBase()+ui.item.url;
        return false;
	  },
      minLength : 0,
      delay     : 150
    });
  });
  
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

  var closeModal = function(modalId) {
    $("#"+modalId).modal("hide");
  };

  // LOGIN stuff
  var showLoginForm = function() {
    $("div.login-content").parent().toggle();
    $("div.login-content").find("#username").focus();
  };

</g:javascript>
