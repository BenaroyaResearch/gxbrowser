<g:javascript>
  $(document).ready(function() {

    function rgb2hex(rgb) {
     rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
     return "#" +
      ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) +
      ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) +
      ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
    }

    $('#settings-dialog').draggable({ handle: 'div.modal-header'});
    $("button#close-settings").click(function() {
      $('#settings-dialog').hide("drop");
    });

    $('.hex-color').click(function(e) {
      var hidden = $('#presetColors').toggle().is(':hidden');
      if (!hidden)
      {
        $('#presetColors').position({
          my: 'left top',
          of: '.hex-color',
          at: 'left bottom',
          offset: '0 2'
        });
      }
      e.stopPropagation();
    });

    $('.preset-color').click(function() {
      var hex = rgb2hex($(this).find('div').css('background-color'));
      var groupId = $('#groups-settings').val();
      $('.hex-color').css('background-color',hex);
      $.farbtastic('#colorpicker').setColor(hex);
      $.ajax({
        type    : 'POST',
        url     : $('.hex-color').attr('id'),
        data    : { color: hex },
        async   : false,
        cache   : false,
        success : function() {
          updateHistogram(groupId, true);
          $('#presetColors').hide();
        }
      });
    });

    function moveGroup(data) {
      var groupId = $('#groups-settings').val();
      $.ajax({
        type    : 'POST',
        url     : '../../datasetGroup/moveGroup/'+groupId,
        data    : data,
        async   : false,
        cache   : false,
        success : function(result) {
          // need a better way of handling reload after move
          location.reload();
        }
      });
    }

    $('button#move-group').click(function() {
      moveGroup({ moveType: $('#position').val(), reference: $('#position-group').val() });
    });

    $('button#move-group-position').click(function() {
      moveGroup({ moveType: 'position', newPosition: $('input[name="displayOrder"]').val() });
    });

    $('select[name="header"]').change(function() {
      var header = $(this).val();
      $.ajax({
        type    : 'POST',
        url     : '../../datasetGroupSet/sampleValues/${groupSet.id}',
        data    : { header: header },
        async   : false,
        cache   : false,
        success : function(json) {
          $('#numColumns span').html(json["sampleValueCount"]);
          $('#sampleValues').html(json["sampleValues"].join(', '));
        }
      });
    });
  });
</g:javascript>
<div id="settings-dialog" class="modal dialog" style="display:none; width: 550px; z-index: 15001;">
  <div class="modal-header" style="cursor: pointer;background-color: #f7f7f7;"><h4>Group Settings</h4></div>
  <div class="modal-body">

<form id="datasetGroup">

        <fieldset>
          <div class="clearfix">
            <label for="groups-settings">Group</label>
            <div class="input">
              <g:select name="groups-settings" from="${groupSet.groups}" optionKey="id" optionValue="name"/>
            </div>
          </div>
          <div class="clearfix">
            <label for="name">Name</label>
            <div class="input">
              <g:textField name="name" class="editable-text xlarge"/>
            </div>
          </div>
          <div class="clearfix">
            <label for="displayOrder">Display Order</label>
            <div class="input">
              <input type="number" name="displayOrder" id="displayOrder" class="spinner-text" min="1" max="${totalGroups}" ${(totalGroups < 2) ? "disabled='true'" : ""}/>
              <button id="move-group-position" class="btn small primary" type="submit" ${(totalGroups < 2) ? "disabled='true'" : ""}>Move</button>
            </div>
          </div>
          <div class="clearfix">
            <label for="position">Move this group to</label>
            <div class="input">
              <div class="inline-inputs">
              <g:select name="position" from="${['before','after']}" disabled="${(totalGroups < 2)}" class="small"/>
              <g:select name="position-group" from="${groupSet.groups}" optionKey="id" optionValue="name" disabled="${(totalGroups < 2)}" class="medium"/>
                <button id="move-group" class="btn small primary" type="submit" ${(totalGroups < 2) ? "disabled='true'" : ""}>Move</button>
                </div>
            </div>
          </div>
          <div class="clearfix">
            <label>Color</label>
            <div class="input">
              <div class="hex-color btn" style="height: 13px;width: 100px;float: left;"></div>
              <button class="colorpicker-open btn image-btn" style="margin-top: 2px;"><span class="ui-icon-color"></span></button>
            </div>
          </div>
        </fieldset>
  </form>
  <g:form controller="datasetGroupSet" action="createGroups" id="${groupSet.id}">
        <fieldset>
          <div class="page-header"><h5>Create Groups</h5></div>
          <g:hiddenField name="splitGroupId" id="splitGroupId"/>
          <div class="clearfix">
            <label>Split by</label>
            <div class="input">
                 <g:select name="header" from="${headers ?: []}" optionKey="label" optionValue="header"/> <span id="numColumns" class="tiny-text">(split into <span>${sampleValueCount}</span> groups)</span>
                <span class="help-block">Ex: <span id="sampleValues">${sampleValues ? sampleValues.join(', ') : ""}</span></span>
            </div>
          </div>
          <div class="clearfix">
            <label>Name by</label>
            <div class="input">
                 <g:select name="namingConvention" from="${['Appending column name to group name', 'Using column name']}" class="xlarge"/>
            </div>
          </div>
          <div class="clearfix">
            <div class="input">
              <button type="submit" class="btn primary">Create Groups</button>
            </div>
          </div>

        </fieldset>
      </g:form>

</div>
  <div class="modal-footer">
    <button id="close-settings" class="btn">Close</button>
  </div>
</div>
<g:render template="/common/default_swatches_template"/>