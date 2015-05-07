<!DOCTYPE html>
<html>

  <head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:layoutTitle default="Sample Set Annotation Tool"/></title>
   <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_ssat.ico')}" type="image/x-icon" />
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}"/>
    %{--<link rel="stylesheet" href="${resource(dir: 'css', file: 'docs.css')}"/>--}%
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.qtip.min.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'common.css')}"/>
       %{--<link rel="stylesheet" href="${resource(dir: 'css', file: 'mat.css')}"/>--}%
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'chipsloaded.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>
    <g:javascript src="application.js"/>
    <g:javascript src="jquery-min.js"/>
    <g:javascript src="jquery-ui-min.js"/>
    <g:javascript src="common.js"/>
    <g:javascript src="jquery.qtip.min.js"/>
    <g:javascript src="bootstrap-dropdown.js"/>
    <g:javascript src="bootstrap-modal.js"/>

    <g:layoutHead />
  </head>

  <body>
    <g:render template="/chipsLoaded/header"/>
    <g:layoutBody/>
    <g:render template="/common/footer"/>
   <g:render template="/common/bugReporter" model="[tool:'ChipsLoaded']"/>
  </body>

</html>
