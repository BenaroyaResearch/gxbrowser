<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>
     <meta name="DC.title" content="GXBtutorialproject_web" />

    <title>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Immune Tolerance Network Module Analysis Toolkit</g:if>
        <g:else>Systems Immunology Toolkit</g:else>
    </title>

      <g:javascript src="swfobject.js"/>

    <script type="text/javascript">
              swfobject.registerObject("csSWF", "9.0.115", "flash/expressInstall.swf");
    </script>

     <style type="text/css">

            #noUpdate
            {
                margin: 0 auto;
                font-family:Arial, Helvetica, sans-serif;
                font-size: x-small;
                color: #cccccc;
                text-align: left;
                width: 210px;
                height: 200px;
                padding: 40px;
            }
        </style>



</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">
	%{--<div class="topbar-inner bri-fill">--}%
    <div class="container">
       <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>
          <h3>
            <a>
                <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Module Analysis Toolkit</g:if>
                <g:else>Systems Immunology Toolkit</g:else>
            </a>
        </h3>


        <ul class="nav secondary-nav">
        %{--<sec:ifNotLoggedIn>--}%
          %{--<li class="dropdown login-dropdown">--}%
            %{--<a class="faq-question" href="#" class="dropdown-toggle" onclick="showLoginForm();">Login</a>--}%
            %{--<div class="dropdown-menu itn-dropdown login-dropdown">--}%
              %{--<div class="login-content">--}%
                %{--<form action="${request.contextPath}/j_spring_security_check" method="post">--}%
                %{--<label>Username:</label><br/>--}%
                %{--<input type="text" name="j_username" id="username"/>--}%
                %{--<label>Password: </label><br/>--}%
                %{--<input type="password" name="j_password" id="password" class="login-pw"/>--}%
                %{--<g:hiddenField name="${SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter}" value="${request.requestURL}"/>--}%
                %{--<g:link controller="secUser" action="forgotPassword" class="btn small forgot-password">Forgot password</g:link>--}%
                %{--<button class="btn primary login-button" type="submit">Login</button>--}%
                %{--</form>--}%
              %{--</div>--}%
            %{--</div>--}%
          %{--</li>--}%
            %{--<li><g:link controller="login" action="auth" params="['lastVisitedPage':request.requestURL]">Login</g:link></li>--}%
        %{--</sec:ifNotLoggedIn>--}%

      </ul>

    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->



<div class="container">
    <div class="page-header">
        <h2>Tutorials</h2>
    </div>

    <div class="itn-header">

         <a href="#gxbtut"> GXB tutorial</a> <g:if test="${grailsApplication.config.tools.available.contains('mat')}">| <a href="#mattut">MAT tutorial</a></g:if><g:if test="${grailsApplication.config.target.audience.contains('ITN')}">| <a href="#matwiztut">MATWizard tutorial</a>  </g:if>

    </div>



    <a name="gxbtut"></a><h3>GXB tutorial</h3>



    <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">

    <div id="media2" style="padding-bottom:20px;">
            <object id="csSWF2" classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="940" height="542" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,115,0">
                <param name="src" value="flash/gxbtut/GXBtutorialproject_controller.swf"/>
                <param name="bgcolor" value="#1a1a1a"/>
                <param name="quality" value="best"/>
                <param name="allowScriptAccess" value="always"/>
                <param name="allowFullScreen" value="true"/>
                <param name="scale" value="showall"/>
                <param name="flashVars" value="autostart=false#&thumb=flash/gxbtut/FirstFrame.png&thumbscale=45&color=0x1A1A1A,0x1A1A1A"/>
                <embed name="csSWF" src="flash/gxbtut/GXBtutorialproject_controller.swf" width="940" height="542" bgcolor="#1a1a1a" quality="best" allowScriptAccess="always" allowFullScreen="true" scale="showall" flashVars="autostart=false&thumb=flash/gxbtut/FirstFrame.png&thumbscale=45&color=0x1A1A1A,0x1A1A1A" wmode="transparent" pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"></embed>
            </object>
    </div>
    </g:if>
	<g:else>
    <div id="media0" style="padding-bottom:20px;">
            <object id="csSWF0" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="940" height="542" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,115,0">
                <param name="src" value="flash/gxbtut-bri/GXB_BRI_controller.swf"/>
                <param name="bgcolor" value="#1a1a1a"/>
                <param name="quality" value="best"/>
                <param name="allowScriptAccess" value="always"/>
                <param name="allowFullScreen" value="true"/>
                <param name="scale" value="showall"/>
                <param name="flashVars" value="autostart=false#&thumb=flash/gxbtut-bri/FirstFrame.png&thumbscale=45&color=0x1A1A1A,0x1A1A1A"/>
                <embed name="csSWF" src="flash/gxbtut-bri/GXB_BRI_controller.swf" width="940" height="542" bgcolor="#1a1a1a" quality="best" allowScriptAccess="always" allowFullScreen="true" scale="showall" flashVars="autostart=false&thumb=flash/gxbtut-bri/FirstFrame.png&thumbscale=45&color=0x1A1A1A,0x1A1A1A" wmode="transparent" pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"></embed>
            </object>
    </div>
    </g:else>
	<g:if test="${grailsApplication.config.tools.available.contains('mat')}">
    <a name="mattut"></a><h3>MAT tutorial</h3>

          %{--insert here--}%


              <div id="media" style="padding-bottom:20px;">
                <object id="csSWF" classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="940" height="542" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,115,0">
                    <param name="src" value="flash/mattut/MATTutorialv1_controller.swf"/>
                    <param name="bgcolor" value="#1a1a1a"/>
                    <param name="quality" value="best"/>
                    <param name="allowScriptAccess" value="always"/>
                    <param name="allowFullScreen" value="true"/>
                    <param name="scale" value="showall"/>
                    <param name="flashVars" value="autostart=false#&thumb=flash/mattut/FirstFrame.png&thumbscale=45&color=0x000000,0x000000"/>
                    <embed name="csSWF" src="flash/mattut/MATTutorialv1_controller.swf" width="940" height="542" bgcolor="#1a1a1a" quality="best" allowScriptAccess="always" allowFullScreen="true" scale="showall" flashVars="autostart=false&thumb=flash/mattut/FirstFrame.png&thumbscale=45&color=0x000000,0x000000" wmode="transparent" pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"></embed>
                </object>
            </div>

          %{--<div id="media">    full version--}%
                %{--<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="940" height="542" id="csSWF"  style="z-index:2;">--}%
                    %{--<param name="movie" value="flash/mattut/MATTutorialv1_controller.swf" />--}%
                    %{--<param name="quality" value="best" />--}%
                    %{--<param name="bgcolor" value="#fff" />--}%
                    %{--<param name="allowfullscreen" value="true" />--}%
                    %{--<param name="scale" value="showall" />--}%
                    %{--<param name="allowscriptaccess" value="always" />--}%
                    %{--<param name="flashvars" value="autostart=false&thumb=flash/mattut/FirstFrame.png&thumbscale=70&color=0x000000,0x000000" />--}%
                    %{--<!--[if !IE]>-->--}%
                    %{--<object type="application/x-shockwave-flash" data="flash/mattut/MATTutorialv1_controller.swf" width="940" height="542">--}%
                        %{--<param name="quality" value="best" />--}%
                        %{--<param name="bgcolor" value="#fff" />--}%
                        %{--<param name="allowfullscreen" value="true" />--}%
                        %{--<param name="scale" value="showall" />--}%
                        %{--<param name="allowscriptaccess" value="always" />--}%
                        %{--<param name="flashvars" value="autostart=false&thumb=flash/mattut/FirstFrame.png&thumbscale=50&color=0x000000,0x000000" />--}%
                    %{--<!--<![endif]-->--}%
                        %{--<div id="noUpdate">--}%
                            %{--<p>The Camtasia Studio video content presented here requires JavaScript to be enabled and the latest version of the Adobe Flash Player. If you are using a browser with JavaScript disabled please enable it now. Otherwise, please update your version of the free Adobe Flash Player by <a href="http://www.adobe.com/go/getflashplayer">downloading here</a>. </p>--}%
                        %{--</div>--}%
                    %{--<!--[if !IE]>-->--}%
                    %{--</object>--}%
                    %{--<!--<![endif]-->--}%
                %{--</object>--}%
            %{--</div>--}%
		</g:if>


     <g:if test="${grailsApplication.config.tools.available.contains('wizard')}">
    <a name="matwiztut"></a><h3>Module Analysis Wizard tutorial</h3>

         <div id="media3">
            <object id="csSWF3" classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="940" height="542" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,115,0">
                <param name="src" value="flash/matwiztut/MATwizproject_controller.swf"/>
                <param name="bgcolor" value="#1a1a1a"/>
                <param name="quality" value="best"/>
                <param name="allowScriptAccess" value="always"/>
                <param name="allowFullScreen" value="true"/>
                <param name="scale" value="showall"/>
                <param name="flashVars" value="autostart=false#&thumb=flash/matwiztut/FirstFrame.png&thumbscale=45&color=0x1A1A1A,0x1A1A1A"/>
                <embed name="csSWF" src="flash/matwiztut/MATwizproject_controller.swf" width="940" height="542" bgcolor="#1a1a1a" quality="best" allowScriptAccess="always" allowFullScreen="true" scale="showall" flashVars="autostart=false&thumb=flash/matwiztut/FirstFrame.png&thumbscale=45&color=0x1A1A1A,0x1A1A1A" wmode="transparent" pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"></embed>
            </object>
        </div>
    </g:if>
    <div class="itn-footer">

           <a href="landing.gsp">Landing page</a> |  <a href="gxbmore.gsp">About the Gene Expression Browser</a>
           <g:if test="${grailsApplication.config.tools.available.contains('mat')}"> | <a href="matmore.gsp">About the Module Analysis Tool</a></g:if>
           <g:if test="${grailsApplication.config.tools.available.contains('wizard')}"> | <a href="matwizmore.gsp">About the Module Analysis Wizard</a></g:if>

       </div>




   %{--<div id="footer">--}%
       %{--&copy; copyright info and other things.--}%

   %{--</div>--}%
</div>

  </body>
</html>