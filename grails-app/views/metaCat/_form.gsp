<%@ page import="org.sagres.mat.MetaCat; org.sagres.sampleSet.component.LookupList; org.sagres.sampleSet.component.LookupListDetail" %>

<div class="fieldcontain ${hasErrors(bean: metaCatInstance, field: 'displayName', 'error')} ">
	<label class="property-label" for="displayName">
		<g:message code="metaCat.displayName.label" default="Display Name" />: 
		
	</label>
	<g:textField name="displayName" value="${metaCatInstance?.displayName}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: metaCatInstance, field: 'generation', 'error')} ">
	<label class="property-label" for="generation">
		<g:message code="metaCat.generation.label" default="Disease" />: 
		
	</label>
<%--		<g:set var="diseaseLookupList" value="${LookupList.findByName('Disease')}"/>--%>
        <g:select from="${LookupList.findByName('Disease').lookupDetails.toList()}"
              id="disease" name="disease.id" noSelection="['null':'-select-']"
              optionKey="id" optionValue="name"
              value="${metaCatInstance?.disease?.id}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: metaCatInstance, field: 'generation', 'error')} ">
	<label class="property-label" for="generation">
		<g:message code="metaCat.generation.label" default="Generation" />: 
		
	</label>
	<g:select name="generation" from="${2..3}" value="${metaCatInstance?.generation}"/>
</div>


<div class="clearfix fieldcontain ${hasErrors(bean: metaCatInstance, field: 'user', 'error')}" style="margin-bottom: 0px;">
	<label class="property-label" for="user">
		<g:message code="project.user.label" default="Owner" />:
	</label>
	<g:if test="${currentUser == metaCatInstance.user || currentUser?.authorities.any { it.authority == 'ROLE_ADMIN' }}">
	<%--	<g:select id="user" name="user.id" noSelection="${[(currentUser ? currentUser.id: 'null'): (currentUser ? currentUser.username : 'Select One...')]}" from="${common.SecUser.list()}" optionKey="id" required="" value="${metaCatInstance?.user?.id ? currentUser?.id}"/>--%>
		<g:select id="user" name="user.id" noSelection="${['null':'Select One...']}" from="${common.SecUser.list()}" optionKey="id" required="" value="${metaCatInstance?.user ? metaCatInstance?.user?.id : currentUser?.id}"/>
	</g:if>
	<g:else>
		<g:select id="user" name="user.id" noSelection="${['null':'Select One...']}" from="${common.SecUser.list()}" optionKey="id" required="" value="${metaCatInstance?.user ? metaCatInstance?.user?.id : currentUser?.id}" disabled='true'/>
	</g:else>
</div>

<div class="fieldcontain ${hasErrors(bean: metaCatInstance, field: 'analyses', 'error')} ">
	<label class="property-label" for="analyses">
		<g:message code="metaCat.analyses.label" default="Analyses" />:
		
	</label>
<%--	<g:select name="analyses" from="${analysisInstanceList}" multiple="multiple" optionKey="id" size="5" value="${metaCatInstance?.analyses*.id}" class="many-to-one"/>--%>

	<table>
		<g:each in="${analysisInstanceList}" status="d" var="analysisInstance">
			<tr><td class="checklist" style="float: left;">
            	<g:checkBox style="float: left;" name="analysis_${analysisInstance.id}" value="${analysisInstance.id}" checked="${metaCatInstance?.analyses.find{ s->s.id == analysisInstance.id} }"/> <span style="display: inline-block; text-align: left; margin-left: 5px; width: 500px;">#${analysisInstance?.id} <g:link controller="analysis" action="show" id="${analysisInstance?.id}" target="_blank">${analysisInstance?.displayName}</g:link></span>
           	</td></tr>
         </g:each>
	</table>

</div>
