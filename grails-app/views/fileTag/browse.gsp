<html>

<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>

  <link rel="stylesheet" href="${resource(dir: 'css', file: 'list.css')}"/>

  <title>File Tags</title>

  <g:javascript src="jquery.dataTables.min.js"/>
  <g:javascript>
    $(document).ready(function() {


      $('#filetag-list tbody tr td').live('click', function() {
        var tag = $(this).text();
        var id = $(this).attr("id");
        $.getJSON("sampleFileList", { id: id }, function(result) {
          var html = "No files found."
          if (result != "")
          {
            html = "<ul class='file-list'>";
            $.each(result, function(id, file) {
              var path = file["filename"];
              var ext = file["extension"];
              html += "<li class='ext_" + ext + " file'>" + path + "</li>";
            });
            html += "</ul>"
          }
          $('#tagHeader').html("Browse Files: " + tag);
          $('#sampleSetFiles').html(html);
        });
      });
    });
  </g:javascript>

</head>

<body>
<div class="sampleset-container">

  <div class="page-header">
    <h2>File Tag Browser</h2>
  </div>

      <g:form controller="fileTag">
        <div class="clearfix">
          <label for="name">Add File Tag</label>
          <div class="input">
            <g:textField name="name"/> <button class="btn primary" type="submit" name="_action_add">Go</button>
          </div>
        </div>

      </g:form>
      <g:hasErrors bean="${fileTagInstance}">
        <g:eachError bean="${fileTagInstance}">
          <div class="error-message">
            <g:message error="${it}"/><br/>
          </div>
        </g:eachError>
      </g:hasErrors>

    <div class="two-col-layout">
      <div class="col2container">
      <div class="col1container">
      <div class="col1" style="min-width: 30%">
        <div class="scrollable-container">
          <div class="page-header">
            <h4>Tag List <small>- select a tag to view its files</small></h4>
          </div>
          <table id="filetag-list" class="zebra-striped">
          <tbody>
          <g:each in="${fileTagInstanceList}" status="i" var="fileTagInstance">
            <tr>
              <td id="${fileTagInstance.id}">${fileTagInstance.tag}</td>
            </tr>
          </g:each>
          </tbody>
        </table>
        </div>
      </div>
      <div class="col2" style="min-width: 50%">
        %{--<div>--}%
        <div class="page-header"><h4 id="tagHeader">Browse Files</h4></div>
        <div id="sampleSetFiles"><span class="help-block">Select a tag in the Tag List to view its files</span></div>
          %{--</div>--}%
      </div>
      </div>
      </div>
    </div>
  </div>
</body>
</html>