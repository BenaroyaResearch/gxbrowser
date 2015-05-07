<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>
  <title>My Collections</title>
  <style type="text/css">
  .scrollable-container {
    border: 1px solid #999999;
  }
  .scrollable-container li {
    list-style-type: none;
    border-bottom: 1px solid #CCCCCC;
    padding: 10px;
  }
  .scrollable-container li:last-child {
    border-bottom: none;
  }
  .scrollable-container li span.count {
    font-style: italic;
  }
  .scrollable-container li:hover {
    background-color: #f3f3f3;
  }
  .scrollable-container li.active {
    color: #FFFFFF;
    background-color: #666666;
  }
  .ui-icon-delete {
    cursor: pointer;
  }
  </style>
</head>

<body>

<div class="sampleset-container">
  <div class="page-header">
    <h2>My Collections</h2>
  </div>

  <div class="container-fluid">

    <div class="sidebar">
      <g:if test="${collections}">
      <div class="scrollable-container">
        <g:each in="${collections?.keySet()}" var="name">
          <li id="${name}">${name} (<span class="count">${collections.get(name).size()}</span>)</li>
        </g:each>
      </div>
      </g:if>
      <g:else>
        No collections were found.
      </g:else>
    </div>

    <div class="content">
      <g:if test="${collections}">
      <table id="collection-sets" class="zebra-striped">
        <thead>
        <tr><th style="width:15px;"></th><th>Name</th><th>Platform</th><th>Species</th><th>Disease</th>
        </thead>
        <tbody></tbody>
        <tfoot>
        <tr><td colspan="5">
          <div id="pager" class="pager">
            <span class="pagination">
              <ul>
                <li class="prev"><a href="#">&laquo; Previous</a></li>
                <li class="next"><a href="#">Next &raquo;</a></li>
              </ul>
            </span>
            <div style="padding-top: 5px;">
              <span style="padding-left: 10px;">
                View
              </span>
              <select class="pagesize small">
                <option selected="selected" value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
              </select>
              sample sets per page
            </div>
            <div class="currentPage" style="display:none;"></div>
          </div></td></tr>
        </tfoot>
      </table>
      </g:if>
    </div>

  </div>

</div>

<g:javascript src="jquery.tablesorter.min.js"/>
<g:javascript src="jquery.tablesorter.pager.js"/>
<g:javascript>
  var textExtractor = function(node) {
    var childNode = node.childNodes[0];
    if (childNode)
    {
      var nodeName = childNode.nodeName.toLowerCase();
      if (nodeName === "span")
      {
        return childNode.title;
      }
      else if (nodeName === "a")
      {
        return childNode.innerHTML;
      }
    }
    return node.textContent;
  };
  var platformIcon = function(platform) {
    if (platform)
    {
      var p = platform.split(" ");
      if (p[0] === "Affymetrix")
      {
        return "<span title='"+platform+"' class='ui-icon-affymetrix'></span>";
      }
      else if (p[0] === "Illumina")
      {
        return "<span title='"+platform+"' class='ui-icon-illumina'></span>";
      }
      else
      {
        return "<span title='"+platform+"'>"+p[0]+"</span>";
      }
    }
  };
  var updateSampleSetTable = function(name) {
    var pagesize = parseInt($("select.pagesize").val());
    $.getJSON(getBase()+"/datasetCollection/collectionSampleSets", { name: name }, function(json) {
      var html = "";
      $.each(json, function(ssId,ss) {
        html += "<tr id='"+ssId+"'>";
        html += "<td><span class='ui-icon-delete'></span></td>";
        html += "<td><a href='"+getBase()+"/sampleSet/show/"+ssId+"' target='_blank'>"+ss.name+"</a></td><td>"+platformIcon(ss.platform)+"</td>";
        if (ss.species !== null) {
          html += "<td>"+ss.species+"</td>";
        } else {
          html += "<td></td>";
        }
        if (ss.disease !== null) {
          html += "<td>"+ss.disease+"</td>";
        } else {
          html += "<td></td>";
        }
        html += "</tr>";
      });
      var tableHtml = $("table#collection-sets>tbody").html(html).closest("div.content").html();
      $("div.content").html(tableHtml);
      $("table#collection-sets").tablesorter({
        headers: { 0: { sorter: false} },
        sortList: [[1,0]],
        textExtraction: textExtractor }).tablesorterPager({container: $("#pager"), size: pagesize });
    });
  };
  $(document).ready(function() {
    var firstCollection = $("div.scrollable-container>li:first-child").attr("id");
    if (firstCollection !== "undefined")
    {
      $("div.scrollable-container>li:first-child").addClass("active");
      updateSampleSetTable(firstCollection);
    }
    $(".scrollable-container li").click(function() {
      var cName = this.id;
      $(this).parent().find("li.active").removeClass("active");
      $(this).addClass("active");
      updateSampleSetTable(cName);
    });
    $("span.ui-icon-delete").live("click", function() {
      var row = $(this).closest("tr");
      var li = $(".scrollable-container li.active");
      var name = li.attr("id");
      var args = { id: row.attr("id"), name: name };
      $.post(getBase()+"/datasetCollection/removeFromCollection", args, function(reply) {
        var newCount = parseInt(li.find("span.count").html()) - 1;
        li.find("span.count").html(newCount);
        updateSampleSetTable(name);
      });
    });
  });
</g:javascript>

</body>
</html>
