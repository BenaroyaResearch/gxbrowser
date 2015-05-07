<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Visualizer: MAT Plots</title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}"/>
    <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_mat.ico')}" type="image/x-icon" />
    <g:javascript src="jquery-min.js"/>
  </head>
  <body>
    <div class="container" style="margin-top:20px;">
      <h2>MAT Plots List</h2>
      <table class="zebra-striped">
        <thead><tr><th>Name</th><th>File Type</th></tr></thead>
        <tbody>
          <g:each in="${plots}" var="p">
            <tr><td><g:link action="matPlot" params="[plotId:p._id, fileType:p.fileType]">${p.name}</g:link></td><td>${p.fileType}</td></tr>
          </g:each>
        </tbody>
      </table>
      <g:link class="btn primary" action="index">Upload MAT Plot</g:link>
    </div>
  </body>
</html>