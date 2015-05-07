<link rel="stylesheet" href="${resource(dir: 'css', file: 'farbtastic.css')}"/>
<g:javascript src="farbtastic.js"/>
<g:javascript>
  $(document).ready(function() {
    $('#colorpicker').farbtastic('#colorpicker-color');
    $('#colorpicker-dialog').draggable({ handle: 'div.modal-header'});
    $("button.closeColorpicker").click(function() {
      $('#colorpicker-dialog').css('z-index', 20000).hide("drop");
    });
  });
</g:javascript>
<div id="colorpicker-dialog" class="modal dialog" style="display:none; width: 296px; z-index: 10000;">

    <div class="modal-header" style="cursor: pointer; background-color: #f7f7f7;">
      <h4 id="colorpicker-label"></h4>
    </div>
    <div class="modal-body" style="text-align: center;">
    <div id="colorpicker" style="padding-left: 30px;"></div>
    <input type="text" id="colorpicker-color" name="colorpicker-color" value="#123456" class="top-margin-less medium" disabled="true" style="text-align: center;"/>
</div>
    <div class="modal-footer">
    <button class="colorpicker-save-close btn primary">Save & Close</button>
    <button class="closeColorpicker btn">Close</button>
    </div>
    <g:hiddenField name="item-id"></g:hiddenField>
    <g:hiddenField name="item-class"></g:hiddenField>
</div>