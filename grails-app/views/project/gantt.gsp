<%@ page import="org.sagres.project.Project" %>
<!doctype html>
<html lang="en">
<head>
  	<meta charset="utf-8">
  	<meta name="layout" content="projectMain"/>
  		<g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
  <title>Gantt Chart</title>
  	<!--[if lt IE 9]>
	<script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
	<![endif]-->
	<%--  <g:javascript src="date-format.js"/>--%>
  	<g:javascript src="moment.js"/>
  	<g:javascript src="moment-workingDays.js"/>
  	<g:javascript src="modules/raphael.js"/>
  	<g:javascript src="raphael-gantt.js"/>

  	<style type="text/css">
	   #chart{
	    width: 100%;
	    overflow: auto;
	  }

	  #chart svg{
	    border: 2px solid black;
	    display: inline-block;
	  }
	  
	  a#show-filters {
	      display: inline;
	    }
  
  	</style>
  	  <g:javascript>
  	  String.prototype.capitalize = function () {
    		return this.replace(/^./, function (char) {
        		return char.toUpperCase();
    		});
		};
		
	  	var doChart = function() {

	 		$('#chart').html("<p id='loading'>Gantt Chart Loading...</p>");

			var args = {
 				title: $("#chartTypeFilter").val().capitalize() + " Gantt Chart",
  				startDate: moment($("#firstDateFilter").val()).format("YYYY-MM-DD"),
 				endDate:   moment($("#lastDateFilter").val()).format("YYYY-MM-DD"),
 				chartType: $("#chartTypeFilter").val()
 			};
	
			$.getJSON(getBase()+"/project/getGanttData", args, function(json) {
	 			var chart = new GanttChart("chart");
				console.log(json);
				if (json.phases && json.phases.length > 0) {
					chart.loadData([json]);
     				chart.draw();
     				$("#loading").hide();
     			} else {
    			    $("#loading").text("No projects fulfill these criteria...").show();
     			}
			});
		}

		function getFirstDay(d) {
				d = d || new Date(); // If no date supplied, use today
				var qList = [0,3,6,9];
				var q = qList[Math.floor(d.getMonth() / 3)];
				return moment([d.getFullYear(), q, 1]).format('YYYY-MM-DD');
		}

		function getLastDay(d) {
				d = d || new Date(); // If no date supplied, use today
				var qList = [0,3,6,9];
				var q = qList[Math.floor(d.getMonth() / 3)];
				return moment([d.getFullYear(), q + 3, 0]).format('YYYY-MM-DD');
		}
		
      	var filterClear = function() {

      		$("#firstDateFilter").val(getFirstDay());
        	$("#lastDateFilter").val(getLastDay());
        	$("#chartTypeFilter").val("project");
      		doChart();
    	};
  	  
	  	$(document).ready(function() {


	     	$("#firstDateClear").click(function() {
		 		$("#firstDateFilter").val("");
	      		doChart();
		 	});
		 	$("#lastDateClear").click(function() {
		 		$("#lastDateFilter").val("");
	      		doChart();
		 	});
		 	
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
		      	dateFormat: 'yy-mm-dd',
		      	showAnim: 'show',
		      	duration: 'fast',
		     }).focus(function() {
		      	$('.datepicker').datepicker("widget").position({
		       		my: "left top",
	  	      		at: "left bottom",
	  	      		of: this
	  	    	});
	     	});			

			filterClear();
	  	});
	  </g:javascript>
  
</head>
<body>
  <h1>Gantt Chart</h1>
      <div id="search-filter-panel" class="well" style="width: 150px;">
       <div class="filter-panel-header"><span name="allClear" class="button tiny" onclick="filterClear();">Reset</span>
       </div>

	    <g:form name="filter-chipsloaded-form" action="filteredChipsLoaded">
	      <div class="filterPanel" id="dateFilterPanel">
		    <h4>Date Range</h4>
		    <div class="fpFieldset">
			  <label for="firstDateFilter">
                From
              </label>
		      <div class="input-append datepicker-from">
                <input type="text" name="firstDateFilter"
                       class="datepicker small" id="firstDateFilter"
                       placeholder="Date"
                       value="${params?.firstDateFilter}"
                       onchange="doChart();" />
                <label class="add-on">
                  <span class="ui-icon-date"></span>
                </label>
               </div>
              <br/>
              <label class="clearfix">
                To
              </label>
		      <div class="input-append datepicker-to">
			    <input type="text" name="lastDateFilter"
			    	   class="datepicker small" id="lastDateFilter"
			    	   placeholder="Date"
			    	   value="${params?.lastDateFilter}"
			    	   onchange="doChart();" />
                <label class="add-on">
                  <span class="ui-icon-date"></span>
                </label>
              </div>
		    </div>
 	      </div>
	      <div class="filterPanel" id="dataSourceFilterPanel">
		    <h4>Chart Type</h4>
		    <div class="fpFieldset">
		      <select id="chartTypeFilter" name="chartType" onchange="doChart();" value="${params.chartType}" style="width: 125px;">
		      	<option value="project">Project</option>
		      	<sec:ifLoggedIn>
			      	<option value="private">Private</option>
		      	</sec:ifLoggedIn>
		      	<option value="resource">Resource</option>
		      </select>
		    </div>
	      </div>
	    </g:form>
	  </div>
	</div>
  <div class="content-menu withFilter">
  <div id="chart">
  	<p id="loading">Gantt Chart Loading...</p>
  </div>
  <script type="text/javascript">
	//var chart = new GanttChart("chart");

<%--      var payload = [{--%>
<%--        name: "Project Name", startDate: "2012-06-28", endDate: "2012-07-10",--%>
<%--        phases : [--%>
<%--          {name : "Phase 1", startDate: "2012-06-28", endDate: "2012-07-04",--%>
<%--            tasks : [--%>
<%--              {name : "Task 1", startDate: "2012-06-28", endDate: "2012-07-01"},--%>
<%--              {name : "Task 2", startDate: "2012-06-29", endDate: "2012-07-04"}--%>
<%--            ]--%>
<%--          },--%>
<%--          {name : "Phase 2", startDate: "2012-07-05", endDate: "2012-07-10",--%>
<%--            tasks : [--%>
<%--              {name : "Task 3", startDate: "2012-07-05", endDate: "2012-07-08"},--%>
<%--              {name : "Task 4", startDate: "2012-07-09", endDate: "2012-07-10"}--%>
<%--            ]--%>
<%--          }--%>
<%--        ]--%>
<%--      }];--%>
<%--      chart.loadData(payload);--%>
<%--      chart.draw();--%>


  </script>
  </div> <!--end container-->
</body>
</html>
