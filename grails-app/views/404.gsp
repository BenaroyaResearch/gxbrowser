<!DOCTYPE>
<html>
<head>
  <title>Page Not Found</title>
    <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_gxb.ico')}" type="image/x-icon" />
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}" type="text/css">
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'common.css')}" type="text/css">
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}" type="text/css">
	<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>


</head>

<body>

<div class="topbar">
	<div class="topbar-inner fill">
	<div class="sampleset-container">
			<h3><a href="${createLink(uri: '/')}">Page Not found</a></h3>
  </div>
  </div><!-- /fill -->
</div><!-- /topbar -->

<div class="sampleset-container" role="main" style="position: relative; height: 99%;">
  <div class="page-header">

     <h2>The Page You're Looking For Cannot Be Found</h2>
   </div>
    <div class="well fourohfour">
  <p>
    <b>The page you are looking for cannot be found at this moment.</b> The url may be been mistyped, or the page may have moved from its previous location.
  </p>
    <p>
        <b>If you followed a link from an email</b>, please make sure that the link you clicked was complete - sometimes email clients break links that take
        up more than one line. You may need to copy and paste the link into the url bar of your browser.
    </p>
    <p><b>If you know what you were looking for</b>, you can get there via one of the following tools:</p>
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
        or <a href="${createLink(uri: '/')}">Return to the landing page</a>

     </div>


    <div>
        <!--note that this position is for 404 version info only, not for general footer info-->
        <em>GXB Version   <g:include view="builddate.gsp"></g:include></em>
    </div>
</div>

</body>
</html>
