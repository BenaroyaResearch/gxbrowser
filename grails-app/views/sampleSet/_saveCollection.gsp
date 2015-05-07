<%@ page import="grails.converters.JSON" %><g:javascript>
  var savedCollections = ${savedCollections as JSON};
  var saveCollection = function() {
    var name = $("div#savecollection-modal").find("input#collectionName").val();
    var args = { sampleSetIds: $("input#ssCollection").val(), collectionName: name };
    $.post(getBase()+"/datasetCollection/saveCollection", args)
    $("div#savecollection-modal").find("input#collectionName").val("");
    closeModal("savecollection-modal");
  };
  var checkCollName = function() {
    var name = $("div#savecollection-modal").find("input#collectionName").val();
    var overwrite = $("div#savecollection-modal").find("input#overwrite").is(":checked");
    if (overwrite || $.inArray(name, savedCollections) === -1)
    {
      $("button#savecollection-button").removeAttr("disabled");
    }
    else
    {
      $("button#savecollection-button").attr("disabled", true);
    }
  };
</g:javascript>
<div id="savecollection-modal" class="modal hide" style="width: 300px;">
  <div class="modal-header">
    <a href="#" class="close">×</a>
    <h3>Save As Collection</h3>
  </div>
  <div class="modal-body">
    <p>Enter a name for this collection:<br/>
    <g:textField name="collectionName" onkeyup="checkCollName();"/>
    </p>
    <p>
      <g:checkBox name="overwrite" checked="false" onchange="checkCollName();"/> Overwrite, if collection already exists
    </p>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="saveCollection();" id="savecollection-button" disabled="true">Save</button>
    <button class="btn" onclick="closeModal('savecollection-modal');">Cancel</button>
  </div>
</div>