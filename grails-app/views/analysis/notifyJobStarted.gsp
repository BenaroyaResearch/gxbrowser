
<%@ page import="org.sagres.mat.Analysis" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="matmain" />
        <g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}" />
				<link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css"')}"/>
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

</div>
    <div class="mat-container">

					<g:javascript>
		function checkAnalysisStatus() {
			$.ajax({
				type: "GET",
				url: "${createLink(action:'analysisRunComplete', id:analysisInstance.id)}",
				async: true,
				cache: false,
				timeout: 20000,
				success: function(data) {
					//check status
					if ("Complete" == data.status) {
								window.location="${createLink(action:'list')}";
					   } else {
							setTimeout(
								'checkAnalysisStatus()',
								60000 //1 minute
							);
					}
				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					setTimeout(
						'checkAnalysisStatus()',
						60000 //1 minute
					);
				}
			});
		};


	</g:javascript>


        <div class="matcreate-wrapper">
		<ul class="breadcrumb">
          <li><a href="${createLink(controller: 'mat')}">Home</a> <span class="divider">/</span></li>
          <li><a href="${createLink(controller: 'analysis', action:'create')}">Create Analysis</a> <span class="divider">/</span></li>
          <li><a href="${createLink(controller: 'analysis', action: 'show', id: analysisInstance.id)}">View Results</a> <span class="divider">/</span></li>
          <li class="active">${analysisInstance?.datasetName}</li>
        </ul>

	 <g:javascript>
	$(document).ready(function() {
			checkAnalysisStatus();
		});

</g:javascript>

		<div class="alert-message block-message success">
		The Module Analysis has been started, and can take a while to run.
			<a href="${createLink(controller: 'mat')}">Results will be available
			as soon as the job is completed</a>.
		</div>





         </div>
        </div>
    </body>
</html>
