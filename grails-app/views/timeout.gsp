<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>

      <link rel="stylesheet" href="${resource(dir: 'css', file: 'gxb.css')}"/>

  <title>
      Timeout
  </title>

  <g:javascript src="jquery-ui-min.js"/>
     <g:javascript src="application.js"/>
  <g:javascript src="jquery-min.js"/>
  <g:javascript src="jquery-ui-min.js"/>
  <g:javascript src="common.js"/>
  <g:javascript src="bootstrap-dropdown.js"/>
  <g:javascript src="bootstrap-modal.js"/>

</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">

    <div class="container">
        <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>
         <h3>
            <a>
                <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Module Analysis Toolkit</g:if>
                <g:else>Systems Immunology Toolkit</g:else>
            </a>
        </h3>


        <ul class="nav secondary-nav">


      </ul>

    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->


<div id='login' class="container row">


	<div class='inner span8'>




    <form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>



      <fieldset>
      <legend>You have been logged out due to inactivity
    </legend>

             <p>
                  You have been logged out due to inactivity. For safety reasons, you will need to log in again in order to continue.</p>
          <p><a href="${createLink(controller: 'login', action: 'auth', params : params)}">Log back in</a></p>

        </div>




      </fieldset>
		</form>
	</div>
    </div>

  </body>
</html>