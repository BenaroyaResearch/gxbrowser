<%@ page import="grails.converters.JSON" %><g:javascript>
  var hasSavedFilters = ${!savedFilters?.isEmpty()};
  var savedFilters = ${savedFilters ? savedFilters as JSON : []};
  var saveFilter = function() {
    var name = $("div#savefilter-modal").find("input#filterName").val();
    var args = $("form#filter-samplesets-form").serializeArray();
    args.push({ name: "filterName", value: name });
    $.post(getBase()+"/sampleSet/saveFilter", args);
    $("div#savefilter-modal").find("input#filterName").val("");
    if (hasSavedFilters == true)
    {
      $("form#load-filter").find("select").append("<option value='"+name+"'>"+name+"</option>");
    }
    else
    {
      $("form#load-filter>p").html("Select a filter to load:<br/><select id='filterName' name='filterName'><option value='"+name+"'>"+name+"</option></select>");
      $("div#loadfilter-modal").find("button.primary").removeAttr("disabled");
      hasSavedFilters = false;
    }
    closeModal("savefilter-modal");
  };
  var checkName = function() {
    var name = $("div#savefilter-modal").find("input#filterName").val();
    var overwrite = $("div#savefilter-modal").find("input#overwrite").is(":checked");
    if (overwrite || $.inArray(name, savedFilters) === -1)
    {
      $("button#savefilter-button").removeAttr("disabled");
    }
    else
    {
      $("button#savefilter-button").attr("disabled", true);
    }
  };
</g:javascript>
<div id="savefilter-modal" class="modal hide" style="width: 300px;">
  <div class="modal-header">
    <a href="#" class="close">×</a>
    <h3>Save Filter</h3>
  </div>
  <div class="modal-body">
    <p>Enter a name for this filter:<br/>
    <g:textField name="filterName" onkeyup="checkName();"/>
    </p>
    <p>
      <g:checkBox name="overwrite" checked="false" onchange="checkName();"/> Overwrite, if filter already exists
    </p>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="saveFilter();" id="savefilter-button" disabled="true">Save</button>
    <button class="btn" onclick="closeModal('savefilter-modal');">Cancel</button>
  </div>
</div>