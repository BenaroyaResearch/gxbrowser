<g:if test="${trialShareProjectInfo != null && trialShareProjectInfo.size() > 0}">
  <h3>ITN Study Association</h3>
  Associate Sampleset with
  <select id="tsproject" name="tsproject" class="medium" onchange="saveSampleSetRole();">
    <option value="-1">-None-</option>
    <g:each in="${trialShareProjectInfo}" var="h">
      <option value="${h.value.id}" <g:if test="${h.value.id == associatedTSRole}">selected</g:if> > ${h.key}</option>
    </g:each>

  </select>
</g:if>

<h3>Reports</h3>
<g:if test="${labkeyReports}">
<table id="groupset-table" class="zebra-striped">
  <thead>
  <tr><th></th><th>Report Name</th><th>Query</th><th>Type</th><th>On</th><th>Sample Set Column</th><th>Report Column</th><th>&nbsp;</th><th>&nbsp;</th></tr>
  </thead>
  <tbody>


  <g:each in="${labkeyReports}" var="r">
    <tr>
      <td><button class="close" data-reportId="${r.id}" onclick="deleteReport(this);">&times;</button></td>
      <td><g:textField name="${r.id}-reportName" value="${r.reportName}" class="medium"/></td>
      <td><g:textField name="${r.id}-query" value="${r.reportURL}"/></td>
      <td><g:select name="${r.id}-category" value="${r.category}"
                    from="${['labkeySubject':'Subject', 'labkeyClinical':'Clinical', 'labkeyLabResults':'Lab Results',
                        'labkeyAncillaryData':'Ancillary Data', 'labkeyFlow':'Flow Data']}"
                    optionKey="key" optionValue="value" class="medium"/></td>
      <td style="text-align:center;"><input type="checkbox" name="${r.id}-enabled" id="${r.id}-enabled"<g:if test="${r.enabled != false}"> checked="checked"</g:if>/> </td>
      <td>
        <select id="${r.id}-sampleSetColumn" name="${r.id}-sampleSetColumn" class="medium">
          <option value="null">-select a column-</option>
          <g:each in="${headers.entrySet()}" var="h">
            <option value="${h.key}"<g:if test="${r.sampleSetColumn == h.key}"> selected</g:if>>${h.value.displayName}</option>
          </g:each>
        </select>
      </td>
      <td><g:select from="${categoryToHeaders.get(r.category)}" name="${r.id}-reportColumn"
                    optionKey="key" optionValue="displayName" value="${r.reportColumn}"
                    noSelection="['null':'-select a column']" class="medium"/></td>
      <td>
        <button class="btn" onclick="reloadReport(this);" data-reportId="${r.id}" data-category="${r.category}">Run</button>
        <button class="btn primary" onclick="save(this);" data-reportId="${r.id}">Save</button>
      </td>
      <td><A HREF="${labkeyHost}/${r.reportURL}">Review Report Data</A> </td>
    </tr>
  </g:each>
  </tbody>
  <tfoot>
    <tr><td colspan="7"><button class="btn primary" onclick="newReport();">Create Report</button></td></tr>
  </tfoot>
</table>
</g:if>
<g:else>
  <p>No reports were found. Please create a report before configuring.</p>
  <p><button class="btn primary" onclick="newReport();">Create Report</button></p>
</g:else>
<h3>Groups/Cohorts</h3>
<div>
  Select a column to match against the ITN data:
  <select id="groups-column" name="groups-column">
    <option value="null">-select a column-</option>        w
    <g:each in="${headers.entrySet()}" var="h">
      <option value="${h.key}" >${h.value.displayName}</option>
    </g:each>
  </select>
  <button class="btn primary" onclick="saveGroupColumn(this);">Save</button>
</div>


<div id="new-report-modal" class="modal hide">
  <div class="modal-header">
    <h4>New Report</h4>
  </div>
  <div class="modal-body">
    <g:form name="create-report-form" controller="sampleSet" action="createReport" id="${sampleSet.id}">
      <fieldset>
        <div class="clearfix">
          <label>Report Name</label>
          <div class="input"><g:textField name="reportName"/></div>
        </div>
        <div class="clearfix">
          <label>Report URL</label>
          <div class="input"><g:textField name="reportURL"/></div>
        </div>
        <div class="clearfix">
          <label>Category</label>
          <div class="input">
          <g:select name="category" optionKey="key" optionValue="value"
                    from="${['labkeySubject':'Subject', 'labkeyClinical':'Clinical', 'labkeyLabResults':'Lab Results',
                        'labkeyAncillaryData':'Ancillary Data', 'labkeyFlow':'Flow Data']}"/>
          </div>
        </div>
      </fieldset>
    </g:form>
  </div>
  <div class="modal-footer">
    <button class="btn primary" id="create-report-btn" onclick="createReport();">Create</button>
    <button class="btn primary" id="cancel-report-btn" onclick="closeNewReport();">Cancel</button>
  </div>
</div>

<g:javascript>
  var reloadReport = function(btn) {
    var id = $(btn).attr("data-reportId");
    var category = $(btn).attr("data-category");
    $.post(getBase()+"/labkeyReport/runReport/"+id, function(json) {
      if (json === "Status Code for request 200") {
        // success! reload the options
        var args = { category:category };
        $.getJSON(getBase()+"/sampleSet/labkeyHeader/${sampleSet.id}", args, function(json) {
          if (json.headers) {
            var option = "<option value='null'>-select a column-</option>";
            $.each(json.headers, function(i,h) {
              option += "<option value='" + h.key + "'>"+h.displayName+"</option>";
            });
            $("select#"+id+"-reportColumn").html(option);
          }
        });
      }
    });
  };

  var save = function(btn) {
    var id = $(btn).attr("data-reportId");
    var sampleSetColumn = $("select#"+id+"-sampleSetColumn").val();
    var reportColumn = $("select#"+id+"-reportColumn").val();

    var name = $("input#"+id+"-reportName").val();
    var queryUrl = $("input#"+id+"-query").val();
    var category = $("select#"+id+"-category").val();
    var enabled = $("input#"+id+"-enabled").is(":checked");

    var args = { sampleSetColumn:sampleSetColumn, reportColumn:reportColumn, name:name,
                 queryUrl:queryUrl, category:category, enabled:enabled };
    $.post(getBase()+"/sampleSet/saveReport/"+id, args, function(json) {
      if (json === "success") {
        alert("The report configuration for " + name + " has been saved successfully.");
      }
    });
  };

  var createReport = function() {
    $("form#create-report-form").submit();
  };

  var newReport = function() {
    $("div#new-report-modal").show();
  };

  var closeNewReport = function() {
    $("div#new-report-modal").hide();
  };

  var deleteReport = function(btn) {
    var reportId = $(btn).attr("data-reportId");
    $.post(getBase()+"/sampleSet/deleteReport", { id:reportId }, function(json) {
      if (json.success) {
        $(btn).closest("tr").detach();    // remove the row from the table
      }
    });
  };

  var saveGroupColumn = function() {
    var groupsColumn = $("select#groups-column").val();
    if (groupsColumn !== 'null') {
      var args = { groupsColumn:groupsColumn };
      $.post(getBase()+"/sampleSet/saveGroupsColumn/${sampleSet.id}", args, function(json) {
        if (json === "success") {
          alert("The groups/cohorts configuration has been saved successfully.");
        }
      });
    }
  };

    var saveSampleSetRole = function() {
    var roleId = $("#tsproject").val();
      var args = { roleId:roleId};
      $.post(getBase()+"/sampleSet/saveSampleSetRole/${sampleSet.id}", args, function(json) {
        if (json === "success") {
          alert("The required Role has been saved successfully.");
        }
      });
  };

</g:javascript>