<!doctype html>

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<title><g:layoutTitle default="Grails"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}" type="text/css">
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'common.css')}"/>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'gxb.css')}"/>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}"/>
		<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>

    	<g:javascript src="jquery-min.js"/>
    	<g:javascript src="jquery-ui-min.js"/>
    	<g:javascript src="common.js"/>
     	<g:javascript src="bootstrap-modal.js"/>
     	<g:javascript src="bootstrap-dropdown.js"/>


		<g:layoutHead/>

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
//  				_gaq.push(['_setCookiePath', gaRootPath]);			
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
        <div class="sampleset-container">
            <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

          <ul class="nav">
            %{--<li><g:link controller="sampleSet" action="list">Sample Set Annotation Tool</g:link></li>--}%
            %{--<li><g:link controller="geneBrowser" action="list">Gene Expression Browser</g:link></li>--}%
            %{--<li><g:link controller="chipsLoaded" action="showUploadExpressionDataForm">Load Expression Data</g:link></li>--}%
            %{--<li><g:link controller="chipsLoaded" action="showUploadSoftFileForm">Load Soft File</g:link></li>--}%
          </ul>
          <ul class="nav secondary-nav">
            <sec:ifNotLoggedIn>
              <li><g:link controller="login" action="auth" params="['lastVisitedPage':(request.forwardURI - request.contextPath)]">Login</g:link></li>
            </sec:ifNotLoggedIn>
            <sec:ifLoggedIn>
              <li><g:link controller="logout">Logout</g:link></li>
            </sec:ifLoggedIn>
          </ul>
        </div>
      </div>
    </div>
     <div class="sampleset-container">
		<g:layoutBody/>
		<g:javascript src="application.js"/>

        <g:render template="/common/footer"/>
       </div>
	</body>
</html>