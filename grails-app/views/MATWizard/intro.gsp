<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="MATWizard"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>

</head>

<body>
<div class="mat-container" style="width:760px; margin-left:auto !important; margin-right:auto !important;">


<h1>Module Analysis Wizard</h1>



		<div class="mat-landing">


            <div class="text">
                <p><span class="intro">Welcome</span> to the <strong>Module Analysis Wizard</strong>. It will walk you through the process of creating and viewing plots
                from your signal data. Logged-in users can choose to return to their results; if you are not a registered user,
                it will give you the option to provide your email address and use it to access your plots.</p>

               In order to generate your plots, you'll need the following:
                  <ol>
                     <li>the <strong>signal data file</strong> (for example, we support Illumina Background Subtracted and Affy zip files) </li>
                     <li>information about your <strong>case and control groups</strong>, either in the signal data file, or to be entered via into the wizard interface.</li>
                   </ol>

                    <g:if test="${!grailsApplication.config.target.audience.contains('ITN')}">
                    <p>More information about signal data files can be found in <a href="http://www.biir.net/public_wikis/module_annotation/Main_Page" target="_blank">the module wiki</a> along with additional information about this wizard.</p>
                    </g:if>


             </div><!--end text-->

                  <img src="${resource(dir: 'images', file: 'mat_landing_examples.png')}" alt="sample result images" class="examples" />

            <div style="text-align: center;">
                <a href="${createLink(action:"uploadSignalData")}" class="btn primary large">START Wizard</a>

            </div>
        </div>

 </div>

</body>
</html>
