<%@ page import="org.sagres.rankList.RankListParams" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="matmain"/>
	<g:set var="entityName" value="${message(code: 'rankListParams.label', default: 'rankListParams')}" />
	<title><g:message code="default.create.label" args="[entityName]"/></title>
	<script type="text/javascript" src="http://twitter.github.com/bootstrap/1.3.0/bootstrap-tabs.js">
</script>

	<g:javascript>

$(function () {
$('.tabs').tabs();
})

	</g:javascript>

</head>

<body>
<div class="mat-container">
<ul class="breadcrumb">
	<li><a href="${createLink(action:'list')}">Home</a> <span class="divider">/</span></li>
	<li class="active">Create Rank List</li>
</ul>

<h1>Create Rank List</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<g:hasErrors bean="${rankListParams}">
	<div class="errors">
		<g:renderErrors bean="${rankListParams}" as="list"/>
	</div>
</g:hasErrors>
<g:form action="runRankList" class="mat-mainform">
	<g:hiddenField name="id" value="${id}"/>
<div class="ranklist-upload-form">

	<div id="preloaded">
		Please select the comparisons you wish to use in the rank list:
		<div class="list">
			<table class="zebra-striped">
				<thead>
			<tr>
				<th class="">Group</th>
				<g:each in="${groups}" status="i" var="sampleGroup">
					<th class="">${sampleGroup.key}</th>
				</g:each>
			</tr>
			</thead>
			<tbody>
			<g:each in="${groups}" status="i" var="outGroup">
				<g:if test="${i < (groups.size() -1)}">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<th>${outGroup.key}</th>
				<g:each in="${groups}" status="j" var="inGroup">
					<td>
						<g:if test="${j>i}">
							<g:checkBox name="comp_${outGroup.value}_${inGroup.value}"/>
						</g:if>
						<g:if test="${!(j>i)}">
							<input type="checkbox" DISABLED/>
						</g:if>
					</td>
				</g:each>
				</tr>
				</g:if>
			</g:each>
			</tbody>

			</table>
		</div>


	</div>



	<div class="formsection formsection5">

		<input name="Run Analysis" value="Run Analysis" id="Run Analysis" class="btn large primary mat-form-submit"
					 type="submit"/>

	</div><!--end formsection-->
</div>

</div>
</g:form>
</div>
</body>
</html>


