<g:javascript>
  $(document).ready(function() {
    $('#subset-options-dialog').draggable({ handle: 'div.modal-header'});
    $("button#close-subset").click(function() {
      $('#subset-options-dialog').hide("drop");
      return false;
    });
  });
</g:javascript>
<div id="subset-options-dialog" class="modal dialog" style="display:none; width: 500px; z-index: 15001;">
  <div class="modal-header"><h4>Create Subset</h4></div>
  <g:form controller="datasetGroupSet" action="createSubset" id="${groupSetId}" style="padding:0;margin:0;">
  <div class="modal-body">
    <div>Select which information you'd like to copy from the current sample set to your new subset.</div>

      <fieldset>
        <g:hiddenField name="groupId"/>
        <div class="clearfix">
          <label id="options">Options</label>
          <div class="input">
            <ul class="inputs-list">
              <li>
                <label>
                  <g:checkBox name="options" value="copyAnnotations" checked="${true}"/>
                  <span>Copy all annotations</span>
                </label>
              </li>
              <li>
                <label>
                  <g:checkBox name="options" value="copyFiles" checked="${true}"/>
                  <span>Copy all files</span>
                </label>
              </li>
            </ul>
          </div>
        </div>
      </fieldset>
  </div>
  <div class="modal-footer">
    <button id="create" class="btn primary" type="submit">Create</button>
    <button id="close-subset" class="btn">Cancel</button>
  </div>
    </g:form>
</div>