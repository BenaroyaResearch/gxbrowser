<!DOCTYPE html>
<html>

<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

  <title><g:layoutTitle default="Sample Set Annotation Tool"/></title>

  %{--<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.3.0.css')}"/>--}%
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}"/>
    <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_ssat.ico')}" type="image/x-icon" />
  %{--<link rel="stylesheet" href="${resource(dir: 'css', file: 'docs.css')}"/>--}%
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'common.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-bri.css')}"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>

  <g:javascript src="application.js"/>
  <g:javascript src="jquery-min.js"/>
  <g:javascript src="jquery-ui-min.js"/>
  <g:javascript src="common.js"/>
  <g:javascript src="jquery.qtip.min.js"/>
  <g:javascript src="bootstrap-dropdown.js"/>
  <g:javascript src="bootstrap-modal.js"/>

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
<g:render template="/common/header"/>
<g:layoutBody/>
<g:render template="/common/footer"/>
<g:render template="/common/bugReporter" model="[tool:'SampleSet']"/>
</body>

</html>