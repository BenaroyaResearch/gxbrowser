<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="MATWizard"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>

	<title><g:message code="default.create.label" args="[entityName]"/></title>
	<link href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet"
				type="text/css"/>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.5/jquery.min.js"></script>
	<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
</head>

<body>

<g:javascript>

	function transferSamples(fromGroup, toGroup, samples) {
        var detailId = [];
        samples.each(function() {
          detailId.push($(this).attr('id'));
        });
        if (fromGroup.attr('id') != toGroup.attr('id'))
        {
          $.post('${createLink(action: "transferSamples")}',
            {
            	id: "${matWizardInstance.id}",
            	receiveGroup: toGroup.attr('id'), samples: detailId.join(','), fromGroup: fromGroup.attr('id') }
          );
        }
      }

	$(function() {

		$('.connectedSortable').droppable({
			accept			: 'li',
			tolerance	 : 'pointer',
			hoverClass	: 'droppable-hover',
			drop				: function(e, ui) {
				var item = ui.helper;
				var itemParent = ui.draggable.closest('.sortable-list');
				var currentGroup = $(this);
				transferSamples(itemParent, currentGroup, item);
			}
		});

		$(".connectedSortable").sortable({
			connectWith: ".connectedSortable",
			update: function(event, ui) {
			//	alert(ui.item.attr('id') + ':' + ui.item.index() );
			}
		}).disableSelection();


	});



</g:javascript>
<div class="mat-container">
	<h1>Module Analysis Wizard</h1>

	<h2>Select Groups</h2>

	<g:form action="saveGroups" class="mat-mainform">

		<div class="mat-upload-form matform-wide" style="width: ${groups.size() * 270 -30}px;">
			<input type="hidden" name="id" value="${matWizardInstance.id}" id="id"/>
			<input type="hidden" name="step" value="5" id="version"/>
			%{--<g:if test="${reasons.size() > 0}">--}%
				%{--<g:each in="${reasons}" var="reason">--}%
					%{--<div class="alert-message error">${reason}</div>--}%
				%{--</g:each>--}%

			%{--</g:if>--}%



			<p>One last thing before we can start, we need to <strong>organize the samples into groups</strong> for the analysis; this is usually case vs control.
			</p>
			<p>For the groups below identify <strong>case(s), control(s), and which should be ignored</strong>.</p>
			<p>You can move samples between groups by dragging and dropping, or by selecting in one list and using the arrow buttons to move between groups.</p>
			<p class="last">By default we will refer to the groups as case and control, you can change these labels by editing the default values below:</p>
			<label class="nopad">Case label:</label>
			<g:textField name="caseLabel" value="${matWizardInstance.caseLabel}"/>
			<div class="formsection formsection3">
				<label class="nopad">Control label:</label>
			<g:textField name="controlLabel" value="${matWizardInstance.controlLabel}"/>

			</div>
			<div class="viewport ui-droppable">

			<div class="grouping-area" id="sortable-groups"><!--begin grouping area-->
				<g:each in="${groups}" status="i" var="group">
					<div class="around-sortable"><!--begin around-sortable-->
						<div class="group-header"><!--begin group-header-->
							<h5>${group.groupName}</h5>
								<g:select from="${groupTypes}" value="${group.groupType}"
													optionKey="value" optionValue="value" name="group_${group.id}" id="group_${id}"/>
						</div>
						<ul class="connectedSortable ui-sortable sortable-list" id="list_${group.id}">
							<g:each in="${samples}" status="j" var="sample">
								<g:if test="${sample.getGroupName().equals(group.groupName)}">
									<li class="drag-item" id="${sample.sampleId}">${sample.sampleId}</li>
								</g:if>
							</g:each>
						</ul>
					</div>
				</g:each>
			</div><!--end grouping area-->
			</div>
			<div class="prevnext">
        <g:link action="signalData" id="${matWizardInstance.id}" class="btn small primary prev">Back</g:link>
				<g:submitButton name="Next" value="Next" class="btn small primary next"/>
			</div>
		</div>
	</g:form>

</div>
</body>
</html>
