<!DOCTYPE html>
<html><head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<title>MAT Analysis</title>
	<link rel="shortcut icon" href="http://srvdm:8080/dm3/images/favicon.ico" type="image/x-icon">
	<link rel="stylesheet" href="http://srvdm:8080/dm3/css/bootstrap-1.2.0.css"/>
	<link rel="stylesheet" href="http://srvdm:8080/dm3/css/docs.css"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}">
	<script src="matAnalysis_files/application.js" type="text/javascript"></script>
	<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>
	<g:javascript library="tabber"/>
	<g:javascript library="jquery" plugin="jquery"/>
	<jqui:resources/>
	<g:layoutHead/>
	<g:javascript library="application"/>


	<meta name="layout" content="gxbmain">
	<g:javascript>



		function loadAvailableSampleGroups() {
			var e = document.getElementById("expressionDateFile");
			var sampleSetId = e.options[e.selectedIndex].value;
			var url = "/MAT/analysis/getAvailableGroupSets?sampleId=".concat(sampleSetId);
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
					var results = JSON.parse(xmlhttp.responseText);
					var d = document.getElementById("datasetGroup");

					d.style.display = "";
					var dd = document.getElementById("dataSetGroupSelector");
					for (i=dd.options.length-1;i>=1;i--)
					{
						dd.remove(i);
					}
					for (var key in results.groups) {
						dd.add(new Option(results.groups[key], key), null);
					}
				}
			}
			xmlhttp.open("GET", url, true);
			xmlhttp.send();
		}

		function loadGroups() {
			var e = document.getElementById("dataSetGroupSelector");
			var sg = e.options[e.selectedIndex].value;
			var url = "/MAT/analysis/getGroupsInSet?groupSet=".concat(sg);
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
					var results = JSON.parse(xmlhttp.responseText);
					var d = document.getElementById("sampleGroups");
					d.style.display = "block";
					var dd = document.getElementById("selectGroups");
					for (var key in results.groups) {
						var objNode =document.createElement("input");
						var nodeLabel =document.createElement("label");
						nodeLabel.htmlFor(results.groups[key]);
						objNode.type="radio";
						objNode.name=""
						//dd.add(new Option(results.groups[key], key), null);
					}
				}
			}
			xmlhttp.open("GET", url, true);
			xmlhttp.send();

		};

		function toggleAll(onOff) {
			toggle("displayDefaultParams");
		}

	</g:javascript>

	%{--<script type="text/javascript">--}%

  %{--var _gaq = _gaq || [];--}%
  %{--_gaq.push(['_setAccount', 'UA-27217617-1']);--}%
  %{--_gaq.push(['_setDomainName', 'benaroyaresearch.org']);--}%
  %{--_gaq.push(['_setAllowLinker', true]);--}%
  %{--_gaq.push(['_trackPageview']);--}%

  %{--(function() {--}%
    %{--var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;--}%
    %{--ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';--}%
    %{--var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);--}%
  %{--})();--}%

%{--</script>--}%

</head>

<body>

<div class="topbar">
	<div class="topbar-inner fill">
		<div class="container" style="text-align: left;">
            <img src="${resource(dir: 'images', file: 'logo_itn.png')}" alt="Immune Tolerance Network" class="itn_logo" />
			<h3><a href="#"><strong>MAT</strong></a></h3>

			<form action="" onsubmit="submitQuery('top');return false;">
				<input id="topSearch" placeholder="Search" type="text">
			</form>


			<ul class="nav secondary-nav">
				<li class="nav">
					<a href="/MAT/analysis/create" class="nav">Run new Analysis</a>
				</li>
				<li class="dropdown">
					<a href="#" class="dropdown-toggle">Advanced</a>
					<ul class="dropdown-menu itn-dropdown">
						<li><a href="/MAT/analysis/create">Run Analysis</a></li>
						<li><a href="/MAT/analysis/list">View Results</a></li>
						<sec:ifAllGranted roles="ROLE_ADMIN">
						<li><a href="/MAT/MATImage/list">Manage Images</a></li>
						<li><a href="/MAT/version/list">Manage Chip Version</a></li>
						</sec:ifAllGranted>
						<li class="divider"></li>
						<sec:ifLoggedIn>
							<li><a href="/MAT/logout/index">Logout <sec:username/></a></li>
						</sec:ifLoggedIn>
						<sec:ifNotLoggedIn>
							<li><a href="/MAT/login/index">Login</a></li>
						</sec:ifNotLoggedIn>

					</ul>
				</li>
			</ul>

		</div>
	</div><!-- /fill -->
</div><!-- /topbar -->

<script type="text/javascript">

	$(document).ready(function() {
		$("body").bind("click", function (e) {
			$('.dropdown-toggle, .menu').parent("li").removeClass("open");
		});
		$(".dropdown-toggle, .menu").click(function (e) {
			var $li = $(this).parent("li").toggleClass('open');
			return false;
		});
	});

</script>

<g:layoutBody/>
</div>

</body></html>