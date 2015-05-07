<div id="notePanel" class="pullout">
  <div class="page-header">
    <h4 style="max-width:100%">Add a New Note</h4>
  </div>
  <div id="noteError" class="alert-message block-message error" style="display:none;"><strong>There was an error adding your note. Please try again.</strong></div>
  <g:form name="noteForm" class="form-stacked">
    <fieldset>
      <div class="clearfix">
        <div class="input" style="text-align:left;">
          <input type="radio" name="level" value="sampleset" checked="checked" onclick="javascript:setSampleSetReference();"/> Sample Set Level
          <span style="margin-left:15px;">&nbsp;</span>
          <input type="radio" name="level" value="gene" onclick="javascript:setGroupSetReference();"/> Dataset Level
          <span style="margin-left:15px;">&nbsp;</span>
          <input type="radio" name="level" value="gene" onclick="javascript:setGeneReference();"/> Gene Level
        </div>
      </div>
      <div class="clearfix">
        <label for="note">Your note</label>
        <div class="input" style="text-align:left;">
          <g:textArea name="note" rows="3" cols="40" style="width:350px;"/>
        </div>
      </div>
      <div class="clearfix">
      	<label class="checkbox">
      	<input type="checkbox" id="privacy" name="privacy" value="private"/> Private
      	</label>
      </div>
      <div class="clearfix">
        <label for="reference">Reference</label>
        <div class="input" style="text-align:left;">
          <g:textField name="reference" style="width:350px;" disabled="disabled"/>
        </div>
      </div>
    </fieldset>
  </g:form>
  <div class="button-actions" style="clear: both;">
    <button class="btn" onclick="closeNotePanel();">Close</button>
    <button id="sendBtn" class="btn primary" onclick="addNote();">Add Note</button>
  </div>
</div>
<div id="noteConfirmPanel" class="pullout">
  <div style="padding:10px;">
    <span style="padding-right:5px;"><strong>Your note has been added</strong></span>
  </div>
  <div class="button-actions" style="clear: both;">
    <button class="btn" onclick="closeNoteConfirmPanel();">Close</button>
  </div>
</div>
<g:javascript>
  var callbackFunc = null;
  var setReference = function(reference) {
    $("form#noteForm input#reference").val(reference);
  };
  var setSampleSetReference = function() {
    setReference('SAMPLESET:'.concat(currentSampleSetID));
  };
  var setGeneReference = function() {
    setReference('PROBE:'.concat(currentProbeID).concat(',GENESYMBOL:').concat(currentGeneSymbol));
  };
  var setGroupSetReference = function() {
    setReference('DATASET:'.concat(currentGroupSetID));
  };
  var addNote = function() {
    var url = "${createLink(controller:params.controller, action:params.action, id:params.id)}";
    var reference = $("form#noteForm input#reference").val();
    var privacy = $("form#noteForm input#privacy").is(':checked');
    var note = $("form[name='noteForm'] textarea[name='note']").val();
    var args = { url:url, reference:reference, note:note, privacy:privacy};
    $.post(getBase()+"/notes/create", args, function(json) {
      if (json.error) {
        $("div#noteError strong").html(json.message);
        $("div#noteError").show();
      } else {
        callbackFunc.call();
        $("div#noteError").hide();
        closeNotePanel();
        $("div#noteConfirmPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
      }
    });
  };
  var showNotePanel = function(callback) {
    callbackFunc = callback;
    $("input[name='level']:checked").trigger("click");
    $("div#notePanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
  };
  var closeNotePanel = function() {
    $("div#notePanel textarea").val("");
    $("div#notePanel").hide().css({ top:0, left:0 });
    $("div#noteError").hide();
  };
  var closeNoteConfirmPanel = function() {
    $("div#noteConfirmPanel").hide().css({ top:0, left:0 });
  };
</g:javascript>

