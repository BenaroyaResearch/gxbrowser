<!DOCTYPE html>
<html><head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<title>Module Analysis Wizard</title>
	<link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_mat.ico')}" type="image/x-icon" />
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'common.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'mat.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'instit-itn.css')}"/>
     <link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: grailsApplication.config.dm3.site.css)}"/>


 <g:javascript src="jquery-min.js"/>
  <g:javascript src="jquery-ui-min.js"/>
  <g:javascript src="common.js"/>
  <g:javascript src="jquery.qtip.min.js"/>
  <g:javascript src="bootstrap-dropdown.js"/>
  <g:javascript src="bootstrap-modal.js"/>

</head>

<body>
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

<div class="topbar">
	<div class="topbar-inner fill">
		<div class="mat-container" style="text-align: left;">
            <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

			<h3><a href="#"><strong>MAT</strong></a></h3>

			<form action="" onsubmit="submitQuery('top');return false;">
				<input id="topSearch" placeholder="Search" type="text">
			</form>
			<ul class="nav secondary-nav">
				<li class="dropdown">
					<a href="#" class="dropdown-toggle">Tools</a>
					<ul class="dropdown-menu skin-dropdown">
						<li><a href="${createLink(controller: 'analysis', action:'create')}">Run Analysis</a></li>
						<li><a href="${createLink(controller: 'analysis', action:'list')}">View Results</a></li>
						<li class="divider"></li>
                        <li><a href="#" onclick="reportBug(true);">Send Feedback</a></li>
                        <li class="divider"></li>
						<sec:ifLoggedIn>
							<li><a href="${createLink(controller: 'logout')}">Logout <sec:username/></a></li>
						</sec:ifLoggedIn>
						<sec:ifNotLoggedIn>
							<li><a href="${createLink(controller: 'login')}">Login</a></li>
						</sec:ifNotLoggedIn>


					</ul>
				</li>
			</ul>
		</div>
	</div><!-- /fill -->
</div><!-- /topbar -->


<g:layoutBody/>
 <g:render template="/common/bugReporter" model="[tool:'MAT']"/>
</body>
</html>