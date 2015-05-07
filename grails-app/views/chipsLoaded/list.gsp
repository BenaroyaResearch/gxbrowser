<%@ page import="common.chipInfo.ChipsLoaded" %>
<html>
  <head>
	<title>ChipsLoaded List</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="layout" content="chipsLoadedMain"/>
    <g:set var="entityName" value="${message(code: 'chipsLoaded.label', default: 'ChipsLoaded')}" />
    %{--<link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>--}%
  	<g:javascript src="jquery-min.js"/>
  	<g:javascript src="jquery-ui-min.js"/>
   	<g:javascript src="jquery.tablesorter.min.js"/>
  	<g:javascript src="jquery.tablesorter.pager.js"/>
  	<g:javascript>
  	var isIE = $.browser.msie;
  	var execFilter = function() {
      $("#filter-chipsloaded-form").submit();
    };

  	var textExtractor = function(node) {
      var childNode = node.getElementsByTagName("span")[0];
      if (!childNode)
      {
      	childNode = node.getElementsByTagName("a")[0];
      }
      if (childNode)
      {
        var nodeName = childNode.nodeName;
        if (nodeName === "SPAN")
        {
          return childNode.title;
        }
        else if (nodeName === "A")
        {
          return childNode.innerHTML;
        }
      }
      if (isIE) {
        return node.innerText;
      } else {
        return node.textContent;
      }
    };
    $(document).ready(function() {
     $("#firstDateClear").click(function() {
	 	$("#firstDateFilter").val("");
      	execFilter();
	 });
	 $("#lastDateClear").click(function() {
	 	$("#lastDateFilter").val("");
      	execFilter();
	 });

      $("table#chipsloaded").tablesorter({
        sortList: [[5,1]],
        textExtraction: textExtractor
      })
      .tablesorterPager({container: $("#pager"), size: 20});
	  <!-- need to use class input-append to capture the CSS as well -->
      $(".ui-icon-date").click(function() {
        var textField = $(this).closest(".input-append").find("input:text");
        if (!textField.is(":disabled"))
       {
        textField.datepicker("show");
        textField.datepicker("widget").position({
          my: "left top",
          at: "left bottom",
          of: textField
        });
      }
     });
     $('.datepicker').datepicker({
      changeMonth: true,
      changeYear: true,
      dateFormat: 'yy-mm-dd'
     }).focus(function() {
      $('.datepicker').datepicker("widget").position({
        my: "left top",
        at: "left bottom",
        of: this
      });
     });
    });
	</g:javascript>
      <g:javascript>
       var filterClear = function() {
      	$("input.fpCheckBox").removeAttr('checked');
      	$("#firstDateFilter").val("");
        $("#lastDateFilter").val("");
      	$("#chipTypeFilter").val("")
        $("#loadStatusFilter").val("")
        $("#dataSourceFilter").val("")
      	execFilter();
    };
      </g:javascript>
  	<style type="text/css">
    a#show-filters {
      display: inline;
    }
  </style>

  </head>
  <body>
 	<div class="sampleset-container">


      <div id="search-filter-panel" class="well">
       <div class="filter-panel-header"><span name="allClear" class="button tiny" onclick="filterClear();">Clear All Filters</span><span class="button tiny close-filter" onclick="toggleFilterPanel()">x</span>
       </div>

	    <g:form name="filter-chipsloaded-form" action="filteredChipsLoaded">
		  <div class="filterPanel" id="platformFilterPanel">
		    <h4>Chip type</h4>
			<div class="fpFieldset">
		      <g:select name="chipTypeFilter"
				        from="${common.chipInfo.ChipType.list()}"
				        optionKey="id"
				        optionValue="name"
				        noSelection="[ '': '--Chip Type--' ]"
                        value="${filter?.chipType?.id}"
                        onchange="execFilter();" />
			</div>
 		  </div>
	      <div class="filterPanel" id="loadStatusFilterPanel">
		    <h4>Load Status</h4>
		    <div class="fpFieldset">
		      <g:select name="loadStatusFilter"
				        from="${org.sagres.FileLoadStatus.list()}"
				        optionKey="id"
				        optionValue="description"
				        noSelection="[ '': '--Load Status--' ]"
                        value="${filter?.loadStatus?.id}"
                        onchange="execFilter();" />
		    </div>
 	      </div>
	      <div class="filterPanel" id="dateFilterPanel">
		    <h4>Load Date</h4>
		    <div class="fpFieldset">
			  <label for="firstDateFilter">
                From
              </label>
		      <div class="input-append datepicker-from">
                <input type="text" name="firstDateFilter"
                       class="datepicker small" id="firstDateFilter"
                       placeholder="Date"
                       value="${params?.firstDateFilter}"
                       onchange="execFilter();" />
                <label class="add-on">
                  <span class="ui-icon-date"></span>
                </label>
                <g:if test="${filter?.firstDate}">
        	 	<div>
        			<!--  <button style="margin: 2px 5px;" name="dateClear" class="button medium" id="dateClear" value="clear" onclick="execFilter();">Clear</button> -->
       			    <button style="margin: 2px 5px;" class="button medium" name="firstDateClear" id="firstDateClear" value="clear">Clear</button>
        	 	</div>
        	 	</g:if>
              </div>
              <label>

                To
              </label>
		      <div class="input-append datepicker-to">
			    <input type="text" name="lastDateFilter"
			    	   class="datepicker small" id="lastDateFilter"
			    	   placeholder="Date"
			    	   value="${params.lastDateFilter}"
			    	   onchange="execFilter();" />
                <label class="add-on">
                  <span class="ui-icon-date"></span>
                </label>
                <g:if test="${filter?.lastDate}">
        	 	<div>
        			<!--  <button style="margin: 2px 5px;" name="dateClear" class="button medium" id="dateClear" value="clear" onclick="execFilter();">Clear</button> -->
       			    <button style="margin: 2px 5px;" class="button medium" name="lastDateClear" id="lastDateClear" value="clear">Clear</button>
        	 	</div>
        	 </g:if>

              </div>
		    </div>
 	      </div>
	      <div class="filterPanel" id="dataSourceFilterPanel">
		    <h4>Data Source</h4>
		    <div class="fpFieldset">
		      <g:select name="dataSourceFilter"
				        from="${common.chipInfo.GenomicDataSource.list()}"
				        optionKey="id"
				        optionValue="name"
				        noSelection="[ '': '--Data Source--' ]"
                        value="${filter?.dataSource?.id}"
                        onchange="execFilter();" />
		    </div>
	      </div>
	    </g:form>
	  </div>

	  <div class="content-menu withFilter">
           <sec:ifLoggedIn>
      <div class="load_nav">
		%{--<ul class="pills chips-pills">--}%
		   %{--<li class="active"><a href="#">ChipsLoaded List</a></li>--}%
		   %{--<li> <g:link action="showUploadExpressionDataForm">--}%
		      %{--Upload Expression Data--}%
	        %{--</g:link>--}%
          %{--</li>--}%
		  %{--<li>--}%
	        %{--<g:link action="showUploadBriRnaSeqFilesForm">--}%
		      %{--Load BRI RNA-seq Files--}%
	        %{--</g:link>--}%
          %{--</li>--}%
		  %{--<li>--}%
	        %{--<g:link action="showUploadFocusedArrayDataForm">--}%
		      %{--Load Focused-Array Data--}%
	        %{--</g:link>--}%
          %{--</li>--}%

		%{--</ul><!--end pill ul-->--}%
      </div><!--end nav-->
	 </sec:ifLoggedIn>
        <div class="page-header">
            <h2>ChipsLoaded List</h2>
        </div>



        <!-- !!!
             <div class="body">
               <h1><g:message code="default.list.label" args="[entityName]" /></h1>

               <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
               </g:if>
               -->
        <g:if test="${!chipsLoadedList.isEmpty()}">
        <div class="list">
          <table id="chipsloaded" class="zebra-striped pretty-table chipsloaded-list">
            <thead>
              <tr>
                <th class="sortable header">Filename</th>
                <th class="sortable header" style="min-width:100px;">Chip Type</th>
                <th class="sortable header" style="min-width:100px;">Samples</th>
                <th class="sortable header" style="min-width:100px;">Probes</th>
                <th class="sortable header" style="white-space: nowrap;">Load Status</th>
                <th class="sortable header" style="white-space: nowrap;">Load Start</th>
                <th class="sortable header" style="white-space: nowrap;">Load End</th>
                <th class="sortable header" style="min-width:120px;">Source</th>
                <th class="sortable header" style="min-width:120px;">SampleSet</th>
                <th class="sortable header chipnotes" style="width:300px; max-width:300px;">Notes</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${chipsLoadedList}"
                      status="i" var="chipsLoadedInstance">
                <tr>
                  <td><g:link action="show" id="${chipsLoadedInstance.id}">${fieldValue(bean: chipsLoadedInstance, field: "filename")}</g:link></td>
                  <td>${fieldValue(bean: chipsLoadedInstance, field: "chipType")}</td>
                  <td>${fieldValue(bean: chipsLoadedInstance, field: "noSamples")}</td>
                  <td>${fieldValue(bean: chipsLoadedInstance, field: "noProbes")}</td>
                  <td>
                      %{--<g:if test="${fieldValue(bean: chipsLoadedInstance, field: 'loadStatus')}=='Complete'">--}%
                       %{--${fieldValue(bean: chipsLoadedInstance, field: "loadStatus")}--}%
                          %{--woo--}%
                  %{--</g:if>--}%

                        %{--<g:else>--}%

                      ${fieldValue(bean: chipsLoadedInstance, field: "loadStatus")}
                  %{--</g:else>--}%

                  </td>
                  <td style="font-size: 11px;"><g:formatDate date="${chipsLoadedInstance.dateStarted}" format="yyyy-MM-dd"/></td>
                  <td style="font-size:11px;"><g:formatDate date="${chipsLoadedInstance.dateEnded}" format="yyyy-MM-dd"/></td>
                  <td style="min-width:120px;">${chipsLoadedInstance.genomicDataSource?.name}</td>
                  <td><g:link controller="sampleSet" action="show" id="${chipsLoadedInstance.sampleSetId}">${fieldValue(bean: chipsLoadedInstance, field: "sampleSetId")}</g:link></td>
                  <td style="text-align: left; width:300px; max-width:300px; overflow: hidden;">${chipsLoadedInstance.notes?.size()>120 ? (chipsLoadedInstance.notes?.substring(0,120) + "...") : chipsLoadedInstance.notes}</td>
                  <!-- <td>${fieldValue(bean: chipsLoadedInstance, field: "notes")}</td> -->
                </tr>
              </g:each>
            </tbody>
            <tfoot>
              <tr>
                <td colspan="9">
	              <div id="pager" class="pager">
	                <span class="pagination">
		              <ul>
		                <li class="prev"><a href="#">&laquo; Previous</a></li>
          				<li class="next"><a href="#">Next &raquo;</a></li>
		              </ul>
	                </span>
      				<div class="samplesets-pages">
           				<span class="view-xy">You're viewing loaded chips <strong><span class="startItem"></span> - <span class="endItem"></span></strong> of <strong> ${chipsLoadedCount}</strong>.</span>
           				<span class="view-amount">View</span>
      					<select class="pagesize small">
        					<option value="5">5</option>
        					<option value="10">10</option>
        					<option selected="selected" value="20">20</option>
        					<option value="50">50</option>
      					</select>
         				loaded chips per page
      				</div>
	              </div>
                </td>
              </tr>
            </tfoot>
          </table> <!--end chipsloaded table-->
          
        </div>  <!--end list-->
          </g:if>
          <g:else>
      		<div class="none-found">No chip sets found.</div>
    	</g:else>

      </div><!--end content-menu-->

    </div> <!--end container-->

  </body>
</html>
