<%@ page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils" %>
<html>
<head>
	<meta name='layout' content='main'/>
	<title><g:message code="springSecurity.login.title"/></title>
	<style type='text/css' media='screen'>



	</style>
</head>

<body>
<div id='login' class="container row">


	<div class='inner span8'>
		%{--<div class='fheader'><g:message code="springSecurity.login.header"/></div>--}%
    <g:if test='${flash.message}'>
        <div class="alert-message error">
          <p>${flash.message}</p>
        </div>
      </g:if>


    <form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>

      <g:if test="${targetUrlParam}">
        <g:hiddenField name="${SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter}" value="${targetUrlParam}"/>
      </g:if>

      <fieldset>
      <legend><g:message code="springSecurity.login.header"/>
      <g:if test="${grailsApplication.config.dm3.authenticate.labkey }"> with your TrialShare username</g:if>
    </legend>
			<div class="clearfix">
				<label for='username'><g:message code="springSecurity.login.username.label"/>:</label>
        <div class="input">
				  <input type='text' class='text_' name='j_username' id='username'/>
        </div>
			</div>

			<div class="clearfix">
				<label for='password'><g:message code="springSecurity.login.password.label"/>:</label>
        <div class="input">
				  <input type='password' class='text_' name='j_password' id='password'/>
        </div>
			</div>

			<div class="clearfix" id="remember_me_holder">
        <div class="input">
				<input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
				<span><g:message code="springSecurity.login.remember.me.label"/></span>
        </div>
			</div>

      <div class="clearfix">
        <div class="input">
          <button type='submit' id="submit" class="btn primary" value='${message(code: "springSecurity.login.button")}'>Login</button>
          <g:if test="${grailsApplication.config.dm3.authenticate.labkey }">
          <g:link class="btn" onclick="javascript:showForgotPassword();return false;">I forgot my password</g:link>
          </g:if>
          <g:else>
          <g:link controller="secUser" action="forgotPassword" class="btn">I forgot my password</g:link>
          </g:else>
        </div>
      </div>
      </fieldset>
		</form>
	</div>
    </div>
</div>
<g:if test="${grailsApplication.config.dm3.authenticate.labkey }">
<g:render template="forgotPassword"/>
</g:if>
<script type='text/javascript'>
	<!--
	(function() {
		document.forms['loginForm'].elements['j_username'].focus();
	})();
	// -->
</script>
</body>
</html>
