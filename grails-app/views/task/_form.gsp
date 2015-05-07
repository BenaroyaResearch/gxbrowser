<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'name', 'error')} ">
	<label class="property-label" for="name">
		<g:message code="task.name.label" default="Name" />:
		
	</label>
	<g:textField name="name" value="${taskInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'type', 'error')} required">
	<label class="property-label" for="type">
		<g:message code="task.type.label" default="Type" />:
	</label>
	<g:select id="type" name="type.id" noSelection="${['null':'Select One...']}" from="${org.sagres.sampleSet.component.LookupList.findByName(grailsApplication.config.project.taskLookupName).lookupDetails}" optionKey="id" required="" value="${taskInstance?.type?.id}" class="many-to-one"/>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'resource', 'error')} ">
	<label class="property-label" for="resource">
		<g:message code="task.resource.label" default="Resource" />:
		
	</label>
	<g:select id="resource" name="resource.id" noSelection="${['null':'Select One...']}" from="${org.sagres.sampleSet.component.LookupList.findByName(grailsApplication.config.project.resourceLookupName).lookupDetails}" optionKey="id" value="${taskInstance?.resource?.id}" class="many-to-one"/>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: taskInstance, field: 'beginDate', 'error')} required" style="margin-bottom: 0px;">
	<label class="property-label" for="beginDate">
		<g:message code="task.beginDate.label" default="Begin Date" />:
		
	</label>
    <div class="input-append datepicker-from">
		<g:textField name="beginDate" class="datepicker" placeholder="Date" value="${formatDate(format:"yyyy-MM-dd", date:taskInstance?.beginDate)}"/>
        <label class="add-on">
        	<span class="ui-icon-date"></span>
        </label>
	</div>
	<span class="required-indicator">*</span>
</div>

<div class="clearfix fieldcontain ${hasErrors(bean: taskInstance, field: 'endDate', 'error')} required" style="margin-bottom: 0px;">
	<label class="property-label" for="endDate">
		<g:message code="task.endDate.label" default="End Date" />:
		
	</label>
    <div class="input-append datepicker-from">
		<g:textField name="endDate" class="datepicker" placeholder="Date" value="${formatDate(format:"yyyy-MM-dd", date:taskInstance?.endDate)}"/>
        <label class="add-on">
        	<span class="ui-icon-date"></span>
        </label>
	</div>
	<span class="required-indicator">*</span>
</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'comments', 'error')} ">
	<label class="property-label" for="comments">
		<g:message code="task.comments.label" default="Comments" />:
		
	</label>
	<g:textField name="comments" value="${taskInstance?.comments}"/>
</div>
<g:hiddenField name="project.id" value="${taskInstance?.project?.id}"/>
<%--<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'project', 'error')} required">--%>
<%--	<label for="project">--%>
<%--		<g:message code="task.project.label" default="Project" />--%>
<%--		<span class="required-indicator">*</span>--%>
<%--	</label>--%>
<%--	<g:select id="project" name="project.id" from="${org.sagres.project.Project.list()}" optionKey="id" required="" value="${taskInstance?.project?.id}" class="many-to-one"/>--%>
<%--</div>--%>

