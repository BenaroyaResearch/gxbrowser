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

		function loadAvailableSampleGroups() {
			var e = document.getElementById("expressionDateFile");
			var sampleSetId = e.options[e.selectedIndex].value;
			var url = "${createLink(controller: 'analysis', action: 'getAvailableGroupSets')}?sampleId=".concat(sampleSetId);
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
					var results = JSON.parse(xmlhttp.responseText);
					//var d = document.getElementById("datasetGroup");

					//d.style.display = "";
					var dd = document.getElementById("dataSetGroupSelector");
					for (i=dd.options.length-1;i>=1;i--)
					{
						dd.remove(i);
					}
					for (var key in results.groups) {
						dd.add(new Option(results.groups[key], key), null);
					}
				}
			}
			xmlhttp.open("GET", url, true);
			xmlhttp.send();
		}

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
<g:form action="saveSampleSelection" class="mat-mainform">
<div class="mat-upload-form">

	<div id="preloaded">

		<div class="formsection formsection3">
			<label for="expressionDateFile">Microarray Data:</label>
			<g:select id="expressionDateFile" from="${sampleSets}" optionKey="key" name="sampleSetId"
								optionValue="value" onchange="javascript:loadAvailableSampleGroups();"/>

		</div>

		<div class="formsection formsection4" id="sampleGroupDiv" >
			<label for="dataSetGroupSelector">Select Group:</label>
			<select id="dataSetGroupSelector" name="sampleSetGroupSetId" >
				<option selected="selected" value="-1">Select Sample Group</option></select>

			<div style="padding:5px 0 0 120px">
				(you can also use the <a href="/dm3/sampleSet/">Annotation Tool</a> to create a new one )
			</div>

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


