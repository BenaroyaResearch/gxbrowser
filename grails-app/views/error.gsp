<!DOCTYPE>
<html>
	<head>
		<title>There seems to be a problem with this page</title>
		<meta name="layout" content="main">
         <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_gxb.ico')}" type="image/x-icon" />
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}" type="text/css">
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'common.css')}" type="text/css">
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}" type="text/css">
	<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>
		%{--<link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">--}%
	</head>
	<body>
    <div class="topbar">
		<div class="topbar-inner fill">
			<div class="sampleset-container">
				<h3><a href="${createLink(uri: '/')}">Error</a></h3>
  			</div>
  		</div><!-- /fill -->
	</div><!-- /topbar -->

    <div class="sampleset-container">

	%{--${exception}--}%
	   <p> We have encountered an error. Either there is a problem with the tool, or you may have been trying to find a sampleset that doesn't exist.</p>
	    <p>You can back out of this page using the Back button, or if you know what you were looking for, you can get there via one of the following tools:</p>
	    <ul class="buttonlist">
	       <g:if test="${grailsApplication.config.tools.available.contains('ssat')}">
	        <li><a class="btn primary" href="${createLink(controller: 'sampleSet')}">Sample Set Annotation Tool</a></li>
	       </g:if>
	       <g:if test="${grailsApplication.config.tools.available.contains('gxb')}">
	        <li><a class="btn primary" href="${createLink(controller: 'geneBrowser')}">Gene Expression Browser</a></li>
	       </g:if>
	       <g:if test="${grailsApplication.config.tools.available.contains('mat')}">
	        <li><a class="btn primary" href="${createLink(controller: 'mat')}">Module Analysis Tool (MAT)</a></li>
	       </g:if>
	       <g:if test="${grailsApplication.config.tools.available.contains('wizard')}">
	        <li><a class="btn primary" href="${createLink(controller: 'MATWizard')}">MATWizard</a></li>
	       </g:if>
	    </ul>
    </div>
	</body>
</html>