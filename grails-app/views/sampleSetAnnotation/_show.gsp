<%@ page import="org.sagres.sampleSet.component.OverviewComponent" %>
<div class="two-col-layout">
  <div class="col2container">
  <div class="col1container">
 <div class="col1" style="min-width: 30%; width:45%;">
    <g:set var="halfMax" value="${Math.ceil((double)sampleSetOverviewComponents.size()/2).toInteger()}"/>
    <g:each in="${sampleSetOverviewComponents}" var="ssComponent">
      <g:if test="${halfMax == 0}">
        </div>
       <div class="col2" style="width:45%;">
      </g:if>
      <g:set var="halfMax" value="${halfMax - 1}"/>
      <g:set var="compType" value="${OverviewComponent.get(ssComponent.componentId).componentType}"/>
      <g:set var="initValue" value="${fieldValue(bean:sampleSet.sampleSetAnnotation, field:OverviewComponent.get(ssComponent.componentId).annotationName)}"/>
      <div id="${OverviewComponent.get(ssComponent.componentId).annotationName}" class="editable-field">
        <h5>${OverviewComponent.get(ssComponent.componentId).name}</h5>

        <span class='min-max ui-icon-min'></span>
        <div id="text" contenteditable="${editable}" class="sampleset-content annotation" style="min-height:1em;">
          <g:if test="${initValue}">
            ${initValue.decodeHTML()}
            </div>
          </g:if>
          <g:else>
            </div>
            <div class="placeholder hint">
            <sec:ifAnyGranted roles="ROLE_USER,ROLE_ADMIN">
              <p>click here to add text</p>
            </sec:ifAnyGranted>
            <sec:ifNotGranted roles="ROLE_USER,ROLE_ADMIN">
              <p>--</p>
            </sec:ifNotGranted>
            </div>
          </g:else>

      </div>
    </g:each>
  </div>
  </div>
</div>

</div>