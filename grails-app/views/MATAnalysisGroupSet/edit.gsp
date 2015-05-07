<%@ page import="org.sagres.mat.MATAnalysisGroup; java.text.SimpleDateFormat; org.sagres.mat.Analysis; org.sagres.mat.MATAnalysisGroupSet" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="matmain"/>
	<g:set var="entityName" value="${message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet')}" />
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css"')}"/>

	<title><g:message code="default.list.label" args="[entityName]"/></title>

</head>

<body>
<div class="mat-container">
   <div class="matcreate-wrapper">
<ul class="breadcrumb">
  <li><a href="${createLink(controller: 'analysis')}">Home</a> <span class="divider">/</span></li>
  <li><a href="${createLink(controller: 'analysis', action: 'create')}">Create Analysis</a> <span class="divider">/</span></li>
  <li class="active">Identify Case and Control Groups</li>
</ul>

 <h1>Identify Case and Control groups</h1>
	<g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${MATAnalysisGroupSetInstance}">
  	<div class="errors">
  		<g:renderErrors bean="${MATAnalysisGroupSetInstance}" as="list" />
		</div>
	</g:hasErrors>
		<g:form method="post" class="mat-mainform">
			 <div class="mat-upload-form">
			  <g:hiddenField name="id" value="${MATAnalysisGroupSetInstance?.id}" />
        <g:hiddenField name="version" value="${MATAnalysisGroupSetInstance?.version}" />
				<g:hiddenField name="analysisId" value="${analysisId}" />
				<g:hiddenField name="sampleSetId" value="${sampleSetId}" />
					<div class="formsection">
						<p style="font-weight:bold">Please specify label to use for the control/treatment group:</p>
						<g:textField name="controlLabel" value="${fieldValue(bean: MATAnalysisGroupSetInstance, field: 'controlLabel')}" />
					</div>
					<div class="formsection">
					 <p style="font-weight:bold">Please specify label to use for the case group:</p>
					 <g:textField name="caseLabel" value="${fieldValue(bean: MATAnalysisGroupSetInstance, field: 'caseLabel')}" />
					</div>
					<div class="formsection">
						<table class=" identify-table zebra-striped">
							<tr>
								<th class="identify-group">Group:</th>
								<th class="identify-label">Case</th>
								<th class="identify-label">Control</th>
								<th class="identify-label">Omitted</th>
							</tr>
				 <g:each var="group" status="i" in="${MATAnalysisGroupSetInstance.groups}">
					 <tr>
						 <td class="identify-group"><label class="nowidth" for="${group.groupSetName}">${group.groupSetName}</label></td>
						 <td class="identify-label"><input type="radio" name="rad_${group.groupSetName.replaceAll(' ','_')}" value="case"></td>
						 <td class="identify-label"><input type="radio" name="rad_${group.groupSetName.replaceAll(' ','_')}" value="control"></td>
						 <td class="identify-label"><input type="radio" name="rad_${group.groupSetName.replaceAll(' ','_')}" value="omitted" checked></td>
					 </tr>
				 </g:each>
					</table>
					</div>

				 <div class="formsection formsection5">
				 <a class="btn small" href="${createLink(controller: 'analysis', action: 'create', params: [datasetName: analysisInstance.datasetName, expressionDataFile: analysisInstance.sampleSetId, dataSetGroups: analysisInstance.dataSetGroups, force: true, id: analysisInstance.id] )}">Change default parameters</a>

				<input type="submit" name="_action_update" value="Run Analysis" class="btn large primary mat-form-submit" /></div>
			</div>
			</g:form>

	  </div>
    </div>
    </body>
</html>