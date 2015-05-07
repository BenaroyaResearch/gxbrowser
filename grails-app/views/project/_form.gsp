<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'name', 'error')} required">
	<label class="property-label" for="name">
		<g:message code="project.name.label" default="Name" />: 
	</label>
	<g:textField name="name" value="${projectInstance?.name}"/>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'title', 'error')} required">
	<label class="property-label" for="title">
		<g:message code="project.title.label" default="Title" />: 
	</label>
	<g:textField name="title" value="${projectInstance?.title}"/>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'status', 'error')} required">
	<label class="property-label" for="status">
		<g:message code="project.status.label" default="Status" />:
	</label>
	<g:select id="status" name="status.id" noSelection="${['null':'Select One...']}" from="${org.sagres.sampleSet.component.LookupList.findByName(grailsApplication.config.project.statusLookupName).lookupDetails}" optionKey="id" required="" value="${projectInstance?.status?.id}"/>
	<span class="required-indicator">*</span>
</div>

<%--<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'beginDate', 'error')} required">--%>
<%--	<label class="property-label" for="beginDate">--%>
<%--		<g:message code="project.beginDate.label" default="Begin Date" />--%>
<%--		<span class="required-indicator">*</span>--%>
<%--	</label>--%>
<%--	<g:datePicker name="beginDate" precision="day"  value="${projectInstance?.beginDate}"  />--%>
<%--</div>--%>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'owner', 'error')} required">
	<label class="property-label" for="owner">
		<g:message code="project.owner.label" default="Owner" />:
	</label>
<%--	<g:select id="owner" name="owner.id" noSelection="${[(currentUser ? currentUser.id: 'null'): (currentUser ? currentUser.username : 'Select One...')]}" from="${common.SecUser.list()}" optionKey="id" required="" value="${projectInstance?.owner?.id ? currentUser?.id}"/>--%>
	<g:select id="owner" name="owner.id" noSelection="${['null':'Select One...']}" from="${common.SecUser.list()}" optionKey="id" required="" value="${projectInstance?.owner ? projectInstance?.owner?.id : currentUser?.id}"/>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'analyst', 'error')} required">
	<label class="property-label" for="analyst">
		<g:message code="project.analyst.label" default="Analyst" />:
	</label>
	<g:select id="analyst" name="analyst.id" noSelection="${['null':'Select One...']}" from="${common.SecUser.list()}" optionKey="id" required="" value="${projectInstance?.analyst?.id}"/>
	<span class="required-indicator">*</span>
</div>


<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'client', 'error')} required">
	<label class="property-label" for="client">
		<g:message code="project.client.label" default="Client" />:
	</label>
	<g:textField name="client" value="${projectInstance?.client}"/>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'contact', 'error')} required">
	<label class="property-label" for="contact">
		<g:message code="project.contact.label" default="Contact" />:
	</label>
	<g:textField name="contact" value="${projectInstance?.contact}"/>		
	<span class="required-indicator">*</span>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'contactDate', 'error')} required" style="margin-bottom: 0px;">
	<label class="property-label" for="contactDate">
		<g:message code="project.contactDate.label" default="Begin Date" />:
	</label>
    <div class="input-append datepicker-from">
		<g:textField name="contactDate" class="datepicker" placeholder="Date" value="${formatDate(format:"yyyy-MM-dd", date:projectInstance?.contactDate)}"/>
        <label class="add-on">
        	<span class="ui-icon-date"></span>
        </label>
	</div>
	<span class="required-indicator">*</span>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'analysisDate', 'error')} required" style="margin-bottom: 0px;">
	<label class="property-label" for="analysisDate">
		<g:message code="project.analysisDate.label" default="Analysis Date" />:
	</label>
    <div class="input-append datepicker-from">
		<g:textField name="analysisDate" class="datepicker" placeholder="Date" value="${formatDate(format:"yyyy-MM-dd", date:projectInstance?.analysisDate)}"/>
        <label class="add-on">
        	<span class="ui-icon-date"></span>
        </label>
	</div>
	<span class="required-indicator">*</span>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'deliveryDate', 'error')} required" style="margin-bottom: 0px;">
	<label class="property-label" for="deliveryDate">
		<g:message code="project.deliveryDate.label" default="Delivery Date" />:
	</label>
    <div class="input-append datepicker-from">
		<g:textField name="deliveryDate" class="datepicker" placeholder="Date" value="${formatDate(format:"yyyy-MM-dd", date:projectInstance?.deliveryDate)}"/>
        <label class="add-on">
        	<span class="ui-icon-date"></span>
        </label>
	</div>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'sampleCount', 'error')}">
	<label class="property-label" for="sampleCount">
		<g:message code="project.sampleCount.label" default="Sample Count" />:
	</label>
	<g:textField name="sampleCount" value="${projectInstance?.sampleCount}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'technology', 'error')} required">
	<label class="property-label" for="technology">
		<g:message code="project.technology.label" default="Technology" />:
	</label>
	<g:select id="technology" name="technology.id" noSelection="${['null':'Select One...']}" from="${org.sagres.sampleSet.component.LookupList.findByName(grailsApplication.config.project.technologyLookupName).lookupDetails}" optionKey="id" required="" value="${projectInstance?.technology?.id}"/>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'organism', 'error')} ">
	<label class="property-label" for="organism">
		<g:message code="project.organism.label" default="Organism" />
		
	</label>
	<g:select id="organism" name="organism.id" noSelection="${['null':'Select One...']}" from="${org.sagres.sampleSet.component.LookupList.findByName(grailsApplication.config.project.speciesLookupName).lookupDetails}" optionKey="id" required="" value="${projectInstance?.organism?.id}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'color', 'error')}">
	<label class="property-label" for="color">
		<g:message code="project.color.label" default="Color" />:
	</label>
	<g:textField name="color" class='simple_color' value="${projectInstance?.color}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'deliverables', 'error')} requried">
	<label class="property-label" for="deliverables">
		<g:message code="project.deliverables.label" default="Deliverables" />:
	</label>
	<table>
		<g:each in="${deliverableInstanceList}" status="d" var="deliverableInstance">
		<tr><td class="checklist" style="float: left;">
      		<label class="checkbox" >
            	<g:checkBox style="float: left;" name="deliverables_${deliverableInstance.id}" value="${deliverableInstance.id}" checked="${projectInstance?.deliverables.find{ s->s.id == deliverableInstance.id} }"/> <span class="text" style="float: left; margin-left: 5px;"  ><g:message code="${deliverableInstance?.name}.label" default="${deliverableInstance?.name}" /></span>
           </label>
           </td></tr>
         </g:each>
	</table>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'tasks', 'error')} requried">
	<label class="property-label" for="tasks">
		<g:message code="project.tasks.label" default="Tasks" />:
	</label>
	<table>
		<g:each in="${projectInstance?.tasks?.sort { t1, t2 -> t1.beginDate <=> t2.beginDate } }" status="t" var="taskInstance">
			<tr><td class="checklist" style="width: 300px;">
		    <g:link controller="task" action="show" id="${taskInstance.id}">${taskInstance.encodeAsHTML()}</g:link> </td><td class="checklist">Dates: <g:formatDate format="yyyy-MM-dd" date="${taskInstance?.beginDate}"/> - <g:formatDate format="yyyy-MM-dd" date="${taskInstance?.endDate}"/>
			</td></tr>
		</g:each>
		<g:if test="${projectInstance?.tasks}">
		<tr><td class="checklist">
<%--		<button class="btn primary small view-analysis" data-keyboard="true" data-backdrop="true" data-controls-modal="viewShift-modal">Shift Tasks</button>--%>
		<a href="#" data-keyboard="true" data-backdrop="true" data-controls-modal="viewShift-modal">Shift Tasks</a>
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
        </td></tr>
		</g:if>
		<tr><td class="checklist">

		
			<g:link controller="task" action="create" params="['project.id': projectInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'task.label', default: 'Task')])}</g:link>
		</td></tr>
		</table>
</div>

<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'tg2id', 'error')}">
	<label class="property-label" for="tg2id">
		<g:message code="project.tg2id.label" default="Tg2 Project" />:
	</label>
	<g:select name="tg2id" noSelection="${['0':'Select One...']}" from="${tg2ProjectList}" optionValue="title" optionKey="id" required="" value="${projectInstance?.tg2id}"/>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'sampleSet', 'error')}">
	<label class="property-label" for="sampleSet">
		<g:message code="project.sampleSet.label" default="Sample Set" />:
	</label>
	<g:select id="sampleSet" name="sampleSet.id" noSelection="${['null':'Select One...']}" from="${org.sagres.sampleSet.SampleSet.list()}" optionKey="id" required="" value="${projectInstance?.sampleSet?.id}"/>	
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'hypothesis', 'error')} ">
	<label class="property-label" for="hypothesis">
		<g:message code="project.hypothesis.label" default="Hypothesis" />:
		
	</label>
	<g:textArea name="hypothesis" style='width: 300px; height: 150px;' value="${projectInstance?.hypothesis}"/>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'design', 'error')} ">
	<label class="property-label" for="design">
		<g:message code="project.design.label" default="Design" />:
		
	</label>
	<g:textArea name="design" style='width: 300px; height: 150px;' value="${projectInstance?.design}"/>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'purpose', 'error')} ">
	<label class="property-label" for="purpose">
		<g:message code="project.purpose.label" default="Purpose" />:
		
	</label>
	<g:textArea name="purpose" style='width: 300px; height: 150px;' value="${projectInstance?.purpose}"/>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: projectInstance, field: 'comments', 'error')} ">
	<label class="property-label" for="comments">
		<g:message code="project.comments.label" default="Comments" />:
	</label>
	<g:textArea name="comments" style='width: 300px; height: 150px;' value="${projectInstance?.comments}"/>
</div>

<%--<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'endDate', 'error')} required">--%>
<%--	<label class="property-label" for="endDate">--%>
<%--		<g:message code="project.endDate.label" default="End Date" />--%>
<%--		<span class="required-indicator">*</span>--%>
<%--	</label>--%>
<%--	<g:datePicker name="endDate" precision="day"  value="${projectInstance?.endDate}"  />--%>
<%--</div>--%>
<%----%>
<%--<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'gxbReference', 'error')} required">--%>
<%--	<label class="property-label" for="gxbReference">--%>
<%--		<g:message code="project.gxbReference.label" default="Gxb Reference" />--%>
<%--		<span class="required-indicator">*</span>--%>
<%--	</label>--%>
<%--	<g:field name="gxbReference" type="number" value="${projectInstance.gxbReference}" required=""/>--%>
<%--</div>--%>
<%----%>
<%--<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'importance', 'error')} required">--%>
<%--	<label class="property-label" for="importance">--%>
<%--		<g:message code="project.importance.label" default="Importance" />--%>
<%--		<span class="required-indicator">*</span>--%>
<%--	</label>--%>
<%--	<g:field name="importance" type="number" value="${projectInstance.importance}" required=""/>--%>
<%--</div>--%>
<%----%>
<%--<g:if test="${projectInstance?.owner?.id == currentUser?.id}">--%>
<div class="clearfix checklist fieldcontain ${hasErrors(bean: projectInstance, field: 'locked', 'error')}">
	<label class="property-label" for="locked">
		<g:message code="project.locked.label" default="Locked" />:
	</label>
	<label class="checkbox" >
   		<g:checkBox name="locked" value="1" checked="${projectInstance?.locked}"/><span class="text" style="margin-left: 5px;">Owner Edit Only</span>
   	</label>
</div>
<div class="clearfix checklist fieldcontain ${hasErrors(bean: projectInstance, field: 'privateRecord', 'error')}">
	<label class="property-label" for="privateRecord">
		<g:message code="project.privateRecord.label" default="Private" />:
	</label>
	<label class="checkbox" >	
   		<g:checkBox name="privateRecord" value="1" checked="${projectInstance?.privateRecord}"/> <span class="text" style="margin-left: 5px;">Private Record</span>
   	</label>
</div>
<%--</g:if>--%>
<%--<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'markedForDelete', 'error')} required">--%>
<%--	<label class="property-label" for="markedForDelete">--%>
<%--		<g:message code="project.markedForDelete.label" default="Marked For Delete" />--%>
<%--		<span class="required-indicator">*</span>--%>
<%--	</label>--%>
<%--	<g:field name="markedForDelete" type="number" value="${projectInstance.markedForDelete}" required=""/>--%>
<%--</div>--%>
<%----%>
<%--<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'priority', 'error')} ">--%>
<%--	<label class="property-label" for="priority">--%>
<%--		<g:message code="project.priority.label" default="Priority" />--%>
<%--		--%>
<%--	</label>--%>
<%--	<g:textField name="priority" value="${projectInstance?.priority}"/>--%>
<%--</div>--%>
<%----%>
<%--<div class="fieldcontain ${hasErrors(bean: projectInstance, field: 'statusDate', 'error')} required">--%>
<%--	<label class="property-label" for="statusDate">--%>
<%--		<g:message code="project.statusDate.label" default="Status Date" />--%>
<%--		<span class="required-indicator">*</span>--%>
<%--	</label>--%>
<%--	<g:datePicker name="statusDate" precision="day"  value="${projectInstance?.statusDate}"  />--%>
<%--</div>--%>
<%----%>


