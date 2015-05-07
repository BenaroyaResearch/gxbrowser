<!DOCTYPE>
<html>
<head>
  <title>Application Status</title>
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
			<h3><a href="${createLink(uri: '/')}">Application Status</a></h3>
  </div>
  </div><!-- /fill -->
</div><!-- /topbar -->

<div class="sampleset-container" role="main" style="position: relative; height: 99%;">
  <div class="page-header">
     <h2>Status</h2>
   </div>
    <div class="well">

    <ul class="">
      <li>Application State: <span class="${grailsApplication.config.dm3.status.class.get(applicationStatus)}">${grailsApplication.config.dm3.status.states.get(applicationStatus)  }</span> </li>
			<li>Mongo State: <span class="${grailsApplication.config.dm3.status.class.get(mongoStatus)}">${grailsApplication.config.dm3.status.states.get(mongoStatus)}</span></li>
			<li>MYSQL State: <span class="${grailsApplication.config.dm3.status.class.get(mysqlStatus)}">${grailsApplication.config.dm3.status.states.get(mysqlStatus)}</span></li>
			<li>File System State: <span class="${grailsApplication.config.dm3.status.class.get(fileSystemStatus)}">${grailsApplication.config.dm3.status.states.get(fileSystemStatus)}</span></li>
    </ul>

     <a href="${createLink(uri: '/')}">Return to the landing page</a>

     </div>

</div>

</body>
</html>
