<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>
  <title>Sample Set Tab Configurator</title>

  <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>


  <style type="text/css">
    .tab-col {
      padding: 10px 20px;
      /*border-left: 1px dashed #cccccc;*/
      float: left;
    }
    .tab-col input {
      text-align: center;
    }
    .sortable-fields {
      padding: 0;
      margin: 0;
      overflow-y: auto;
      border: 1px solid #999999;
    }
    .sortable-fields .drag-item {
      list-style-type: none;
      padding: 5px 8px;
      border-bottom: 1px solid #FFFFFF;
      white-space: nowrap;
      overflow-x: hidden;
      color: #333333;
    }
    .sortable-fields .drag-item:hover {
      color: #333333;
      background-color: #79b2ea;
      cursor: move;
    }
    .sortable-fields .drag-item:last-child {
      border-bottom: none;
    }
    .template:hover {
      background-color: #f3f3f3;
    }
    .gene {
      background-color: #e2d5f0;
    }
    .ben_tg2 {
      background-color: #d1eed1;
    }
    .values {
      background-color: #ffcccc;
    }

    .button-group {
      vertical-align: bottom;
    }

    .autofill {
      width: 90px;
      float: left;
      /*display: block;*/
      /*position: relative;*/
      text-align: left;
      border-radius: 0;
    }
    .reset {
      text-align: center;
      width: 30px;
      border-radius: 0;
    }
    .delete {
      text-align: center;
      width: 30px;
      border-radius: 0;
    }

    .autofill-container {
      margin-top: 8px;
    }

    .autofillDown:after {
      content:" ";
      display: block;
      position:absolute;
      width: 0px;
      height: 0px;
      font-size: 1px;
      top:9px;
      right: 15px;
      border-color: #666 transparent;
      border-style: solid solid none;
      border-width: 6px 6px medium;
    }

    .autofillUp:after {
      content:" ";
      display: block;
      border: 1px solid red;
      position:absolute;
      width: 0px;
      height: 0px;
      font-size: 1px;
      top:9px;
      right: 15px;
      border-color: #666 transparent;
      border-width: 0 6px 6px;
    }

    #fieldTypes {
      display: none;
    }
    .autofillDropdown {
      position: absolute;
      /*margin-top: -5px;*/
      /*top: 24px;*/
      list-style-type: none;
      border: 1px solid #999999;
      width: 218px;
      -moz-box-shadow: 2px 2px 5px #ccc;
      -webkit-box-shadow: 2px 2px 5px #ccc;
      box-shadow: 2px 2px 5px #ccc;
      /*display: none;*/
    }
    .autofillDropdown li {
      padding: 5px 8px;
      width: 202px;
      /*background-color: #ffffff;*/
      color: #333333;
      border-bottom: 1px solid #CCCCCC;
      white-space: nowrap;
      overflow-x: hidden;
    }
    .autofillDropdown li:last-child {
      border-bottom: none;
    }
    .autofillDropdown li:hover {
      background-color: #CCCCCC;
    }

    li a.close {
      float: right;
      margin-top: -2px;
      color: #000000;
      font-size: 20px;
      font-weight: bold;
      text-shadow: 0 1px 0 #ffffff;
      filter: alpha(opacity=20);
      -khtml-opacity: 0.2;
      -moz-opacity: 0.2;
      opacity: 0.2;
    }
    li a.close:hover {
      color: #000000;
      text-decoration: none;
      filter: alpha(opacity=40);
      -khtml-opacity: 0.4;
      -moz-opacity: 0.4;
      opacity: 0.4;
    }

    li span.fieldLabel {
      float: left;
    }

    ul.template li a.close {
      display: block;
    }

    div.sidebar .sortable-fields li a.close {
      display: none;
    }

    div.autofillDropdown {
      z-index: 100;
    }

     /*.sortable-fields {height:400px;}*/
     /*.template {height:50% !important;}*/

    /*#sortable { list-style-type: none; margin: 0; padding: 0; width: 60%; }*/
	  /*#sortable li { margin: 0 3px 3px 3px; padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; height: 18px; }*/

  </style>

  <g:javascript>
    var deleteContainer = null;
    var deleteTab = function() {
      if (deleteContainer != null)
      {
        reset(deleteContainer.find(".sortable-fields"));
        deleteContainer.detach();
      }
      $('#deletetab-modal').modal('hide');
    };
    var reset = function(container) {
      if (container)
      {
        container.find("li").each(function() {
          $("div.sidebar>ul.sortable-fields").append($(this));
        });
      }
    };
    $(document).ready(function() {
      $("#error-namesave-modal").modal({ backdrop:true });

      var updateHeights = function() {
        var maxHeight = $(window).height() - 300;
        $(".sortable-fields").height(maxHeight);
//        $(".template").height(maxHeight);

var templateHeight = $(window).height() - 600;
//        $(".sortable-fields").height(templateHeight);
        $(".template").height(templateHeight);
      };



      updateHeights();

      var updateSortables = function() {
        $(".sortable-fields").sortable({
          connectWith: ".sortable-fields",
          tolerance: "pointer"
        });
        $(".sortable-fields").droppable();

        $(".content").sortable({ items: ".tab-col", tolerance: "pointer" });
      };
      updateSortables();

      $("button#add-col").click(function() {
        var html = "<div class='tab-col span5'>" +
         "<div class='tabs-config-box'>" +
          "<input type='text' name='tabName' placeholder='enter tab name here' title='Enter a tab name' class='config-tab-name'/>" +
          "<div class='autofill-container'>" +
          "<div class='button-group'>" +
          "<a href='#' class='button autofill autofillDown'>Autofill</a>" +
          "<a href='#' class='button reset'>Reset</a>" +
          "<a href='#' class='button delete'>Delete</a>" +
          "</div></div>" +
          "<ul class='sortable-fields template'></ul></div></div>";
        $("div.content").append(html);
        updateHeights();
        updateSortables();
      });

      var saveConfiguration = function() {
        var errorFound = false;
        var tabs = { id: ${sampleSet.id} };
        var tabOrder = [];
        $(".tab-col").each(function(i) {
          var name = $(this).find("input[name='tabName']").val();
          var tabname = "tab-"+name;
          if ($.trim(name) === "")
          {
            errorFound = true;
            $('#error-namesave-modal').modal('show');
//            $("div#error-namesave-modal").show();
          }
          var tabfields = new Array();
          $(this).find(".sortable-fields>li").each(function() {
            tabfields.push(this.id);
          });
          tabs[tabname] = tabfields;
          tabOrder.push(tabname);
        });
        tabs["order"] = tabOrder;
        if (!errorFound)
        {
          $.post(getBase()+"/sampleSetTabConfigurator/save", tabs);
        }
      };

      var toggleAutofillPanel = function(button) {
        $("div#fieldTypes").toggle().insertAfter(button);
      };

      var autofill = function(newContainer, ftype) {
        $(".sortable-fields>li."+ftype).each(function() {
          newContainer.append($(this));
        });
      };

      var removeItem = function(item) {
        $("div.sidebar>ul.sortable-fields").append(item);
      };

      $("a.autofill").live("click", function() {
        console.log("autofill");
        $(this).toggleClass("autofillDown");
        $(this).toggleClass("autofillUp");
        toggleAutofillPanel($(this).parent());
      });

      $(".autofillDropdown>li").live("click", function() {
        var autofillContainer = $(this).closest(".autofill-container");
        autofillContainer.find(".autofill").toggleClass("autofillDown");
        autofillContainer.find(".autofill").toggleClass("autofillUp");
        $(this).closest('#fieldTypes').hide();
        // autofill this container with selected field type
        autofill(autofillContainer.parent().find(".sortable-fields"), $(this).attr("class"));
      });

      $("a.reset").live("click", function() {
        var container = $(this).closest(".tab-col").find(".sortable-fields");
        reset(container);
      });

      $("button#reset-all").click(function() {
        $(".tab-col>.tabs-config-box>.sortable-fields>li").each(function() {
          $("div.sidebar>ul.sortable-fields").append($(this));
        });
      });

      $("button#save").click(function() {
        saveConfiguration();
      });

      $("a.delete").live("click", function() {
        deleteContainer = $(this).closest(".tab-col");
        $('#deletetab-modal').modal('show');
      });

      $("ul.template li a.close").live("click", function() {
        removeItem($(this).parent());
      });

//      $("body").mousedown(function() {
//        if ($("div#fieldTypes").is(":visible"))
//        {
//          var aTag = $("div#fieldTypes").closest(".autofill-container").find(".autofill");
//          aTag.toggleClass("autofillDown");
//          aTag.toggleClass("autofillUp");
//          $("div#fieldTypes").hide();
//        }
//      });

    });
  </g:javascript>

</head>

<body>

<div class="sampleset-container">

  <div class="page-header">
    <h2>Tabs: ${sampleSet.name}</h2>
  </div>

  <div class="container-fluid">
    <div class="sidebar well tabconfig">

      <h5>Available Fields</h5>
      <p>Drag from this list to desired tab list</p>
      <ul class="sortable-fields">
        <g:each in="${internalFields.keySet()}" var="field">
          <li class="drag-item ${internalFields.get(field)}" id="internal.${internalFields.get(field)}.${field}" title="${field.encodeAsHumanize()}">
            <span class="fieldLabel"><g:abbreviate maxLength="25" value="${field.encodeAsHumanize()}"/></span>
            <a class="close" href="#">×</a>
          </li>
        </g:each>
        <g:each in="${externalFields.keySet()}" var="fieldType">
          <g:each in="${externalFields.get(fieldType)}" var="field">
            <li class="drag-item ${fieldType}" id="${fieldType}.${field}" title="${field.encodeAsHumanize()}">
              <span class="fieldLabel"><g:abbreviate maxLength="25" value="${field.encodeAsHumanize()}"/></span>
              <a class="close" href="#">×</a>
            </li>
          </g:each>
        </g:each>
      </ul>
    </div>




    <div class="content"  style="max-width: 100%;">
      <div style="margin-left: 20px;">
      	<g:link controller="sampleSet" action="show" id="${sampleSet.id}" class="btn info">Return to Annotation Tool</g:link>
      	<g:link controller="geneBrowser" action="show" id="${sampleSet.id}" class="btn primary">View in Gene Expression Browser</g:link>
          <div class="buttondivider">&nbsp;</div>

      <button id="add-col" class="btn">Add Tab</button>
      <button id="reset-all" class="btn" title="Clear all tabs">Reset</button>
          <div class="buttondivider">&nbsp;</div>
      <button id="save" class="btn primary" title="Save the current tabs configuration">Save</button>
      </div>
      <g:if test="${existingFields}">
        <g:each in="${existingFields.keySet()}" var="tabName" status="i">
          <div class="tab-col span5">
              <div class="tabs-config-box">
            <input type="text" name="tabName" placeholder="enter tab name here" value="${tabName}" title="Enter a tab name" class="config-tab-name"/>
            <div class="autofill-container">
              <div class="button-group">
                <a href="#" class="button autofill autofillDown">Autofill</a>
                <a href="#" class="button reset">Clear</a>
                <a href="#" class="button delete">Delete</a>
              </div>
            </div>
            <ul class="sortable-fields template">
              <g:each in="${existingFields.get(tabName)}" var="exField">
                <li class="drag-item ${exField.externalDb ?: "values"}" id="${exField.externalDb ?: "internal"}.${exField.value}" title="${exField.displayName}">
                  <span class="fieldLabel"><g:abbreviate maxLength="25" value="${exField.displayName}"/></span>
                  <a class="close" href="#" title="Remove field from this tab">×</a>
                </li>
              </g:each>
            </ul>
              </div>
          </div>
        </g:each>
      </g:if>
      <g:else>
        <div class="tab-col span5">
            <div class="tabs-config-box">
          <input type="text" name="tabName" placeholder="enter tab name here" title="Enter a tab name" class="config-tab-name"/>
          <div class="autofill-container">
            <div class="button-group">
              <a href="#" class="button autofill autofillDown">Autofill</a>
              <a href="#" class="button reset">Clear</a>
              <a href="#" class="button delete">Delete</a>
            </div>
          </div>
          <ul class="sortable-fields template">
          </ul>
          </div>
        </div>
      </g:else>
    </div>
  </div>

  <div id="fieldTypes">
    <div class="autofillDropdown">
      <g:each in="${internalFieldTypes}" var="fieldType">
        <li id="internal.${fieldType}" class="${fieldType}">${fieldType.toUpperCase()}</li>
      </g:each>
      <g:each in="${externalFields.keySet()}" var="fieldType">
        <li id="${fieldType}" class="${fieldType}">${fieldType.toUpperCase()}</li>
      </g:each>
    </div>
  </div>

</div>

<g:javascript>
  var closeModal = function(modalId) {
    $(modalId).modal("hide");
  };
</g:javascript>
<div id="error-namesave-modal" class="modal hide" style="width:400px;">
  <div class="modal-header"><h4>Save Error</h4></div>
  <div class="modal-body">
    <p>There was a problem saving the configuration. Please make sure a name has been entered for each tab and try again.</p>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="closeModal('div#error-namesave-modal');">Close</button>
  </div>
</div>

<div id="deletetab-modal" class="modal hide" style="width: 300px;">
  <div class="modal-header">
    <a href="#" class="close">×</a>
    <h3>Delete Tab?</h3>
  </div>
  <div class="modal-body">
    <p>Are you sure you want to delele this tab?</p>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="deleteTab();">Yes</button>
    <button class="btn" onclick="closeModal('div#deletetab-modal');">No</button>
  </div>
</div>

</body>
</html>

