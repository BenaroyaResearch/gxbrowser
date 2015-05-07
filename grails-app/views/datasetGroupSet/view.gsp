<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="layout" content="sampleSetMain"/>

<title>Sample Set Sample Groups</title>

<g:javascript src="bootstrap-modal.js"/>
<g:javascript src="jquery.tinyscrollbar.min.js"/>
<g:javascript src="highcharts/highcharts.js"/>
<g:javascript src="highcharts/exporting.js"/>
<g:javascript src="charting/chartify.js"/>
<g:javascript>
    var signalDataTable = "${signalDataTable}";
    var editable = ${editable};
    var groupSetMax = 0.0;
    var updateHistogram;
    $(document).ready(function() {
      var totalWidth = 0;
      $('.group').each(function() {
        totalWidth += $(this).outerWidth(true);
      });
      $('body').css('min-width',totalWidth+50+'px');

      var probeId;
      $.get(getBase()+'/charts/getProbeId', { sampleSetId:${groupSet.sampleSet.id}, signalDataTable:signalDataTable }, function(result) {
        probeId = result;
        var args = { sampleSetId: ${groupSet.sampleSet.id}, groupSetId: ${groupSet.id}, probeId: result, chartType: "highcharts" };
        $.getJSON(getBase()+'/charts/getHistogramDataAsGroups', args, function(json) {
          $.each(json, function(groupId, val) {
            groupSetMax = val["max"];
            $("#histogram"+groupId).chartify('drawHistogram', { data: val["data"], colors: val["colors"], showExport: false, maxY: val["max"] });
          });
        });
      });

      function tooltipHtml(json) {
        var sample = json["sample"];
        var html = "Sample ID: " + sample["sampleId"] + "<br/>";
        if (sample["sampleRow"])
        {
          $.each(sample["sampleRow"], function(key, value) {
            html += key + ": " + value + "<br/>";
          });
        }
        if (sample["arrayData"])
        {
          $.each(sample["arrayData"], function(key, value) {
            html += key + ": " + value + "<br/>";
          });
        }
        return html;
      }

      function setGroupInfo(groupId, openDialog, x, y) {
        $.ajaxSetup({cache: false});
        $.getJSON(getBase()+'/datasetGroup/groupInfo/'+groupId, function(result) {
          var groupInfo = result[0];
          var nameTextField = $('#settings-dialog form input:text[name="name"]');
          nameTextField.attr('id', groupId+"::name");
          nameTextField.val(groupInfo["name"]);
          var displayOrderField = $('#settings-dialog form input[name="displayOrder"]');
          displayOrderField.attr('id', '../../datasetGroup/setter/'+groupId);
          displayOrderField.val(groupInfo["displayOrder"]);
          var hexColorDiv = $('.hex-color');
          hexColorDiv.attr('id', '../../datasetGroup/setHexColor/'+groupId);
          hexColorDiv.css('background-color', groupInfo['hexColor']);
          $('#splitGroupId').val(groupId);
          $("#settings-dialog form button.colorpicker-open").attr("id", groupId);
          if (openDialog) {
            $("#settings-dialog").show("drop");
          };
        });
        $.ajaxSetup({cache: true});
      }

      // Open the color picker
      $('.colorpicker-open').click(function(e) {
        var id = this.id;
        $.ajaxSetup({cache: false});
        $.getJSON(getBase()+"/datasetGroup/groupInfo/"+id, function(result) {
          var groupInfo = result[0];
          $('#colorpicker-dialog div #colorpicker-label').html(groupInfo["name"]);
          $('#colorpicker-dialog #item-id').val(groupInfo["id"]);
          $('#colorpicker-dialog #item-class').val(groupInfo["classType"]);
          var color = groupInfo["hexColor"];
          if (color == null)
          {
            color = '#123456';
          }
          $.farbtastic('#colorpicker').setColor(color);
          var x = e.pageX + 40;
          var y = e.pageY - window.pageYOffset - 100;
          $('#colorpicker-dialog').css('z-index',20001).show('drop');
        });
        $.ajaxSetup({cache: true});
        return false;
      });

      // Save the color from the color picker to the appropriate domain class
      $('.colorpicker-save-close').click(function() {
        var id = $('#colorpicker-dialog #item-id').val();
        var type = $('#colorpicker-dialog #item-class').val();
        $.ajax({
          type    : "POST",
          url     : "../../"+type+"/setHexColor/"+id,
          data    : { color: $('#colorpicker-color').val().toUpperCase() },
          async   : false,
          cache   : false,
          success : function() {
            if (type == 'datasetGroup')
            {
              updateHistogram(id, true);
              if ($('#groups-settings').val() == id)
              {
                setGroupInfo(id, false, 0, 0);
              }
            }
            $('#colorpicker-dialog').hide("drop");
          }
        });
      });

      // open the group settings dialog
      $('.settings-open').click(function(e) {
        var groupId = $(this).attr("id");
        $('#groups-settings').val(groupId);
        var x = e.pageX;
        var y = e.pageY - window.pageYOffset + 20;
        setGroupInfo(groupId, true, x, y);
        return false;
      });

      // change the group information on group settings dialog
      $('#groups-settings').change(function() {
        setGroupInfo($(this).val(), false, 0, 0);
      });

      // change the name of the group in the group settings dialog
      $('#settings-dialog>div.modal-body>form>fieldset>div>div.input>input:text[name="name"]').change(function() {
        var newName = this.value;
        if (newName != "")
        {
          var groupId = $('select#groups-settings').val();
          $('select#groups-settings>option[value="'+groupId+'"]').text(newName);
          $('select#position-group>option[value="'+groupId+'"]').text(newName);
          $('div#group-name>div#group-'+groupId+'>strong').html(this.value);
          $('select.receiveGroup>option[value="'+groupId+'"]').each(function(index) {
            $(this).text(newName);
          });
        }
      });

      // delete group functionality
      $('.delete').click(function() {
        var form = $(this).parent();
        $('#deleteConfirmationDialog').modal({
          backdrop: 'static',
          show: true
        });
        $('#deleteGroupBtn').click(function() {
          form.submit();
          $('#deleteConfirmationDialog').modal('hide');
        });
        $('#deleteCancelBtn').click(function() {
          $('#deleteConfirmationDialog').modal('hide');
        });
        return false;
      });

      $(".create-subset").click(function() {
        var group = $(this).closest('.group');
        var groupId = group.attr("id");
        $("#subset-options-dialog").find("form>div.modal-body>fieldset>input#groupId").val(groupId);
        $("#subset-options-dialog").show("drop");
      });

      if (editable)
      {
        $('#sortable-groups').sortable({
          axis        : 'x',
          handle      : '.handle',
          cursor      : 'pointer',
          update      : function(event, ui) {
            var groupId = ui.item.attr('id');
            var displayOrder = ui.item.index() + 1;
            $.ajax({
              type    : 'POST',
              url     : '../../datasetGroup/moveGroup/'+groupId,
              data    : { moveType: 'position', newPosition: displayOrder },
              async   : false,
              cache   : false
            });
          }
        });
      }

      $('.scrollable-list').tinyscrollbar();

      $('.sortable-list').delegate('li', {
        mouseover: function(e) {
          if (!e.metaKey && !e.ctrlKey)
          {
            $(this).qtip({
              position: {
                my    : 'bottom center',
                at    : 'top left',
                adjust: {
                  x: 30,
                  y: 0,
                  method: 'shift flip'
                },
                viewport: $(window)
              },
              content: {
                text: function(api) {
                  var html = 'No information found';
                  $.ajax({
                    type    : 'GET',
                    url     : '../../datasetGroupDetail/getSampleInfo',
                    data    : { id: $(this).attr('id') },
                    async   : false,
                    cache   : false,
                    success : function(result) {
                      html = tooltipHtml(result);
                    }
                  });
                  return html;
                }
              },
              show: {
                solo: true,
                ready: true,
                delay: 1000
              },
              hide: {
                event: 'click mouseleave',
                inactive: 3000
              },
              style: {
                classes: 'ui-tooltip-tipsy'
              }
            });
          }
        },
        mouseenter: function() {
          $(this).addClass('highlight');
        },
        mouseleave: function() {
          $(this).removeClass('highlight');
        }//,
//        dblclick: function() {
//          $('#sample-annotation-editor').dialog('moveToTop').dialog('open');
//        }
      });

      if (editable)
      {
        $('.sortable-list li').liveDraggable();
      }

      function updateSelectedText(group) {
        var total = group.find('li').length;
        var selected = group.find("li.ui-selected").length;
        group.closest('form').find('.num-samples').html(selected + " of " + total);
      }

      function updateButtons(from, to)
      {
        if (jQuery.trim(from.html()) == "")
        {
          var fGroup = from.closest(".group");
          fGroup.find("div.handle>form>button.save-image").attr("disabled", true);
          fGroup.find("div.handle>form>button.delete").removeAttr("disabled");
          from.closest("form").parent().find("div>button.create-subset").attr("disabled", true);
        }

        var tGroup = to.closest(".group");
        tGroup.find("div.handle>form>button.save-image").removeAttr("disabled");
        tGroup.find("div.handle>form>button.delete").attr("disabled", true);
        to.closest("form").parent().find("div>button.create-subset").removeAttr("disabled");
      }

      updateHistogram = function(groupId, colorOnly) {
        $.ajax({
          type: 'GET',
          url: '../../datasetGroup/getData/'+groupId,
          data: { probeId: probeId, floor: 10, chartLibrary: 'highcharts' },
          async: false,
          cache: false,
          success: function(json) {
            $("#histogram"+groupId).chartify('redraw', { newData: json["data"], newColors: json["colors"], colorOnly: colorOnly, showExport: false, maxY: groupSetMax });
          }
        });
      }

      function transferSamples(fromGroup, toGroup, samples, clone) {
        var detailId = []
        samples.each(function() {
          detailId.push($(this).attr('id'));
        });
        if (fromGroup.attr('id') != toGroup.attr('id'))
        {
          $.post('../../datasetGroupSet/addToGroup/${groupSet.id}',
            { receiveGroup: toGroup.attr('id'), samples: detailId.join(',') },
            function() {
              if (clone)
              {
                fromGroup.find('.ui-selected, .ui-selecting').detach();
              }
              else
              {
                samples.detach();
              }
              samples.removeClass('ui-selected').removeClass('ui-selecting');
              toGroup.append(samples);
              toGroup.closest('.scrollable-list').tinyscrollbar_update();
              fromGroup.closest('.scrollable-list').tinyscrollbar_update();
              updateButtons(fromGroup, toGroup);
              updateSelectedText(fromGroup);
              updateSelectedText(toGroup);
              updateHistogram(fromGroup.attr('id'), false);
              updateHistogram(toGroup.attr('id'), false);
            }
          );
        }
      }

      $('.viewport').droppable({
        accept      : 'li',
        tolerance   : 'pointer',
        hoverClass  : 'droppable-hover',
        drop        : function(e, ui) {
          var item = ui.helper;
          var itemParent = ui.draggable.closest('.sortable-list');
          var currentGroup = $(this).find('ol');
          transferSamples(itemParent, currentGroup, item.find('li'), true);
        }
      });

      $('.sortable-list').delegate('li', {
        mousedown: function(e) {
          $(this).addClass('ui-selecting');
        },
        mouseup: function(e) {
          $(this).removeClass('ui-selecting');
          if (!e.metaKey && !e.ctrlKey)
          {
            $(this).siblings().removeClass('ui-selected').removeClass('ui-selecting');
            if (!$(this).hasClass('ui-selected'))
            {
              $(this).addClass('ui-selected');
            }
          }
          else
          {
            $(this).toggleClass('ui-selected');
          }
          updateSelectedText($(this).parent());
        }
      });

      $('.transfer-samples').click(function() {
        var toGroupId = $(this).prev().val();
        var fromGroup = $(this).closest('form').find('.sortable-list');
        var toGroup = $(this).closest('.group').siblings('#'+toGroupId).find('.sortable-list');
        var samples = fromGroup.find('li.ui-selected, li.ui-selecting');
        transferSamples(fromGroup, toGroup, samples, false);
        return false;
      });

      $('.save-image').click(function(e) {
        var groupId = $(this).closest('.group').attr('id');
        var chartName = "${groupSet.name.replaceAll("\\s+","_")}-";
        chartName = chartName.concat($.trim($(this).closest('.group').find("#group-name").text().replace(/s+/g," ")).replace(/ /g,"_"));
        $("#histogram"+groupId).chartify('saveChart', { filename: chartName });
        return false;
      });

      $('.copy-image').click(function() {
        var groupId = $(this).closest('.group').attr('id');
        $("#histogram"+groupId).chartify('copyToClipboard');
        return false;
      });

      $("form#create-group-form>fieldset>div>div.input>input:text[name=name]").keyup(function() {
        if ($(this).val() != "")
        {
          $("button#create-group").removeAttr("disabled");
        }
        else
        {
          $("button#create-group").attr("disabled", "true");
        }
      });
    });

    var openModal = function(modalId) {
      $("div#"+modalId).show();
    };

    var closeModalDialog = function(modalId) {
      $("div#"+modalId).hide();
    };

    var submitForm = function(formId) {
      $("form#"+formId).submit();
    }
</g:javascript>

</head>

<body>

<div class="sampleset-container no-wrap-content">

  <g:if test="${flash.message}">
    <div class="message shadow ui-corner-all">${flash.message}</div>
  </g:if>

  <g:hasErrors bean="${groupSet}">
    <g:eachError bean="${groupSet}">
      <div class="error-message shadow ui-corner-all">
        <g:message error="${it}"/><br/>
      </div>
    </g:eachError>
  </g:hasErrors>

  <div class="page-header">
    <h2>${groupSet.name} <small style="padding-left: 20px;"></small></h2>
  </div>

  <div style="margin-bottom: 10px;">
    <button class="btn primary" onclick="openModal('new-group-modal');" ${!editable ? "disabled='true'" : ""}>New Group</button>
    <button class="btn primary" onclick="openModal('split-groupset-modal');" ${!editable ? "disabled='true'" : ""}>Split Group Set</button>
    <button class="btn primary" onclick="openModal('reset-groupset-modal');" ${!editable ? "disabled='true'" : ""}>Reset Group Set</button>
    <g:link controller="geneBrowser" action="show" id="${groupSet.sampleSet.id}" params="[defaultGroupSetId:groupSet.id]" class="btn info" target="_blank">View in Gene Expression Browser</g:link>
    <g:link controller="sampleSet" action="show" id="${groupSet.sampleSet.id}" class="btn info">Return to Annotation Tool</g:link>
  </div>

  <div class="page-header">
    <h4>Groups</h4>
  </div>

  <div id="sortable-groups" class="groups-row">
    <g:each in="${groupSet.groups}" var="group">
      <g:set var="isEmpty" value="${group.groupDetails.empty}"/>
      <div class="group group-well" id="${group.id}">
        <div class="handle" style="text-align:right;padding:1px;">
        %{--<div class="ui-icon-drag" style="float:left;margin-left: 3px;margin-top:3px;"></div>--}%
          <g:form action="deleteGroup" id="${group.id}" style="clear:both;margin:0;padding:0;">
            <button id="${group.id}" class="colorpicker-open btn image-btn" ${!editable ? "disabled='true'" : ""}><span class="ui-icon-color"></span></button>
            <button id="${group.id}" class="settings-open btn image-btn" ${!editable ? "disabled='true'" : ""}><span class="ui-icon-settings"></span></button>
            <button class="save-image btn image-btn" ${isEmpty ? "disabled='true'" : ""}><span class="ui-icon-save"></span></button>
            <button class="copy-image btn image-btn"><span class="ui-icon-copyclipboard"></span></button>
            <button class="delete btn image-btn" type="submit" ${isEmpty && editable ? "" : "disabled='true'"}><span class="ui-icon-delete"></span></button>
          </g:form>
        </div>
        <div class="histogram-container">
          <div class="histogram" id="histogram${group.id}"></div>
        </div>
        <div id="group-name" style="text-align:center;">
          <div id="group-${group.id}"><strong>${group.name}</strong></div>
        </div>
        <div style="padding: 5px;">
          <g:form method="post" action="addToGroup" id="${groupSet.id}">
            <g:hiddenField name="groupId" value="${group.id}"/>
            <g:hiddenField name="samples" value=""/>
            <g:if test="${group.groupDetails}">
              <g:draggableList name="${group.id}" from="${group.groupDetails.sort { it.displayOrder }.sort { it.sample.id }}" optionKey="id" optionValue="sample"/>
            </g:if>
            <g:else>
              <g:draggableList name="${group.id}" from="${[]}" empty="true"/>
            </g:else>
            <div style="text-align:center;">
              <span class="hint tiny-text"><span class="num-samples">0</span> samples selected</span>
            </div>
            <g:if test="${totalGroups > 1}">
              <div class="top-margin" style="text-align: center;">
                <div><strong>Move selected samples to group</strong></div>
                <div class="top-margin-less">
                  <g:selectField name="receiveGroup" from="${groupSet.groups}" optionKey="id" optionValue="name" exclude="${group}" class="medium receiveGroup"/>
                  <button class="transfer-samples btn small primary" type="submit" ${!editable ? "disabled='true'" : ""}>Move</button>
                </div>
              </div>
            </g:if>
          </g:form>
          <div class="top-margin" style="text-align: center;"><button class="create-subset btn" ${isEmpty ? "disabled='true'" : ""}>Create sample set from this group</button></div>
        </div>
      </div>
    </g:each>
  </div>
</div>

<div id="deleteConfirmationDialog" class="modal" style="display:none;">
  <div class="modal-header">
    <h3>Are you sure?</h3>
  </div>
  <div class="modal-body">
    <p>Are you sure you want to delete this group?</p>
  </div>
  <div class="modal-footer">
    <a id="deleteGroupBtn" href="#" class="btn primary" ${!editable ? "disabled='true'" : ""}>Delete</a>
    <a id="deleteCancelBtn" href="#" class="btn secondary">Cancel</a>
  </div>
</div>

<div id="new-group-modal" class="modal hide" style="width:350px;">
  <div class="modal-header">
    <h4>New Group</h4>
  </div>
  <div class="modal-body">
    <g:form name="create-group-form" controller="datasetGroup" action="create" id="${groupSet.id}">
      Name <g:textField name="name" style="margin-left:5px;"/>
    </g:form>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="submitForm('create-group-form');" ${!editable ? "disabled='true'" : ""}>Create</button>
    <button class="btn" onclick="closeModalDialog('new-group-modal');">Cancel</button>
  </div>
</div>

<div id="split-groupset-modal" class="modal hide" style="width:350px;">
  <div class="modal-header">
    <h4>Split Group Set</h4>
  </div>
  <div class="modal-body">
    <g:form name="split-groupset-form" controller="datasetGroupSet" action="createGroupsFromAll" id="${groupSet.id}">
      Split Using <g:select name="header" from="${headers}" optionKey="label" optionValue="header" style="margin-left:5px;"/>
    </g:form>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="submitForm('split-groupset-form');" ${!editable ? "disabled='true'" : ""}>Split Group Set</button>
    <button class="btn" onclick="closeModalDialog('split-groupset-modal');">Cancel</button>
  </div>
</div>

<div id="reset-groupset-modal" class="modal hide" style="width:350px;">
  <div class="modal-header">
    <h4>Reset Group Set</h4>
  </div>
  <div class="modal-body">
    This will remove all current groups and create one group with all samples! Are you sure you want to reset?
  </div>
  <div class="modal-footer">
    <g:link controller="datasetGroupSet" action="resetGroups" id="${groupSet.id}" class="btn primary">Yes, reset!</g:link>
    <button class="btn" onclick="closeModalDialog('reset-groupset-modal');">No</button>
  </div>
</div>



<g:render template="/common/createSubsetTemplate" model="[groupSetId:groupSet.id]"/>
<g:render template="/common/colorPickerTemplate"/>
<g:render template="/common/groupSettingsTemplate" model="[groupSet:groupSet, headers:headers, totalGroups:totalGroups, sampleValueCount:sampleValueCount]"/>


</body>
</html>

