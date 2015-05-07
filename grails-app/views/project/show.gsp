<%@ page import="org.sagres.project.Project" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
		<g:javascript src="moment.js"/>
		<g:javascript src="moment-workingDays.js"/>
		<g:javascript src="moment-range.js"/>
  		<g:javascript src="modules/raphael.js"/>
  		<g:javascript src="raphael-gantt.js"/>
		<g:javascript src="jquery.simple-color.js"/>
		
		<style type="text/css">

  			#chart{
  				margin-left: 100px;
    			width: 100%;
    			overflow: auto;
  			}

  			#chart svg{
    			border: 2px solid black;
    			display: inline-block;
  			}
			span.property-label { display: inline-block; width: 100px; }
			table td {
				border-bottom: none;
				padding: 2px 2px 2px;
			}
			li p{width: 800px; margin-left: 102px}

			.simpleColorDisplay {
    			font-family: Helvetica;
   				 margin: 10px 100px;
  			}
  			.simpleColorChooser {
   				 background-color: #fff;
  			}
			
		</style>
		<g:javascript>
			var closeModal = function(modalId) {
    			$("#"+modalId).modal("hide");
  			};
		
			var taskShift = function() {
				var args = {
					type:		$("#shiftType").val(),
				  	pid:		$("#shiftProject").val(),
  					tid:		$("#shiftTask").val(),
 					amount:		$("#shiftAmount").val(),
 					units:		$("#shiftUnits").val(),
 					direction:	$("#shiftDirect").val(),
 					adjust:		$("#shiftEnds").val()
 				};
	
				$.getJSON(getBase()+"/project/taskShift", args, function(json) {
					window.location.reload(true);
				});
			};
			
		
		// requires moment.js, moment-range.js
		var isActive = function(start, end, today) {
			var range = moment().range(start, end);
			return range.contains(today); 
		};
		
	  	$(document).ready(function() {
	  	
			   $('.simple_color').simpleColor({
								    displayColorCode: true,
    								livePreview: true,
    								readonly: true});

 	  		var args = {
	  			id: ${projectInstance.id},
	  			title: "${projectInstance.title}",
	  			startDate: moment("${projectInstance.contactDate}").format("YYYY-MM-DD"),
	  			endDate: moment("${projectInstance.deliveryDate}").format("YYYY-MM-DD"),
	  			chartType: 'project'
	  		};
	  		
	  		if (args.startDate && args.endDate) {
				$.getJSON(getBase()+"/project/getGanttData", args, function(json) {

					//console.log([json]);
					chart.loadData([json]);
	      			chart.draw();
	      			$("#loading").hide();
				});
			}
	  	});
	  </g:javascript>
		
	</head>
	<body>
		<div id="show-project" class="content scaffold-show" role="main">
			<h1><g:fieldValue bean="${projectInstance}" field="name"/> - <g:fieldValue bean="${projectInstance}" field="title"/> </h1>
			<div id="tab-container" class="container">
			<g:if test="${flash.message}">
				<div class="message" role="status"><h2>${flash.message}</h2></div>
			</g:if>
			</div>
			<ul class="property-list project">

				<g:if test="${projectInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="project.name.label" default="Name" />: </span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${projectInstance}" field="name"/></span>
					
				</li>
				</g:if>

				<g:if test="${projectInstance?.title}">
				<li class="fieldcontain">
					<span id="title-label" class="property-label"><g:message code="project.title.label" default="Title" />: </span>
					
						<span class="property-value" aria-labelledby="title-label"><g:fieldValue bean="${projectInstance}" field="title"/></span>
					
				</li>
				</g:if>
			

				<g:if test="${projectInstance?.status}">
				<li class="fieldcontain">
					<span id="status-label" class="property-label"><g:message code="project.status.label" default="Status" />: </span>
					
						<span class="property-value" aria-labelledby="status-label"><g:link controller="lookupList" action="show" id="${projectInstance?.status?.lookupList.id}">${projectInstance?.status?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
				<g:if test="${projectInstance?.statusDate}">
				<li class="fieldcontain">
					<span id="statusDate-label" class="property-label"><g:message code="project.statusDate.label" default="Status Date" />: </span>
					
						<span class="property-value" aria-labelledby="statusDate-label"><g:formatDate format="yyyy-MM-dd" date="${projectInstance?.statusDate}" /></span>
					
				</li>
				</g:if>
				
				<g:if test="${projectInstance?.owner}">
				<li class="fieldcontain">
					<span id="owner-label" class="property-label"><g:message code="project.owner.label" default="Owner" />: </span>
					
						<span class="property-value" aria-labelledby="owner-label"><g:link controller="secUser" action="show" id="${projectInstance?.owner?.id}">${projectInstance?.owner?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
									
				<g:if test="${projectInstance?.analyst}">
				<li class="fieldcontain">
					<span id="analyst-label" class="property-label"><g:message code="project.analyst.label" default="Analyst" />: </span>
					
						<span class="property-value" aria-labelledby="analyst-label"><g:link controller="secUser" action="show" id="${projectInstance?.analyst?.id}">${projectInstance?.analyst?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
<%--				<g:if test="${projectInstance?.beginDate}">--%>
<%--				<li class="fieldcontain">--%>
<%--					<span id="beginDate-label" class="property-label"><g:message code="project.beginDate.label" default="Begin Date" /></span>--%>
<%--					--%>
<%--						<span class="property-value" aria-labelledby="beginDate-label"><g:formatDate date="${projectInstance?.beginDate}" /></span>--%>
<%--					--%>
<%--				</li>--%>
<%--				</g:if>--%>
			
				<g:if test="${projectInstance?.client}">
				<li class="fieldcontain">
					<span id="client-label" class="property-label"><g:message code="project.client.label" default="Client" />: </span>
					
						<span class="property-value" aria-labelledby="client-label"><g:fieldValue bean="${projectInstance}" field="client"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${projectInstance?.contact}">
				<li class="fieldcontain">
					<span id="contact-label" class="property-label"><g:message code="project.contact.label" default="Contact" />: </span>
					
						<span class="property-value" aria-labelledby="contact-label"><g:fieldValue bean="${projectInstance}" field="contact"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${projectInstance?.contactDate}">
				<li class="fieldcontain">
					<span id="contactDate-label" class="property-label"><g:message code="project.contactDate.label" default="Begin Date" />: </span>
					
						<span class="property-value" aria-labelledby="contactDate-label"><g:formatDate format="yyyy-MM-dd" date="${projectInstance?.contactDate}" /></span>
					
				</li>
				</g:if>
				
				<g:if test="${projectInstance?.analysisDate}">
				<li class="fieldcontain">
					<span id="analysisDate-label" class="property-label"><g:message code="project.analysisDate.label" default="Analysis Date" />: </span>
					
						<span class="property-value" aria-labelledby="analysisDate-label"><g:formatDate format="yyyy-MM-dd" date="${projectInstance?.analysisDate}" /></span>
					
				</li>
				</g:if>

				<g:if test="${projectInstance?.deliveryDate}">
				<li class="fieldcontain">
					<span id="deliveryDate-label" class="property-label"><g:message code="project.deliveryDate.label" default="Delivery Date" />: </span>
					
						<span class="property-value" aria-labelledby="deliveryDate-label"><g:formatDate format="yyyy-MM-dd" date="${projectInstance?.deliveryDate}" /></span>
					
				</li>
				</g:if>

				<g:if test="${projectInstance?.sampleCount}">
				<li class="fieldcontain">
					<span id="sampleCount-label" class="property-label"><g:message code="project.sampleCount.label" default="Sample Count" />: </span>
					
						<span class="property-value" aria-labelledby="sampleCount-label"><g:fieldValue bean="${projectInstance}" field="sampleCount"/></span>
					
				</li>
				</g:if>
				

				<g:if test="${projectInstance?.technology}">
				<li class="fieldcontain">
					<span id="technology-label" class="property-label"><g:message code="project.technology.label" default="Technology" />: </span>
					
						<span class="property-value" aria-labelledby="technology-label"><g:fieldValue bean="${projectInstance}" field="technology"/></span>
					
				</li>
				</g:if>
						
				<g:if test="${projectInstance?.organism}">
				<li class="fieldcontain">
					<span id="organism-label" class="property-label"><g:message code="project.organism.label" default="Organism" />: </span>
					
						<span class="property-value" aria-labelledby="organism-label"><g:fieldValue bean="${projectInstance}" field="organism"/></span>
					
				</li>
				</g:if>
				<g:if test="${projectInstance?.color}">
				<li class="fieldcontain">
					<span id="organism-label" class="property-label"><g:message code="project.organism.label" default="Color" />: </span>
					
						<span class="property-value" aria-labelledby="organism-label"><g:textField name="color" class='simple_color' readonly="readonly" value="${projectInstance?.color}"/></span>
					
				</li>
				</g:if>
				
				
				<g:if test="${projectInstance?.tasks}">
				<li class="fieldcontain">
					<span id="tasks-label" class="property-label"><g:message code="project.tasks.label" default="Tasks" />: </span>
						<p>
						<g:each in="${projectInstance?.tasks.sort { t1, t2 -> t1.beginDate <=> t2.beginDate } }" status="t" var="taskInstance">
							<span class="property-value" aria-labelledby="tasks-label"><g:link controller="task" action="show" id="${taskInstance.id}">${taskInstance.encodeAsHTML()}</g:link></span>  <%-- Dates: <g:formatDate format="yyyy-MM-dd" date="${taskInstance?.beginDate}"/> - <g:formatDate format="yyyy-MM-dd" date="${taskInstance?.endDate}"/> --%> 
							<g:javascript>
								var beginDate = moment("${taskInstance.beginDate}");
								var endDate   = moment("${taskInstance.endDate}");
								var today	  = moment();
								if (isActive(beginDate, endDate, today)) {
									document.write("<span style=\"color: red;\"> - Currently Active</span>");
								}
							</g:javascript> 
							</br>
				         </g:each>
				         </p>
				</li>
				</g:if>
						
				<g:if test="${projectInstance?.deliverables}">
				<li class="fieldcontain">
					<span id="deliverables-label" class="property-label"><g:message code="project.deliverables.label" default="Deliverables" />: </span>
						<p>
						<g:each in="${deliverableInstanceList}" status="d" var="deliverableInstance">
							<g:if test="${projectInstance?.deliverables.find{ s->s.id == deliverableInstance.id}}">

				            	<span class="deliverables-value" aria-labelledby="tasks-label"><g:message code="${deliverableInstance?.name}.label" default="${deliverableInstance?.name}" /></span><br/>

				           </g:if>
				         </g:each>
				         </p>
				</li>
				</g:if>
				<g:if test="${projectInstance?.tg2id}">
				<li class="fieldcontain">
					<span id="tg2id-label" class="property-label"><g:message code="project.tg2id.label" default="Tg2 Reference" />: </span>
					
						<span class="property-value" aria-labelledby="tg2id-label"><a href="http://srvdm:8080/TG2/project/projectDetail?tab=0&tab2=1&id=${projectInstance?.tg2id}">${tg2ProjectList.find({it.id == projectInstance?.tg2id})?.title}</a></span>
					
				</li>
				</g:if>
				
			<g:if test="${projectInstance?.sampleSet}">
				<li class="fieldcontain">
					<span id="sampleSet-label" class="property-label"><g:message code="project.sampleSet.label" default="Sample Set" />: </span>
					
						<span class="property-value" aria-labelledby="sampleSet-label"><g:link controller="sampleSet" action="show" id="${projectInstance?.sampleSet?.id}">${projectInstance?.sampleSet?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
				<g:if test="${projectInstance?.locked}">
				<li class="fieldcontain">
					<span id="locked-label" class="property-label"><g:message code="project.locked.label" default="Locked"/>:</span>
					
					<span class="property-value" aria-labelledby="importance-label">
				        	<g:checkBox name="locked" value="1" checked="${projectInstance?.locked}" disabled="true" />
				    </span>
				</li>
				</g:if>

				<g:if test="${projectInstance?.privateRecord}">
				<li class="fieldcontain">
					<span id="privateRecord-label" class="property-label"><g:message code="project.privateRecord.label" default="Private Record"/>:</span>
					
					<span class="property-value" aria-labelledby="importance-label">
				        	<g:checkBox name="privateRecord" value="1" checked="${projectInstance?.privateRecord}" disabled="true" />
				    </span>
				</li>
				</g:if>
			
				<g:if test="${projectInstance?.hypothesis}">
				<li class="fieldcontain">
					<span id="hypothesis-label" class="property-label"><g:message code="project.hypothesis.label" default="Hypothesis" />: </span>
						<p>
						<span class="property-value" aria-labelledby="hypothesis-label"><g:fieldValue bean="${projectInstance}" field="hypothesis"/></span>
						</p>
				</li>
				</g:if>
				
				<g:if test="${projectInstance?.design}">
				<li class="fieldcontain">
					<span id="design-label" class="property-label"><g:message code="project.design.label" default="Design" />: </span>
						<p>
						<span class="property-value" aria-labelledby="design-label" ><g:fieldValue bean="${projectInstance}" field="design"/></span>
						</p>
				</li>
				</g:if>
			
				<g:if test="${projectInstance?.purpose}">
				<li class="fieldcontain">
					<span id="design-label" class="property-label"><g:message code="project.design.label" default="Purpose" />: </span>
						<p>
						<span class="property-value" aria-labelledby="design-label"><g:fieldValue bean="${projectInstance}" field="design"/></span>
						</p>
				</li>
				</g:if>			
					
				<g:if test="${projectInstance?.comments}">
				<li class="fieldcontain">
					<span id="comments-label" class="property-label"><g:message code="project.comments.label" default="Comments" />: </span>
						<p>
						<span class="property-value" aria-labelledby="comments-label"><g:fieldValue bean="${projectInstance}" field="comments"/></span>
						</p>
				</li>
				</g:if>
			
			
<%--				<g:if test="${projectInstance?.endDate}">--%>
<%--				<li class="fieldcontain">--%>
<%--					<span id="endDate-label" class="property-label"><g:message code="project.endDate.label" default="End Date" /></span>--%>
<%--					--%>
<%--						<span class="property-value" aria-labelledby="endDate-label"><g:formatDate date="${projectInstance?.endDate}" /></span>--%>
<%--					--%>
<%--				</li>--%>
<%--				</g:if>--%>
<%--			--%>
<%--				<g:if test="${projectInstance?.gxbReference}">--%>
<%--				<li class="fieldcontain">--%>
<%--					<span id="gxbReference-label" class="property-label"><g:message code="project.gxbReference.label" default="Gxb Reference" /></span>--%>
<%--					--%>
<%--						<span class="property-value" aria-labelledby="gxbReference-label"><g:fieldValue bean="${projectInstance}" field="gxbReference"/></span>--%>
<%--					--%>
<%--				</li>--%>
<%--				</g:if>--%>
<%--			--%>
<%--				<g:if test="${projectInstance?.importance}">--%>
<%--				<li class="fieldcontain">--%>
<%--					<span id="importance-label" class="property-label"><g:message code="project.importance.label" default="Importance" /></span>--%>
<%--					--%>
<%--						<span class="property-value" aria-labelledby="importance-label"><g:fieldValue bean="${projectInstance}" field="importance"/></span>--%>
<%--					--%>
<%--				</li>--%>
<%--				</g:if>--%>
<%--			--%>
<%--				<g:if test="${projectInstance?.markedForDelete}">--%>
<%--				<li class="fieldcontain">--%>
<%--					<span id="markedForDelete-label" class="property-label"><g:message code="project.markedForDelete.label" default="Marked For Delete" /></span>--%>
<%--					--%>
<%--						<span class="property-value" aria-labelledby="markedForDelete-label"><g:fieldValue bean="${projectInstance}" field="markedForDelete"/></span>--%>
<%--					--%>
<%--				</li>--%>
<%--				</g:if>--%>
			
				<g:if test="${projectInstance?.priority}">
				<li class="fieldcontain">
					<span id="priority-label" class="property-label"><g:message code="project.priority.label" default="Priority" /></span>
					
						<span class="property-value" aria-labelledby="priority-label"><g:fieldValue bean="${projectInstance}" field="priority"/></span>
					
				</li>
				</g:if>
			

			<li><spanid="chart-label" class="property-label">Gantt Chart:</span>
<%--			  <h3>Project Gantt Chart</h3>--%>
<%--			<span class="property-value" aria-labelledby="chart-label">--%>
 			<div class="property-value" aria-labelledby="chart-label" id="chart">
  			<p id="loading">Gantt Chart Loading...</p>
  			</div>
  			</li>
  			<script type="text/javascript">
      			var chart = new GanttChart("chart");
 			</script>
 			</ul>
			<g:if test="${!projectInstance.locked || projectInstance.owner.id == currentUser?.id}">
			<g:form>
				<fieldset class="buttons">
				
					<g:hiddenField name="id" value="${projectInstance?.id}" />
					<g:if test="${projectInstance?.tasks}">
					<a href="#" class="button medium" data-keyboard="true" data-backdrop="true" data-controls-modal="viewShift-modal">Shift Tasks</a>
			        <div id="viewShift-modal" class="modal hide" >
			            <div class="modal-header">
			              <a href="#" class="close">×</a>
			              <h4>Shift Tasks</h4>
			            </div>
			            <div class="modal-body" style="overflow-y:scroll; max-height:300px;">
			            	<p>Use 'Shift' to move both begin and end dates, or 'Extend' to move just the end date of the selected task, and shift the following tasks accordingly</p> 
				 			<span>
				 				<g:hiddenField name="shiftProject" value="${projectInstance?.id}" />
								<g:select name="shiftType"  style="width: 70px;" from="${['Shift', 'Extend']}" /> 
			 					<g:select name="shiftTask" noSelection="${['0':'Starting With...']}" from="${projectInstance?.tasks.sort { t1, t2 -> t1.beginDate <=> t2.beginDate } }" optionKey="id"/> x
								<g:select name="shiftAmount" style="width: 45px;" from="${1..10}" /> 
								<g:select name="shiftUnits"  style="width: 70px;" from="${['Days', 'Weeks']}" /> 
								<g:select name="shiftDirect" style="width: 85px;" from="${['Forward', 'Backward']}" /> 
							</span>
							<br/>
							<br/>
							<span class="checkbox"><g:checkBox name="shiftEnds" value="false" style="margin-right: 5px;"/>Adjust project endpoints as necessary</span>			
			            </div>
			            <div class="modal-footer">
			              <button class="btn primary small" onclick="taskShift();">Shift Tasks</button>
			              <button class="btn primary small" onclick="closeModal('viewShift-modal');">Cancel</button>
			            </div>
		            </div>
					</g:if>
					<g:link class="button edit" action="edit" id="${projectInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="button delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
			</g:if>
		</div>
	</body>
</html>
