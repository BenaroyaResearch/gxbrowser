<html>
<head>
  <meta name="layout" content="sampleSetMain"/>
  <title>Sample Set Annotation Tool: Configure Annotations</title>
  <style type="text/css">
    span.name-edit:hover {
      background-color: #c8d7db;
    }
  </style>
     <g:javascript src="jquery.tablesorter.min.js"/>
     <g:javascript src="jquery.tablesorter.pager.js"/>

     %{--<g:javascript>--}%

     %{--$(document).ready(function() {--}%
      %{--$("table#id").tablesorter({--}%
        %{--sortList: [[0,0]],--}%
        %{--textExtraction: textExtractor, widthFixed: true--}%
      %{--}).tablesorterPager({container: $("#pager"), size: 15});--}%
      %{--});--}%

    %{--</g:javascript>--}%
</head>


<body>
<div class="container">
    <div class="page-header">
    <h2>Configure Annotations</h2>
  	</div>
  <g:each in="${annotations.keySet()}" var="collection">
  <h4>${collection}</h4>
  	<div class="content">
      <g:link controller="sampleSet" action="show" id="${params.id}" class="btn info">Return to SampleSet Annotation Tool</g:link>
      <g:link controller="geneBrowser" action="show" id="${params.id}" class="btn primary">View in Gene Expression Browser</g:link>
	</div>
    <table id="id" class="zebra-striped pretty-table annotation-table">
    <thead>
      <tr>
        <th style="width:10px;"></th>
        <th style="text-align:left;">Name</th>
        <th style="width:300px;">Data Type</th>
        <th style="width:120px;">Overlay?</th>
        %{--<th style="width:120px;">SSAT?</th>--}%
        %{--<g:if test="${collection == 'Spreadsheet'}">--}%
        %{--<th style="width:150px;">Tab  (<g:link controller="sampleSetTabConfigurator" action="create" id="${params.id}" target="_blank">edit...</g:link>)</th>--}%
        %{--</g:if>--}%
        %{--<th style="width:20px;">#</th>--}%
      </tr>
    </thead>
    <tbody>
      <g:each in="${annotations.get(collection)}" var="annot">
      <tr name="${annot.key}.order">
        <td class="handle" style="background-color:#3E81B2;" title="Click and drag to re-order"></td>
        <td style="text-align:left;"><span contenteditable="true" class="name-edit" name="${annot.key}.displayName">${annot.displayName}</span></td>
        <td>
          <div class="button-group" name="${annot.key}.datatype">
            <span class="button ${annot.datatype == 'string' ? 'active' : ''}" name="string">Categorical</span>
            <span class="button ${annot.datatype == 'number' ? 'active' : ''}" name="number">Numerical</span>
            <span class="button ${annot.datatype == 'timepoint' ? 'active' : ''}" name="timepoint">Time Point</span>
            <span class="button ${annot.datatype == 'image' ? 'active' : ''}" name="image">Image</span>
          </div>
        </td>
        <td>
          <div class="button-group" name="${annot.key}.overlay_visible">
            <span class="button ${annot.overlay_visible == 'show' ? 'active' : ''}" name="show">Show</span>
            <span class="button ${annot.overlay_visible == 'hide' ? 'active' : ''}" name="hide">Hide</span>
          </div>
        </td>
        %{--<td>--}%
          %{--<div class="button-group" name="${annot.key}.ssat_visible">--}%
            %{--<span class="button ${annot.ssat_visible == 'show' ? 'active' : ''}" name="show">Show</span>--}%
            %{--<span class="button ${annot.ssat_visible == 'hide' ? 'active' : ''}" name="hide">Hide</span>--}%
          %{--</div>--}%
        %{--</td>--}%
        %{--<g:if test="${collection == 'Spreadsheet'}">--}%
        %{--<td><g:select name="${annot.key}.tab" from="${tabs}" noSelection="['null':'None']" value="${varToTab[annot.key]}" class="medium" onchange="updateTab(this);"/></td>--}%
        %{--</g:if>--}%
        %{--<td><span name="order">${annot.order}</span></td>--}%
      </tr>
      </g:each>
    </tbody>

       <tfoot>
        <tr>
          <td colspan="7">
            <div id="pager" class="pager">
              <span class="pagination">
                <ul>
                  <li class="prev"><a href="#">&laquo; Previous</a></li>
                  <li class="next"><a href="#">Next &raquo;</a></li>
                </ul>
              </span>
              <div class="samplesets-pages">
                  <span class="view-xy">You're viewing overlay <strong><span class="startItem"></span> - <span class="endItem"></span></strong> of <strong></strong>.</span>




              </div>
            </div>
          </td>
        </tr>
      </tfoot>




  </table>
  </g:each>
</div>
<g:javascript>
  var datasetId = ${params.id};

  var updateTab = function(elt) {
    var key = $(elt).attr("name");
    var tab = $(elt).val();
    if (tab === "null") {
      var args = { id:datasetId, annotation:key };
      $.post(getBase()+"/annotation/removeSetting", args);
    } else {
      var args = { id:datasetId, annotation:key, value:tab };
      $.post(getBase()+"/annotation/updateSetting", args);
    }
  };

  $(document).ready(function() {

    $(".button").click(function() {
      var elt = $(this);
      elt.siblings().each(function() {
        $(this).removeClass("active");
      });
      elt.addClass("active");

      // now update the database
      var setting = elt.parent();
      var annotation = setting.attr("name");
      var settingValue = elt.attr("name");
      var args = { id:datasetId, annotation:annotation, value:settingValue };
      $.post(getBase()+"/annotation/updateSetting", args);
    });

    var oldName = null;
    var updateDisplayName = function(elt) {
      var text = $.trim($(elt).text());
      var key = $(elt).attr("name");
      if (oldName !== text) {
        var args = { id:datasetId, annotation:key, value:text };
        $.post(getBase()+"/annotation/updateSetting", args);
        oldName = null;
      }
    };

    $(".name-edit").bind({
      focus: function() {
        oldName = $.trim($(this).text());
      },
      blur: function() {
        updateDisplayName(this);
      },
      keypress: function(e) {
        if (e.keyCode === 13) {
          $(this).trigger("blur");
          e.stopPropagation();
          e.preventDefault();
        } else if (e.keyCode === 27) {
          $(this).html(oldName);
          $(this).trigger("blur");
        }
      }
    });

    $("table tbody").sortable({
      handle:"td.handle",
      containment:"parent",
      update: function(event, ui) {
        var annotation = ui.item.attr("name");
        var order = ui.item.index();
        var args = { id:datasetId, annotation:annotation, order:order };
        $.post(getBase()+"/annotation/updateOrder", args);
      }
    });

  });
</g:javascript>

</body>
</html>