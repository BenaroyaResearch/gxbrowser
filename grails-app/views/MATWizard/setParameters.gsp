<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="MATWizard"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="mat-container">

	<h1>Module Analysis Wizard</h1>

	<h2>Set Parameters</h2>

  <g:if test="${flash.message}">
	 <div class="alert-message matwiz-message message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${matWizardInstance}">
    <div class="alert-message block-message error">
	<g:renderErrors bean="${matWizardInstance}" as="list"/>
	 </div>
	</g:hasErrors>

	<g:form action="saveParameters" class="mat-mainform">
	  <div class="mat-upload-form">
      <input type="hidden" name="id" value="${matWizardInstance.id}" id="id"/>
			<input type="hidden" name="step" value="6"/>
      <g:if test="${reasons.size() > 0}">
				<g:each in="${reasons}" var="reason">
					<div class="alert-message error">${reason}</div>
				</g:each>
			</g:if>

      <p>The settings below are the default settings for Module Analysis Tool. If you like, you can leave them exactly as they are and go to the next page.</p>
      <p> If you like, you can also change some or all of the parameters. <a href="${resource(dir: "", file :"matwizmore.gsp#faq")}" target="_blank">Details on the parameters can be found in the FAQ</a>.</p>

      <div>
        <g:hiddenField name="method" value="${matWizardInstance.method}"/>
        <p>
          <label>Method:</label>
          <div class="button-group" name="method-buttons">
            <span class="button ${matWizardInstance.method == 'fold' ? 'active' : ''}" name="fold" title="Fold Change" onclick="toggleMethod('fold');">Fold Change</span>
            <span class="button ${matWizardInstance.method == 'zscore' ? 'active' : ''}" name="zscore" title="Z-score" onclick="toggleMethod('zscore');">Z-score</span>
          </div>
        </p>

        <!--show this only if fold change is chosen above-->
        <p class="methodInputs" id="foldInput" <g:if test="${matWizardInstance.method != 'fold'}">style="display:none;"</g:if>>
          <label>Fold Change Threshold:</label>
          <g:textField name="foldCut" class="mini" value="${matWizardInstance.foldCut}"/>
        </p>
        <!--end-->

        <!--show this only if z-score is chosen above-->
        <p class="methodInputs" id="zscoreInput" <g:if test="${matWizardInstance.method != 'zscore'}">style="display:none;"</g:if>>
          <label>Z-score Threshold:</label>
          <g:textField name="zscoreCut" class="mini" value="${matWizardInstance.zscoreCut}"/>
        </p>
        <!--end-->

        <p>
          <label>Difference Threshold:</label>
          <g:textField name="diffThreshold" class="mini" value="${matWizardInstance.diffThreshold}"/>
        </p>

          <p>
          <g:hiddenField name="mts" value="${matWizardInstance.mts}"/>
          <label>Multiple Testing Correction:<br/><span style="font-weight: normal; font-size: 12px; font-style: italic;">(utilizing Benjamini-Hochberg correction)</span></label>
          <div class="button-group" name="mts-buttons">
            <span class="button ${matWizardInstance.mts == 'TRUE' ? 'active' : ''}" name="mtsTRUE" title="On" onclick="toggleMts('TRUE');">On</span>
            <span class="button ${matWizardInstance.mts == 'TRUE' ? '' : 'active'}" name="mtsFALSE" title="Off" onclick="toggleMts('FALSE');">Off</span>
          </div>
        </p>

        <p>
          <label>False Discovery Rate:</label>
          <g:textField name="fdr" class="mini" value="${matWizardInstance.fdr}"/> <em>(range 0-1)</em>
        </p>



      </div>

      <div class="prevnext">
        <g:link action="selectGroups" id="${matWizardInstance.id}" class="btn small primary prev">Back</g:link>
				<g:submitButton name="Next" value="Next" class="btn small primary next"/>
			</div>

    </div>
	</g:form>

</div>
<g:javascript>
  var toggleMethod = function(method) {
    $("div[name='method-buttons'] span.button").removeClass("active");
    $("span.button[name='"+method+"']").addClass("active");
    $("input[name='method']").val(method);
    $("p.methodInputs").hide();
    $("p#"+method+"Input").show();
  };
  var toggleMts = function(mts) {
    $("div[name='mts-buttons'] span.button").removeClass("active");
    $("span.button[name='mts"+mts+"']").addClass("active");
    $("input[name='mts']").val(mts);
  }
</g:javascript>

</body>
</html>
