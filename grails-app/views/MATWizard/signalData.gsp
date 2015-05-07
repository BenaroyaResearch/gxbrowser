<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
</head>

<body>
<div class="mat-container">
	<h1>Module Analysis Wizard</h1>
		<h2>Upload Signal Data: Verify Your Data</h2>
			<form action="${createLink(action: 'saveModuleGeneration')}" method="post" class="mat-mainform" name="wizardForm">
			<div class="mat-upload-form">
				<g:hiddenField name="id" value="${matWizardInstance.id}"/>
				<g:hiddenField name="step" value="2"/>

				<p class="last">
				<g:if test="${importSuccess}">
			In order to make sure that we do this correctly please verify that we were able to understand the data that you have given us. After taking an initial look at your data this is what we found:
				</g:if>
				<g:else>
			Unfortunately, the file you uploaded doesn't appear to match with the chip type selected. Please try again.
				</g:else>

				</p>
					<p>
							<label class="nopad">Signal Data:</label> ${matWizardInstance?.signalData?: "Missing"}
				</p>
				<p>
					<label class="nopad">File type:</label> ${matWizardInstance?.fileType?:"Missing"}
				</p>
				<g:if test="${chipType != null && chipType?.name.length() > 0}">
					<p><label class="nopad">Chip Type:</label> ${chipType.name}</p>
				</g:if>
				<p>
					<label class="nopad">Number of samples:</label> ${matWizardInstance?.sampleCount?:"Missing"}
				</p>
				<p>
					<label class="nopad">Grouping/Annotation:</label> ${matWizardInstance?.annotationInfo?: "Missing"}
				</p>
				<g:if test="${matWizardInstance.getAnnotationFileRequired()}">
				<p>
					<label class="nopad">Annotation File:</label> ${matWizardInstance?.annotationFile?: "Missing"}
				</p>
				</g:if>
				<div class="prevnext">
          <g:link action="uploadSignalData" id="${matWizardInstance.id}" class="btn small primary prev">Back</g:link>
					<g:if test="${importSuccess}">
						<g:submitButton name="Next" value="Next" class="btn small primary next"/>
					</g:if>
				</div>
			</div>
			</form>
	</div>
</body>
</html>
