
<%@ page import="org.sagres.rankList.RankListParams" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="matmain" />
			<g:set var="entityName" value="${message(code: 'rankListParams.label', default: 'RankListParams')}" />
				<link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css"')}"/>
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
		<ul class="breadcrumb">
  <li><a href="${createLink(controller: 'rankListParams')}">Home</a> <span class="divider">/</span></li>
  <li><a href="${createLink(controller: 'rankListParams', action:'create')}">Create Rank List</a> <span class="divider">/</span></li>
  <li><a href="${createLink(controller: 'rankListParams')}">View Results</a> <span class="divider">/</span></li>
</ul>

		<div class="mat_sidecol">


	<div class="info well">

	</div><!--end info well-->

		</div><!--end mat_sidecol -->
		<div class="mat_maincol">

		<div class="alert-message block-message success">
		The Generation of a new Rank List <g:if test="${rankListParamsInstance.sampleSetName}">for Data Set <i>${rankListParamsInstance.sampleSetName}</i> </g:if> has been started, and can take a few minutes to run.
		<p/>
		The Group Comparisons that will be used to generate the list are:
			<UL>
			<g:each in="${rankListParamsInstance.comparisons}" var="comp">
				<LI>${comp.groupOneName} vs ${comp.groupTwoName}</LI>
			</g:each>
			</UL>

		</div>





		</div><!--end maincol-->



    </body>
</html>
