<html>
<head>
  <title>Notes Browser</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <g:javascript src="jquery.tablesorter.min.js"/>
  <g:javascript src="jquery.tablesorter.pager.js"/>
</head>

<body>
<div class="container-fluid">
  <div class="sidebar">
    <div id="search-notes-panel" class="well">
      <div class="filterPanel" id="dateFilterPanel">
        <h4>Filter By Date</h4>
        <g:form name="notes-filter-form" action="list">
          <g:hiddenField name="filter" value="true"/>
          <div class="fpFieldset">
            Between:
            <div class="input-append">
              <input type="text" name="createdOn" class="datepicker" style="width:130px;" onchange="javascript:filterNotes();" value="${selectedFilters?.createdOn?.get(0)}"/>
              <label class="add-on">
                <span class="ui-icon-date" onclick="javascript:showDatepicker(this);"></span>
              </label>
            </div><br/><br/>
            and:
            <div class="input-append">
              <input type="text" name="createdOn" class="datepicker" style="width:130px;" onchange="javascript:filterNotes();" value="${selectedFilters?.createdOn?.get(1)}"/>
              <label class="add-on">
                <span class="ui-icon-date" onclick="javascript:showDatepicker(this);"></span>
              </label>
            </div>
          </div>
        </div>
        <div class="filterPanel" id="tagFilterPanel">
          <h4>Filter By Tag</h4>
          <div class="fpFieldset">
            <g:each in="${tags}" var="tag">
              <div><input type="checkbox" name="tags" value="${tag}" onclick="javascript:filterNotes();" ${selectedFilters?.tags?.contains(tag) ? 'checked="checked"' : ''}/> ${tag}</div>
            </g:each>
          </div>
        </div>
        <div class="filterPanel" id="userFilterPanel">
          <h4>Filter By User</h4>
          <div class="fpFieldset">
            <g:each in="${users}" var="user">
              <div><input type="checkbox" name="user" value="${user}" onclick="javascript:filterNotes();" ${selectedFilters?.user?.contains(user) ? 'checked="checked"' : ''}/> ${user}</div>
            </g:each>
          </div>
        </div>
      </g:form>
    </div>
  </div>
  <div class="content">
    <table id="table-notes" class="zebra-striped">
      <thead><tr>
        <th style="min-width:80px;">Date</th><th>Note</th><th>Tags</th><th>Private</th><th>User</th><th style="width:20px;"></th>
      </tr></thead>
      <tbody>
      <g:each in="${notes}" var="note">
      	<g:if test="${currentUser.username == note.user || currentUser?.authorities?.any { it.authority == 'ROLE_ADMIN' }}">
        <tr id="${note._id}">
          <td><g:formatDate date="${note.createdOn}" format="yyyy-MM-dd"/></td>
          <td>${note.note}</td>
          <td>${note.tags?.join(", ")}</td>
          <td>${note.privacy}</td>
          <td>${note.user}</td>
          <td><span class="ui-icon-delete" title="Delete this note" onclick="javascript:deleteNote(this);">&nbsp;</span></td>
        </tr>
      </g:if>
      </g:each>
      </tbody>
      <tfoot>
        <tr>
          <td colspan="5">
          <div id="pager" class="pager">
            <span class="pagination">
              <ul>
                <li class="prev"><a href="#">&laquo; Previous</a></li>
                <li class="next"><a href="#">Next &raquo;</a></li>
              </ul>
            </span>
            <div class="samplesets-pages">
              <span class="view-xy">You're viewing notes <strong><span class="startItem">x</span> - <span class="endItem">y</span></strong> of <strong> <span class="totalRows">${notes.size()}</span></strong>.</span>
              <span class="view-amount">View</span>
              <select class="pagesize small">
                <option value="5">5</option>
                <option value="10">10</option>
                <option selected="selected" value="20">20</option>
                <option value="50">50</option>
              </select>
              notes per page
            </div>
          </div>
          </td>
        </tr>
      </tfoot>
    </table>
  </div>
</div>
<g:javascript>
  var showDatepicker = function(elt) {
    var textField = $(elt).closest(".input-append").find("input:text");
    if (!textField.is(":disabled"))
    {
      textField.datepicker("show");
      textField.datepicker("widget").position({
        my: "left top",
        at: "left bottom",
        of: textField
      });
    }
  };

  var filterNotes = function() {
    $("form#notes-filter-form").submit();
  };

  var deleteNote = function(elt) {
    var noteId = $(elt).closest("tr").attr("id");
    $.post(getBase()+"/notes/deleteNote", { noteId:noteId }, function(msg) {
      if (!msg) {
        var tr = $(elt).closest("tr");
        var index = tr.index();
        tr.remove();
        var table = $("table#table-notes");
        var c = table.get(0).config;
        c.rowsCopy.splice(index,1);
        c.totalRows = c.rowsCopy.length;
        c.totalPages = Math.ceil(c.totalRows / config.size);
        var first = c.page < c.totalPages-1;
        $(".next").trigger("click");
        if (first) {
          $(".prev").trigger("click");
        }
        $(".totalRows").html(c.totalRows);
      }
    });
  };

  $(document).ready(function() {
    $("table#table-notes").tablesorter({ sortList: [[0,0]] })
      .tablesorterPager({ container:$("#pager"), size:20 });
    $(".datepicker").datepicker({
      changeMonth: true,
      changeYear: true
    }).focus(function() {
      $(this).datepicker("widget").position({
        my: "left top",
        at: "left bottom",
        of: this
      });
    });
  });
</g:javascript>
</body>
</html>