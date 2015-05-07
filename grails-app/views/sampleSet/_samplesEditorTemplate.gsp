<%@ page import="java.text.SimpleDateFormat" %>

<g:javascript>
  $(document).ready(function() {

    $("table#samples-table").tablesorter();

    $("table#samples-table tr td").click(function() {
      $(this).find("div.sample-content").focus();
    });

    $(".sample-content").live("blur", function() {
      var newText = $.trim($(this).text());
      var id = this.id.split("::");
      var oldText = localStorage.getItem(id);
      $(this).text(newText);
      if (oldText != newText)
      {
        $.post(getBase()+"/sampleSet/updateSample/"+id[0], { sampleSetId: ${sampleSetId}, field: id[1], text: newText });
      }
    });

    $(".sample-content").live("focus", function() {
      var oldText = $.trim($(this).text());
      var id = this.id.split("::");
      localStorage.setItem(id, oldText);
    });

    $(".sample-content").bind("keyup", function(e) {
      var code = (e.keyCode ? e.keyCode : e.which);
      if (code === 27)
      {
        var id = this.id.split("::");
        var oldText = localStorage.getItem(id);
        $(this).html(oldText).trigger("blur");
      }
    });

    $(".dateField").datepicker({
      changeMonth: true,
      changeYear: true,
      showOn: "button",
      buttonImage: "../../images/icons/calendar_view_day.png",
      buttonImageOnly: true,
      showButtonPanel: true
    }).change(function() {
      var newDate = $(this).val();
      var id = $(this).attr("id").split("::");
      $(this).parent().find(".sample-content").html(newDate);
      $.post(getBase()+"/sampleSet/updateSample/"+id[0], { sampleSetId: ${sampleSetId}, field: id[1], date: newDate });
    });
  });
</g:javascript>
<h4>Samples Viewer / Editor <a href="${createLink(action:'exportSpreadsheet',id:sampleSetId)}" id="download_link" class="icon_download" title="Download Spreadsheet"></a></h4>
<p>All fields are editable except the "Sample ID" column. To edit a cell, click within the cell. To edit a "date" cell, click on the calendar icon. To cancel an edit, press the ESC key.</p>
<g:if test="${headers.isEmpty()}">
  <p>No available data. Please <a href="#" id="uploadSpreadsheetLink">upload</a> a spreadsheet.</p>
</g:if>
<div class="scrollable-container no-wrap-content" style="max-height: 600px">
<table class="zebra-striped pretty-table" id="samples-table">
  <thead>
  <tr>
    <th>Sample ID</th>
    <g:each in="${headers}" var="header">
      <th>${headers.get(header.key).displayName.encodeAsHumanize()}</th>
    </g:each>
  </tr>
  </thead>
  <tbody>
    <g:if test="${samples}">
      <g:each in="${samples}" var="sample">
        <tr>
          <td>${sample.sampleId}</td>
          <g:each in="${headers}" var="header">
            <g:if test="${headers.get(header.key).type == 'date'}">
              <td style="text-align: right;"><span class="sample-content">${StringUtil.toDate(sample.get(header.key))}</span> <input id="${sample.id}::${header.key}" class="dateField" style="display:none;" ${editable ? "" : "disabled='true'"}/></td>
            </g:if>
            <g:else>
              <td style="text-align: center;"><div class="sample-content" id="${sample.id}::${header.key}" contenteditable="${editable}">${sample.get(header.key)?:"&nbsp;"}</div></td>
            </g:else>
          </g:each>
        </tr>
      </g:each>
    </g:if>
    <g:else>
      <tr><td>No samples found in this sample set.</td></tr>
    </g:else>
  </tbody>
</table>
</div>
