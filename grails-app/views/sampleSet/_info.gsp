<%@ page import="org.sagres.sampleSet.SampleSetLink; common.ClinicalDataSource; common.chipInfo.RawSignalDataType; org.sagres.sampleSet.SampleSetRole" %>
<g:form name="sampleSet">
  <fieldset>
    <div class="clearfix">
      <label>Raw Signal Data Type</label>
      <div class="input">

          <sec:ifLoggedIn>
            <g:select name="raw-signal-type" from="${RawSignalDataType.list()}" optionKey="id" optionValue="displayName"
                  noSelection="['-select-':'-select-']" value="${sampleSet.rawSignalType?.id}" disabled="${!editable}"/>
           </sec:ifLoggedIn>

          <sec:ifNotLoggedIn>
          <span class="notlogged-replace">${sampleSet.rawSignalType?.displayName} </span>
          </sec:ifNotLoggedIn>


      </div>
    </div>
    <div class="clearfix">
      <label>Default Display Type</label>
      <div class="input">
        <sec:ifLoggedIn>
            <g:select name="default-display-type" from="${RawSignalDataType.list()}" optionKey="id" optionValue="displayName"
                  noSelection="['-select-':'-select-']" value="${sampleSet.defaultSignalDisplayType?.id}" disabled="${!editable}"/>
         </sec:ifLoggedIn>

         <sec:ifNotLoggedIn>
            <span class="notlogged-replace">${sampleSet.defaultSignalDisplayType?.displayName} </span>
         </sec:ifNotLoggedIn>

      </div>
    </div>
    <div class="clearfix">
      <label>Sharing/Publishing</label>
      <div class="input">
        <ul class="inputs-list">
          <li>
            <label style="display: inline-block;" for="publish-gxb">
              <g:checkBox name="publish-gxb" value="${sampleSet.gxbPublished == 1}" disabled="${!editable}"/>
              <span class="chkboxLabel">Publish to Gene Expression Browser (GXB)</span> </label>&nbsp;&nbsp;|&nbsp;&nbsp;<g:link controller="geneBrowser" action="show" id="${sampleSet.id}" class="btn" target="_blank">View in GXB &raquo;</g:link>

          </li>
          <li>
            <label for="set-privacy" style="display:inline-block;">
              <g:checkBox name="set-privacy" value="${!SampleSetRole.findBySampleSetId(sampleSet.id)}" disabled="${!editable}"/>
              <span class="chkboxLabel">Publish sample set</span>
            </label>
          </li>
        </ul>
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Clinical Datasource</legend>
    <div class="clearfix">
      <label>Datasource</label>
      <div class="input">
          <sec:ifLoggedIn>

              <g:select name="clinical-datasource" from="${ClinicalDataSource.list()}" optionKey="id" optionValue="displayName"
                          noSelection="['-select-':'-select-']" value="${sampleSet.clinicalDataSource?.id}" disabled="${!editable}"/>
              </sec:ifLoggedIn>
          <sec:ifNotLoggedIn>

             <span class="notlogged-replace"> ${sampleSet.clinicalDataSource?.displayName} </span>

          </sec:ifNotLoggedIn>

      </div>
    </div>
    <div class="clearfix">
      <label>Data Url</label>
      <div class="input">
          <sec:ifLoggedIn>
            <input type="text" id="${sampleSet.id}::clinicalDatasourceDataUrl" name="${sampleSet.id}::clinicalDatasourceDataUrl"
                   class="editable-text xlarge" value="${sampleSet.clinicalDatasourceDataUrl}" ${editable ? "" : "disabled='true'"}
                   title="${sampleSet.clinicalDatasourceDataUrl}"/>
            <g:link class="field-url" url="${sampleSet.clinicalDatasourceDataUrl}"><span class="ui-icon-linkgo"></span></g:link>
            <span class="help-block bri-help-block" style="clear:both;">Enter a URL such as http://www.benaroyaresearch.org</span>
        </sec:ifLoggedIn>
          <sec:ifNotLoggedIn>

             <span class="notlogged-replace"><g:link url="${sampleSet.clinicalDatasourceDataUrl}" target="_blank">${sampleSet.clinicalDataSource?.displayName}</g:link></span>

          </sec:ifNotLoggedIn>


      </div>
    </div>
  </fieldset>
</g:form>
<g:form>
  <fieldset>
    <legend>Links</legend>
    <g:each in="${SampleSetLink.findAllByVisible(1)}" var="link">
      <div class="clearfix">
      <label>${link.displayName}</label>
      <div class="input">

         <g:set var="dataUrl" value="${sampleSet.links?.find { it.linkType == link }?.dataUrl}"/>

        <sec:ifLoggedIn>
        <input type="text" id="${link.id}" name="${link.id}" class="sampleSetLink xlarge"
               title="${dataUrl}" value="${dataUrl}" ${editable ? "" : "disabled='true'"}/>
        <g:link class="field-url" url="${dataUrl}"><span class="ui-icon-linkgo"></span></g:link>
        <span class="help-block bri-help-block" style="clear:both;">Enter a URL</span>
         </sec:ifLoggedIn>

          <sec:ifNotLoggedIn>
              <span class="notlogged-replace"> <g:link uri="${dataUrl}" target="_blank">${dataUrl}</g:link> </span>
          </sec:ifNotLoggedIn>

      </div>
    </div>
    </g:each>
  </fieldset>
  <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SETAPPROVAL">
  <fieldset>
    <div class="clearfix">
      <div class="input">
        <button class="btn error" onclick="javascript:showDeleteConfirmModal();return false;">Delete this Sample Set</button>
      </div>
    </div>
  </fieldset>
  </sec:ifAnyGranted>
</g:form>
<g:javascript>
  <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SETAPPROVAL">
    var showDeleteConfirmModal = function() {
      $("div#deletesampleset-modal").show();
    };
    var closeDeleteConfirmModal = function() {
      $("div#deletesampleset-modal").hide();
    };
    var closeDeleteErrorModal = function() {
      $("div#delete-error-modal").hide();
    };
    var deleteSampleSet = function() {
      $.post(getBase()+"/sampleSet/markForDeletion/${sampleSet.id}", {}, function(json) {
        closeDeleteConfirmModal();
        if (json.error) {
          $("div#delete-error-modal").show();
        } else {
          window.location = "${createLink(controller:'sampleSet', action:'list')}";
        }
      });
    };
  </sec:ifAnyGranted>
  $(document).ready(function() {
    // initialize the clinical datasource on load
    var clinicalDatasource = $("#clinical-datasource").val();
    localStorage.setItem("clinical-datasource", clinicalDatasource);
    $("#raw-signal-type").change(function() {
      var args = { signalType: $(this).val() };
      $.post(getBase()+"/sampleSet/rawSignalDatatype/${sampleSet.id}", args);
    });
    $("#default-display-type").change(function() {
      var args = { signalType: $(this).val() };
      $.post(getBase()+"/sampleSet/defaultSignalDatatype/${sampleSet.id}", args);
    });
    $("#publish-gxb").change(function() {
      var args = { publish: $(this).is(":checked") };
      $.post(getBase()+"/sampleSet/publishToGxb/${sampleSet.id}", args);
    });
    $("#set-privacy").change(function() {
      var args = { isPublic: $(this).is(":checked") };
      $.post(getBase()+"/sampleSet/setPrivacy/${sampleSet.id}", args);
    });
    $("div#tab-info").delegate("#clinical-datasource", "change", function() {
      var value = $(this).val() === '-select-' ? null : $(this).val();
      var args = { clinicalDatasource: value };
      $.post(getBase()+"/sampleSet/clinicalDatasource/${sampleSet.id}", args);
    });
    $("input.sampleSetLink").blur(function() {
      var field = $(this);
      var url = field.val();
      var linkTypeId = field.attr("id");
      var args = { url: url, linkTypeId: linkTypeId };
      $.post(getBase()+"/sampleSet/setSampleSetLink/${sampleSet.id}", args, function() {
        field.siblings(".field-url").attr("href", url);
      });
    });
  });
</g:javascript>
<sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SETAPPROVAL">
<div id="deletesampleset-modal" class="modal hide">
  <div class="modal-header">
    <a href="#" class="close">×</a>
    <h3>Delete Sample Set</h3>
  </div>
  <div class="modal-body">
    <p><strong>Sample Set:</strong> ${sampleSet.name}</p>
    <p>Upon deletion, you will be redirected to the sample set list page.
      Are you sure you want to delete this sample set?</p>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="deleteSampleSet();">Yes</button>
    <button class="btn" onclick="closeDeleteConfirmModal();">Cancel</button>
  </div>
</div>
<div id="delete-error-modal" class="modal hide">
  <div class="modal-header">
    <a href="#" class="close">×</a>
    <h3>Error: Delete Sample Set</h3>
  </div>
  <div class="modal-body">
    <p>There was a problem deleting the sample set, ${sampleSet.name}.</p>
  </div>
  <div class="modal-footer">
    <button class="btn" onclick="closeDeleteErrorModal();">Close</button>
  </div>
</div>
</sec:ifAnyGranted>