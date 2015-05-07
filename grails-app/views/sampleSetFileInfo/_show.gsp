<%@ page import="org.sagres.sampleSet.component.FileTag" %>

<g:javascript>
  $(document).ready(function() {

    $('#fileTag').autocomplete({
      source    : getBase()+"/fileTag/tagList",
      minLength : 0,
      delay     : 150
    });

    $('#showFileTags').click(function(e) {
      $('#fileTag').autocomplete("search","");
      $('#fileTag').focus();
      e.stopPropagation();
      e.preventDefault();
    });

    var updateSampleSetFiles = function() {
      var tag = $("#fileTagChooser").val();
      $.get(getBase()+"/sampleSet/files/${sampleSet.id}", { fileTag: tag }, function(files) {
        var html = "";
        $.each(files, function(i, file) {
          html += "<tr><td>"+file.filename+"</td><td>"+file.description+"</td><td><a href='"+getBase()+"/sampleSetFile/download/"+file.id+"' class='icon_download'></a></td></tr>";
        });
        $("#sampleset-files>tbody").html(html);
      });
    };

    $("select#fileTagChooser").change(function() {
      updateSampleSetFiles();
    });

  });
</g:javascript>
<div class="two-col-layout">
  <div class="col2container">
    <div class="col1container">
      <div class="col1" style="min-width: 30%">
        <div class="page-header">
          <h4>Upload Files</h4>
        </div>
          <sec:ifLoggedIn>
        <g:form name="filetag-form" class="form-stacked">
          <fieldset>
          <div class="clearfix">
            <label for="fileTag">Enter a tag</label>
            <div class="input">
              <div class="inline-inputs">
                <input type="text" id="fileTag" name="fileTag" class="xlarge" ${editable ? "" : "disabled='true'"}/>
                <button id="showFileTags" class="btn small primary" ${editable ? "" : "disabled='true'"}>List Tags</button>
              </div>
            </div>
          </div>
          <div class="clearfix">
            <label for="fileDescription">Enter a description</label>
            <div class="input">
              <textarea id="fileDescription" name="description" rows="4" class="xlarge" cols="50" ${editable ? "" : "disabled='true'"}></textarea>
            </div>
          </div>
          </fieldset>
        </g:form>
        <div id="fileupload" class="indent-value">
          <g:form name="file-upload-form" controller="sampleSetFile" action="upload" id="${sampleSet.id}"
                  enctype="multipart/form-data">
          <div class="fileupload-buttonbar">
            <label class="fileinput-button">
              <span>Add Files...</span>
              <input type="file" name="files[]" multiple>
            </label>
            <button type="submit" class="start" ${editable ? "" : "disabled='true'"}>Upload All</button>
            <button type="reset" class="cancel">Cancel All</button>
          </div>
          </g:form>
          <div class="fileupload-content">
            <table class="files"></table>
          </div>

        </div>

              </sec:ifLoggedIn>
           <sec:ifNotLoggedIn>

                <em>Please log in in order to upload files.</em>

            </sec:ifNotLoggedIn>
      </div>

      <div class="col2">
        <div class="page-header">
          <h4>Browse Files</h4>
        </div>
        <g:form>
          <fieldset>
          <div class="clearfix">
            <label for="fileTagChooser">View files for</label>
            <div class="input">
              <g:select name="fileTagChooser" from="${tags}" value="All Files"/>
            </div>
          </div>
          </fieldset>
        </g:form>
        <table id="sampleset-files" class="zebra-striped">
          <thead>
          %{--<tr><th>File</th><th>Description</th><th></th></tr>--}%
          <tr><th>File</th><th></th></tr>
          </thead>
          <tbody>
            <g:each in="${files}" var="file">
              <tr>
                <td><span title="${file.filename}">${file.filename}</span><br/>
                <g:if test="${file.description != null && !file.description.isEmpty()}">
                	<span class="filedesc" title="${file.description}">${file.description}</span>
                </g:if>
                </td>
                %{--<td>${file.filename}</td><td>${file.description}</td>--}%
                <td><g:link controller="sampleSetFile" action="download" id="${file.id}" class="icon_download"/></td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
<script id="template-upload" type="text/x-jquery-tmpl">
  <tr class="template-upload{{if error}} ui-state-error{{/if}}">
    <td class="name"><p>{{= name}}</p></td>
    {{if error}}
    <td class="error" colspan="2">Error:
    {{if error === 'maxFileSize'}}File is too big
    {{else error === 'minFileSize'}}File is too small
    {{else error === 'acceptFileTypes'}}Filetype not allowed
    {{else error === 'maxNumberOfFiles'}}Max number of files exceeded
    {{else error === 'tagNotFound'}}No tag was assigned to this file
    {{else}}{{= error}}
    {{/if}}
    </td>
    {{else}}
    <td class="progress" style="padding: 3px 3px 0 0"><div></div></td>
    <td class="start" style="padding: 3px 0 0 0;"><button class="smallButton" ${editable ? "" : "disabled='true'"}>Start</button></td>
    {{/if}}
    <td class="cancel" style="padding: 3px 2px 0;"><button class="smallButton">Cancel</button></td>
  </tr>
</script>