<%@ page import="common.GeneInfo" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<g:set var="entityName" value="${message(code: 'geneInfo.label', default: 'GXB: Gene Info')}"/>
	<meta name="layout" content="main"/>
	<title><g:message code="default.list.label" args="[entityName]"/></title>


	<style>
		.geneBox {
			border-bottom:  solid 1px #eeeeee;
			border-right:  solid 1px #eeeeee;
			margin-bottom: 2px;
			padding: 7px;
			background-color: white;
		}

		.geneSymbol {
			/*font-family: 'MolengoRegular', Arial, Sans-serif;*/
			font-weight: bold;
			font-size: 110%;
			/*font-size: 125%;*/
			/*text-shadow: 1px 1px 1px #000000;*/
		}

		.geneDetail {
			/*font-family: 'LuxiSansRegular', Arial, sans-serif;*/
			/*font-family: 'ArchitectsDaughterRegular', Arial, Sans-serif;*/
			white-space: nowrap;
			overflow: hidden;
			text-overflow: ellipsis;
			font-size: 100%;
		}

		.resultList
		{
			overflow-y: auto;
		}

		.geneList {
			/*width: 300px;*/
			min-height: 250px;
			max-height: 700px;
			overflow-y: auto;

			border:  solid 1px black;
			box-shadow: 2px 2px 2px darkgray;
			border-radius: 4px;
			margin-top: 10px;
			color: black;


			/*padding:  7px;*/
		}

		.geneTitle {
			font-size: 140%;
		}

		.ui-autocomplete-loading { background: white url('../images/ui-anim_basic_16x16.gif') right center no-repeat; }
		#city { width: 25em; }

		/* play with the topbar popover menu ... to be moved to common */
		div.popover-well {
		  min-height: 160px;
		}

		div.popover-well div.popover {
		  display: block;
		}

		div.popover-well div.popover-wrapper {
		  width: 50%;
		  height: 160px;
		  float: left;
		  margin-left: 55px;
		  position: relative;
		}

		div.popover-well div.popover-menu-wrapper {
		  height: 80px;
		}


	</style>
</head>

<body>

<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<div class="container-fluid">
		<div class="sidebar">


	<div id="advancedQueryPanel">
		<form  action="" onsubmit="submitQuery('panel'); return false;">
		<input type="text" placeholder="enter query terms here" id="queryEntry" />
		<input type="button" class="btn primary" id="queryButton" value="Query" />
		</form>
	</div>
			<div id="alertMsg" class="alert-message info"><strong>updating ...</strong></div>
			<div id="errorNotFound" class="alert-message error"><a class="close" href="#">&times;</a><strong><span id="notFoundMsg">No Records Found</span></strong></div>
	<div id="geneList" class="geneList">
		<div id="resultList">

%{--
		<g:each in="${geneInfoInstanceList}" status="i" var="geneInfoInstance">
			<div id="geneBox_${geneInfoInstance.geneID}" onclick="selectGene('#geneBox_${geneInfoInstance.geneID}', '${geneInfoInstance.symbol}'); return false;" class="geneBox">
				<span class="geneSymbol">${geneInfoInstance.symbol}</span>
				<br/>
				<div class="geneDetail">${geneInfoInstance.name}</div>
			</div>
		</g:each>
--}%
		</div>
	</div>
		</div>
		<div class="content" >

			<strong><span id="gene_symbol" class="geneTitle"></span> <span id="gene_description" class="geneTitle"></span></strong>
			<br/>


		</div>
	</div>

</div>
<script>
	var currentGeneSymbol = "";
	var currentGeneDiv = "";
	var currentQuery = "";

	var selectGene = function(geneID, symbol)
	{
//			alert('select: ' + divID);
		var divID = "#geneBox_" + geneID;

		$(currentGeneDiv).css('background-color', 'white');
		$(currentGeneDiv).css('color', 'black');
		$(divID).css('background-color', '#007AFC');
		$(divID).css('color', 'white');
		currentGeneDiv = divID;
		currentGeneSymbol = symbol;

		var args =
		{
			geneID: geneID,
			symbol: currentGeneSymbol
		};

		$.getJSON("queryGeneInfo", args, function(json)
		{
			console.log("back");
			$('#gene_symbol').html(json.entrez.Name);
			$('#gene_description').html(json.entrez.Description);
		});
	};

	var runQuery = function(queryEntry)
	{
		$("#resultList").hide();
		$("#errorNotFound").hide();
		$("#alertMsg").show();
		var args =
		{
			queryString: queryEntry,
			rankList: -1,
			rt: "span"  // return type = span
		};

		$.getJSON("queryGeneList", args, function(json)
		{
			$("#alertMsg").hide();
			if (json.gl != "")
			{
				$('#resultList').show();
				$("#resultList").replaceWith(json.gl);
				currentQuery = queryEntry;
				// make sure both text boxes are in sync
				$('#queryEntry').val(currentQuery);
				$('#topSearch').val(currentQuery);
			}
			else
			{
				$('#notFoundMsg').html("<span id='notFoundMsg'>No records found for: " + queryEntry);
				$('#errorNotFound').show();
			}

		});

	};

	var submitQuery = function(qt)
	{
		var queryEntry = "";
		if (qt == "top")
			queryEntry = $("#topSearch").val()
		else
			queryEntry = $('#queryEntry').val();
		runQuery(queryEntry);
		return false;
	};

	var toggleAdvancedQueryPanel = function()
	{
		$('#advancedQueryPanel').show();
	};

	$(document).ready(function() {
		$('#queryButton').click(submitQuery);
		$('#errorNotFound').hide();
		$('#advancedQueryPanel').hide();

		// get height of window
		var windowHeight = $(window).height() - 80;

		// adjust for topbar, other stuff

		$('#geneList').height(windowHeight);
		submitQuery();
	});
</script>

</body>
</html>
