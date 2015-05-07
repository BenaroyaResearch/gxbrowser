<%@ page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils" %>

<div class="topbar">
  <div class="topbar-inner bri-fill">
    %{--<div class="topbar-inner itn-fill">--}%
      <div class="chipsloaded-container">

          <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

        <h3><g:link controller="chipsLoaded" action="list">Chips Loaded</g:link></h3>
         <g:form controller="chipsLoaded" action="list" >
          <input name="chipsLoadedSearch" id="chipsLoadedSearch" type="text" placeholder="Search" />
          <a id="show-filters" href="#" class="btn small primary topbar-btn" onclick="toggleFilterPanel();">Hide Filters</a>
        </g:form>
        <ul class="nav secondary-nav">
          <sec:ifNotLoggedIn>
            <li>
              <g:link controller="login" action="auth"
                      params="['lastVisitedPage':request.requestURL]">Login
              </g:link>
            </li>
          </sec:ifNotLoggedIn>
          %{--<sec:ifLoggedIn>--}%
            %{--<li><g:link controller="logout">Logout</g:link></li>--}%
            %{--</sec:ifLoggedIn>--}%

            <li class="dropdown">
              <a href="#" class="dropdown-toggle">Tools</a>
              <ul class="dropdown-menu skin-dropdown">
                  <sec:ifLoggedIn>
                <sec:ifAllGranted roles="ROLE_USER">
                  <li>
                    <g:link controller="secUser" action="account">My Profile
                    </g:link>
                  </li>
                </sec:ifAllGranted>
                <sec:ifAllGranted roles="ROLE_ADMIN">
                  <li class="divider"></li>
                  <li>
                    <g:link controller="secUser" action="list">Users
                    </g:link>
                  </li>
                  <li>
                    <g:link controller="secUser" action="create">New User
                    </g:link>
                  </li>
                </sec:ifAllGranted>


                <li class="divider"></li>
                <li> <g:link action="showUploadExpressionDataForm">Upload Expression Data </g:link></li>
		        <li><g:link action="showUploadBriRnaSeqFilesForm">Load BRI RNA-seq Files</g:link></li>
		        <li><g:link action="showUploadFocusedArrayDataForm">Load Focused-Array Data</g:link></li>
                <li class="divider"></li>
              </sec:ifLoggedIn>

                        <li><a href="#" onclick="reportBug(true);">Send Feedback</a></li>
              </ul>
            </li>

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
    $("body").bind("click", function (e) {
      $('.dropdown-toggle, .menu').parent("li").removeClass("open");
    });
    $(".dropdown-toggle, .menu").click(function (e) {
      var $li = $(this).parent("li").toggleClass('open');
      return false;
    });
    $("#chipsLoadedSearch").autocomplete({
      source: function(request, response) {
        $.getJSON(getBase()+"/chipsLoaded/filenames", request, function(data) {
          var items = [];
          $.each(data, function(key, val) {
            items.push({ label: val.text, value: val.url });
          });
          response(items);
        });
      },
      select: function(event, ui) {
        $("#chipsLoadedSearch").val(ui.item.label);
			  location.href = getBase()+ui.item.value;
        return false;
			},
      minLength : 0,
      delay     : 150
    });
  });
</g:javascript>
