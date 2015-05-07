<!DOCTYPE html>
<%@ page import="org.sagres.mat.Analysis; org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils" contentType="text/html;charset=UTF-8" %>
<html>
	<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_mat.ico')}" type="image/x-icon" />
    <title><g:layoutTitle default="Grails" /></title>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'mat.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'gxb.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'common.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>
 	<g:javascript src="jquery-min.js"/>
 	
  	
	<g:layoutHead/>

	<script type="text/javascript">

		function toggle(section) {
			section = section || 'displayDefaultParams';
			var ele = document.getElementById(section);
			if (ele.style.display == "block") {
				ele.style.display = "none";
			} else {
				ele.style.display = "block";
			}
		}

		function toggleUpload() {
			var ele = document.getElementById("uploadLabel");
			var eleUpload = document.getElementById("uploadData");
			var eleSelect = document.getElementById("selectData");
			if (ele.innerHTML == "Upload Data") {
				ele.innerHTML = "Use Preloaded Data";
				eleUpload.style.display = "block";
				eleSelect.style.display = "none";
			} else {
				ele.innerHTML = "Upload Data";
				eleUpload.style.display = "none";
				eleSelect.style.display = "block";
			}
		}

		function toggleAll(onOff) {
			toggle("displayDefaultParams");
		}

		function getElementsByClassName(node,classname) {
			  if (node.getElementsByClassName) { // use native implementation if available
			    return node.getElementsByClassName(classname);
			  } else {
			    return (function getElementsByClass(searchClass,node) {
			        if ( node == null )
			          node = document;
			        var classElements = [],
			            els = node.getElementsByTagName("*"),
			            elsLen = els.length,
			            pattern = new RegExp("(^|\\s)"+searchClass+"(\\s|$)"), i, j;

			        for (i = 0, j = 0; i < elsLen; i++) {
			          if ( pattern.test(els[i].className) ) {
			              classElements[j] = els[i];
			              j++;
			          }
			        }
			        return classElements;
			    })(classname, node);
			  }
		}
		function setPublishing(onOff) {
			var elements = getElementsByClassName(document, "matpublish"),
				n = elements.length;
			
		   	for (var i = 0; i < n; i++) {
		    	var e = elements[i];
		     	if(onOff == true) {
		       		e.style.display = 'block';
		     	} else {
		       		e.style.display = 'none';
		     	}
		  	}
		}

		var togglePublish = function(id, newState) {
	        $.ajax({
	            url: getBase() + '/analysis/togglePublished',
	            type: 'POST',
	            data: { matId: id, state: newState },
	            success: function() {
		            if (newState == 1) {
		            	alert("Published!");
		            } else {
		            	alert("Un-published!");
		            }
	              	window.location.reload();
				}                     
	       	});
	     };		
</script>

	<g:if test="${grailsApplication.config.dm3.analytics.account}">
       	<script type="text/javascript">
			var gaAccount    = "${grailsApplication.config.dm3.analytics.account}";
   			var gaRootPath	 = '/' + "${grailsApplication.metadata['app.name']}";
			var gaDomainName = "${grailsApplication.config.dm3.analytics.domain}";
		
   			// Disable tracking if the opt-out cookie exists.
   			var gaDisableStr = 'ga-disable-' + gaAccount;
   			if (document.cookie.indexOf(gaDisableStr + '=true') > -1) {
	       	  	window[gaDisableStr] = true;
   			}

   			// Opt-out function
   			var gaOptout = function() {
	       	  	document.cookie = gaDisableStr + '=true; expires=Thu, 31 Dec 2099 23:59:59 UTC; path=' + gaRootPath + '/';
   	  			window[gaDisableStr] = true;
   	  			if ($('#gaOptout').length) {
   	  				$('#gaOptout').hide();
   	  			}
   	  			alert("Google Analytics has been disabled for this website");
   			}
   	       	
      		var _gaq = _gaq || [];
	      		_gaq.push(['_setAccount', gaAccount]);
  				_gaq.push(['_setDomainName', gaDomainName]);
 // 				_gaq.push(['_setCookiePath', gaRootPath]);			
  				_gaq.push(['_setAllowLinker', true]);
  				_gaq.push(['_trackPageview']);

	  		(function() {
        		var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    				ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    			var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  			})();
    	</script>
    </g:if>

</head>

<body>

<div class="topbar">
	<div class="topbar-inner fill">
		<div class="mat-container">
            <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

			<h3><a href="${createLink(controller: 'mat', action:'list')}"><strong>MAT</strong></a></h3>

			<form action="" onsubmit="return false;">
				<input id="topSearch" placeholder="Search" type="text" class="mat-topsearch">


			</form>
      <g:javascript src="jquery-ui-min.js"/>
      <g:javascript>
        function getBase() {
          var curPath = location.pathname;
          return curPath.substring(0, curPath.indexOf("/",1));
        };
        $(document).ready(function() {
          $("#topSearch").autocomplete({
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
              $("#topSearch").val(ui.item.label);
              location.href = getBase()+ui.item.value;
              return false;
            },
            minLength : 0,
            delay     : 150
          });
        });
      </g:javascript>

        	<%-- <b>params: ${params}<br></b> --%>

			<ul class="nav secondary-nav">
			<g:if test="${grailsApplication.config.googleplus.on}">
      	   		<li><g:link onclick="googlePlusLink(); return false;" class="gplus noshade" title="Post this link to your google+ circles"></g:link></li>
      		</g:if>
			
        <li class="dropdown">
          <a href="#" class="help-dropdown noshade dropdown-toggle" title="Help"></a>
          <ul class="dropdown-menu skin-dropdown">
            <li><g:link controller="moduleDesc" target="_blank">Module Generation Description</g:link></li>
            <li><g:link controller="faq" target="_blank">FAQs</g:link></li>
          </ul>
        </li>
        <g:if test="${params.action in ['show']}">
        <li><a onclick="showModuleParams();return false;" class="info noshade" title="Information"></a></li>
        </g:if>
        %{--<g:if test="${grailsApplication.config.send.email.on}">--}%
        	<%-- FIXME: unset variable used to match redirect after miniURL/view --%>
            <g:if test="${params.action in ['show', 'correlation', 'matPlot', 'notifyJobStarted', 'metaCompare', 'metaScatter', 'metaPCA']}">
         		<li><g:link onclick="generateLink(false,true); return false;" class="email-link noshade" title="Email Link"></g:link></li>
            </g:if>
        %{--</g:if>--}%

				<li class="dropdown">
					<a href="#" class="dropdown-toggle">Tools</a>

					<ul class="dropdown-menu skin-dropdown">
                        %{--<li><a href="" target="_blank" id="GXBLink"></a></li>--}%

           				%{--<li class="divider"></li>--}%
<%--            			<g:if test="${grailsApplication.config.mat.access.on}">--%>
<%--							<li><a href="${createLink(controller: 'analysis', action:'create')}">Run Analysis</a></li>--%>
<%--							<li><a href="${createLink(controller: 'mat', action:'list')}">View Results</a></li>--%>
<%--						</g:if>--%>
						<g:if test="${params.id && params.controller.equals('analysis') && editable}">
							<g:if test="${Analysis.get(params.id)?.matPublished}">
								<li><a href="#" class="matpublish" style="display: none;" onclick="togglePublish(${params.id}, 0);">Un-Publish Results</a></li>
							</g:if>
							<g:else>
								<li><a href="#" class="matpublish" style="display: none;" onclick="togglePublish(${params.id}, 1);">Publish Results</a></li>
							</g:else>
						</g:if>
						<sec:ifAllGranted roles="ROLE_ADMIN">
						<li><a href="${createLink(controller: 'version', action:'list')}">Manage Chip Version</a></li>
						</sec:ifAllGranted>
           <g:if test="${params.action in ['show','correlation', 'matPlot', 'metaCompare', 'metaScatter', 'metaPCA']}">
              <g:if test="${!grailsApplication.config.target.audience.contains('ITN') && params.action == 'show'}">
                <li id="correlation-menu"><a href="${createLink(controller: 'analysis', action:'correlation', id:params.id)}" target="_blank">View Correlations</a></li>
              </g:if>
              <g:if test="${hasGSA && grailsApplication.config.mat.plot.types.contains('gsa')}">
                <li><a id="enable-gsa-option" href="" onclick="enableGSA(this);return false;">Enable GSA Plot</a></li>
              </g:if>
<%--              <g:if test="${params.action == 'correlation' && grailsApplication.config.mat.access.on}">--%>
<%--                <li><a href="${createLink(controller: 'analysis', action:'show', id:params.id)}" target="_blank">View Analysis Plots</a></li>--%>
<%--              </g:if>--%>

              %{--<li class="divider"></li>--}%
              %{--<li><a href="#" onclick="showCorrelationPanel();">View Correlations</a></li>--}%
              <li class="divider"></li>
              %{--<g:if test="${grailsApplication.config.send.email.on}">--}%
                %{--<li><a href="#" onclick="generateLink(true);">Email Link</a></li>--}%
              %{--</g:if>--}%
              <li><a href="#" onclick="generateLink(false);">Copy Link</a></li>
              <li class="divider"></li>
              %{--<g:if test="${grailsApplication.config.send.email.on}">--}%
                %{--<li><a href="#" onclick="reportBug(false);">Report Bug</a></li>--}%
              %{--</g:if>--}%
              %{--<g:else>--}%

              %{--</g:else>--}%
              %{--<li class="divider"></li>--}%
              %{--<li><a href="#" onclick="showNotePanel();">Add Note</a></li>--}%
            </g:if>

               <li><a href="#" onclick="reportBug(true);">Send Feedback</a></li>

				</ul>
				</li>

        <g:if test="${!grailsApplication.config.target.audience.contains('ITN') && params.action in ['show','correlation', 'matPlot', 'metaScatter', 'metaPCA']}">
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



<script type="text/javascript">
	var googlePlusLink = function() 
	{
		var args = generateArgs();
		$.getJSON(getBase()+"/miniURL/create", args, function(json) {
			window.open("https://plus.google.com/share?url=" + json.link, '_blank',
				'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=400,width=400');
			return false;
		});
	};
  var showHelpTips = function() {
    $(".help-tips").qtip("toggle");
  };
  $(document).ready(function() {
		$("body").bind("click", function (e) {
			$('.dropdown-toggle, .menu').parent("li").removeClass("open");
		});
		$(".dropdown-toggle, .menu").click(function (e) {
			var $li = $(this).parent("li").toggleClass('open');
			return false;
		});
	});

  // LOGIN stuff
  var showLoginForm = function() {
    $("div.login-content").parent().toggle();
    $("div.login-content").find("#username").focus();
  };

</script>

 <g:if test="${grailsApplication.config.area.login}">
<g:if test="${params.action in ['show', 'correlation', 'matPlot', 'list', 'create', 'nofityJobStarted']}">
 <div class="loggedInArea">
    <div class="mat-container">
        <div class="text">
      <sec:ifLoggedIn>
<%--      createLink(action:params.action, params:params, absolute:false).encodeAsURL()--%>
        You're logged in as <span class="loggedInUser"><sec:username/></span> | <g:link controller="logout" params="[ 'lastVisitedPage':(request.forwardURI - request.contextPath) ]">Logout</g:link>
      </sec:ifLoggedIn>
      <sec:ifNotLoggedIn>
        <em>You are not logged in. | <a href="${createLink(controller:'login', params:[ 'lastVisitedPage':(request.forwardURI - request.contextPath) ])}">Log in</a></em>
    </sec:ifNotLoggedIn>
    </div>
    </div>
</div>
</g:if>
</g:if>
<g:layoutBody/>

</body></html>
