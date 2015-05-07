<%@ page import="common.chipInfo.Species; org.sagres.sampleSet.SampleSet; org.sagres.sampleSet.component.LookupList; org.sagres.sampleSet.component.LookupListDetail" %>

<g:javascript>

	function clearAnnotation(id) {
		var args = { id: id };
		//console.log("about to clear annotation for Sample Set: " + id);
	    $.post(getBase()+"/sampleSet/resetSampleSet", args, function(result) {
	        //console.log("Clear Annotation completed");
	   		location.reload();
	    });
    }

  $(document).ready(function() {
    $("table#samplesPreview").tablesorter();
    $("th.header").live("mouseup", function() {
      updateSamplesPreview($(this));
    });
    function updateSamplesPreview(header)
    {
      var sortDir = header.hasClass("headerSortDown") ? -1 : 1;
      var args = { sortKey: header.attr("id"), sortDir: sortDir };
      $.get(getBase()+"/sampleSet/previewSamples/${sampleSetId}", args, function(result) {
        var samples = result["samples"];
        var html = "";
        $.each(samples, function(key, value) {
          html += "<tr>";
          $.each(value, function(i, v) {
            html += "<td>"+v+"</td>";
          });
          html += "</tr>";
        });
        $("table#samplesPreview>tbody").html(html);
      });
    }
    $("input.optionsSampleSource").click(function() {
      var selected = this.checked;
      var sampleSourceId = $(this).val();
      var args = { selected: selected, sampleSourceId: sampleSourceId };
      $.post(getBase()+"/sampleSetSampleInfo/setSampleSources/${sampleSetSampleInfo.id}", args);
    });

    $("input#optionsSampleSourceOther").keyup(function(e) {
      var code = (e.keyCode ? e.keyCode : e.which);
      if (code === 13)
      {
        var thisChkBox = $(this);
        var newSampleSource = thisChkBox.val();
        var args = { otherSampleSource: newSampleSource };
        $.post(getBase()+"/sampleSetSampleInfo/setSampleSources/${sampleSetSampleInfo.id}", args, function(newId) {
          var checkbox = $("input.optionsSampleSource[value='"+newId+"']");
          if (checkbox.length > 0)
          {
            checkbox.prop("checked",true);
          }
          else
          {
            $("ul#sampleSources").prepend("<li><label>" +
             "<input type='checkbox' name='optionsSampleSource' id='optionsSampleSource' class='optionsSampleSource' value='"+newId+"' checked='true'/> " +
             newSampleSource+"</label></li>");
          }
          thisChkBox.val("");
        });
      }
      else if (code === 27)
      {
        $(this).val("");
      }
    });
  });
</g:javascript>
<div class="two-col-layout two-col-even">
  <div class="col2container">
    <div class="col1container">
      <div class="column-left">

        <g:form name="sampleSetSampleInfo">
          <fieldset>
            <div class="clearfix">
              <label>Expected Samples:</label>
              <div class="input">

                  <sec:ifLoggedIn>
                  <input type="number" name="${sampleSetSampleInfo.id}::numberOfSamples" id="${sampleSetSampleInfo.id}::numberOfSamples"
                  class="editable-text small" min="1" value="${sampleSetSampleInfo.numberOfSamples}" ${editable ? "" : "disabled='true'"}/>
                     <span style="padding-left:5px;">Samples Loaded: ${numSamples}</span>
                  </sec:ifLoggedIn>

                  <sec:ifNotLoggedIn>
                   <span class="notlogged-replace">${sampleSetSampleInfo.numberOfSamples} </span>
                       <span style="padding-left:25px; padding-top:8px; display:block; float: left;">Samples Loaded: ${numSamples}</span>
                  </sec:ifNotLoggedIn>

              </div>
            </div>
            <div class="clearfix">
              <label>Species</label>
              <div class="input">
                  <sec:ifLoggedIn>
                <g:select name="species" noSelection="['-select-':'-select-']"
                          from="${Species.list()}"
                          value="${sampleSetSampleInfo.species?.id}" optionKey="id" optionValue="latin"
                          class="updateSampleSetInfo" disabled="${!editable}"/>
                %{--<div id="hiddenOther" style="display:none;" class="top-margin-less">--}%
                  %{--<input type="text" id="other-species" name="other-species" class="other xlarge" ${editable ? "" : "disabled='true'"}/>--}%
                  %{--<span class="help-block" style="margin: 0 0 0.1em 0.4em;">Press enter to add to Species and ESC to cancel</span>--}%
                %{--</div>--}%
                   </sec:ifLoggedIn>

                  <sec:ifNotLoggedIn>
                     <span class="notlogged-replace">${sampleSetSampleInfo.species}  </span>
                    </sec:ifNotLoggedIn>

              </div>
            </div>
            <div class="clearfix">
              <label>Sample Source</label>
              <div class="input">

                  <sec:ifLoggedIn>
                <g:set var="sampleSetSampleSources" value="${sampleSetSampleInfo.sampleSources.toList()}"/>
                <ul id="sampleSources" class="checklist inputs-list">
                <g:each in="${sampleSetSampleSources}" var="sampleSource">
                  <li><label>
                    <g:checkBox name="optionsSampleSource" class="optionsSampleSource" value="${sampleSource.id}" checked="${true}" disabled="${!editable}"/> ${sampleSource.name}
                  </label></li>
                </g:each>
                <g:each in="${LookupList.findByName('Sample Source').lookupDetails.toList()}" var="sampleSource">
                  <g:if test="${!sampleSetSampleSources.contains(sampleSource)}">
                    <li><label>
                      <g:checkBox name="optionsSampleSource" class="optionsSampleSource" value="${sampleSource.id}" checked="${false}" disabled="${!editable}"/> ${sampleSource.name}
                    </label></li>
                  </g:if>
                </g:each>
                </ul>
                <div style="margin-top:10px;">
                  Other <input type="text" id="optionsSampleSourceOther" name="optionsSampleSourceOther" class="large" ${editable ? "" : "disabled='true'"}/>
                  <span class="help-block" style="margin: 0 0 0.1em 0;">Press enter to add to Sample Source</span>
                </div>
                      </sec:ifLoggedIn>

                  <sec:ifNotLoggedIn>
                       <span class="notlogged-replace">
                       <g:each in="${sampleSetSampleInfo.sampleSources}" var="sampleSource" status="index">
                            <g:if test="${index > 0}">
                               ,
                           </g:if>
                          ${sampleSource}
                       </g:each>
                      </span>

                  </sec:ifNotLoggedIn>
              </div>
            </div>
            <div class="clearfix">
              <label>Disease</label>
              <div class="input">
                  <sec:ifLoggedIn>
                <g:set var="diseaseLookupList" value="${LookupList.findByName('Disease')}"/>
                <g:select from="${LookupList.findByName('Disease').lookupDetails.toList()+[id:'Other', name:'Other']}"
                          name="disease" noSelection="['-select-':'-select-']"
                          optionKey="id" optionValue="name"
                          value="${sampleSetSampleInfo.disease?.id}"
                          class="updateSampleSetInfo" disabled="${!editable}"/>
                <div id="hiddenOther" style="display:none;" class="top-margin-less">
                  <input type="text" id="${diseaseLookupList.id}" name="${diseaseLookupList.id}" class="other xlarge" ${editable ? "" : "disabled='true'"}/>
                  <span class="help-block" style="margin: 0 0 0.1em 0.4em;">Press enter to add to Disease and ESC to cancel</span>
                </div>
                      </sec:ifLoggedIn>

                      <sec:ifNotLoggedIn>
                           <span class="notlogged-replace">${sampleSetSampleInfo.disease}</span>
                      </sec:ifNotLoggedIn>
              </div>
            </div>
            <div class="clearfix">
              <label for="${sampleSetSampleInfo.id}::treatmentProtocol">Treatment Protocol</label>

              <div class="input">
                  <sec:ifLoggedIn>
                <textarea name="${sampleSetSampleInfo.id}::treatmentProtocol" id="${sampleSetSampleInfo.id}::treatmentProtocol" class="editable-text xlarge"
                             cols="50" rows="3" ${editable ? "" : "disabled='true'"}>${sampleSetSampleInfo.treatmentProtocol}</textarea>
                      </sec:ifLoggedIn>

                  <sec:ifNotLoggedIn>
                     <span class="notlogged-replace">${sampleSetSampleInfo.treatmentProtocol}  </span>

                  </sec:ifNotLoggedIn>
              </div>
            </div>

            <div class="clearfix">
              <label for="${sampleSetSampleInfo.id}::growthProtocol">Growth Protocol</label>

              <div class="input">

                  <sec:ifLoggedIn>
                <textarea name="${sampleSetSampleInfo.id}::growthProtocol" id="${sampleSetSampleInfo.id}::growthProtocol" class="editable-text xlarge"
                             cols="50" rows="3" ${editable ? "" : "disabled='true'"}>${sampleSetSampleInfo.growthProtocol}</textarea>
                   </sec:ifLoggedIn>

                  <sec:ifNotLoggedIn>
                     <span class="notlogged-replace"> ${sampleSetSampleInfo.growthProtocol} </span>

                  </sec:ifNotLoggedIn>

              </div>
            </div>

            <div class="clearfix">
              <label for="${sampleSetSampleInfo.id}::extractionProtocol">Extraction Protocol</label>

              <div class="input">
                  <sec:ifLoggedIn>
                <textarea name="${sampleSetSampleInfo.id}::extractionProtocol" id="${sampleSetSampleInfo.id}::extractionProtocol" class="editable-text xlarge"
                             cols="50" rows="3" ${editable ? "" : "disabled='true'"}>${sampleSetSampleInfo.extractionProtocol}</textarea>

                   </sec:ifLoggedIn>

                  <sec:ifNotLoggedIn>
                     <span class="notlogged-replace"> ${sampleSetSampleInfo.extractionProtocol} </span>

                  </sec:ifNotLoggedIn>
              </div>
            </div>

            <div class="clearfix">
              <label for="${sampleSetSampleInfo.id}::storageConditions">Storage Conditions</label>

              <div class="input">
                  <sec:ifLoggedIn>
                <textarea name="${sampleSetSampleInfo.id}::storageConditions" id="${sampleSetSampleInfo.id}::storageConditions" class="editable-text xlarge"
                             cols="50" rows="3" ${editable ? "" : "disabled='true'"}>${sampleSetSampleInfo.storageConditions}</textarea>

                  </sec:ifLoggedIn>

                  <sec:ifNotLoggedIn>

                      <span class="notlogged-replace">${sampleSetSampleInfo.storageConditions}</span>
                  </sec:ifNotLoggedIn>
              </div>
            </div>

          </fieldset>
        </g:form>
      </div>

      <div class="column-right">
        <div class="sampleSetInfo">
          <h5>Sample Set Spreadsheet</h5>
            <sec:ifLoggedIn>
          <div style="padding-bottom:10px;">
          <g:if test="${sampleSetSampleInfo.sampleSetSpreadsheet}">
            Current File: <span id="spreadsheetFilename">${sampleSetSampleInfo.sampleSetSpreadsheet.filename}</span>
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SETAPPROVAL">
 				<button class="btn error" onclick="javascript:clearAnnotation(${sampleSetId});return false;">Clear</button>
		  	</sec:ifAnyGranted>
          </g:if>
          <g:else>
            <span id="noSpreadsheet" class="hint">Please upload a spreadsheet.</span>
          </g:else>
          </div>

          <div class="indent-value">
            <div id="spreadsheetupload">
              %{--<div style="padding: 5px 0 10px">Uploaded File: <span id="filename"><g:abbreviate--}%
                %{--value="${sampleSetSampleInfo.sampleSetSpreadsheet?.filename}"--}%
                %{--hint="Please upload a spreadsheet"/></span></div>--}%
              <g:form controller="sampleSetFile" action="upload" id="${sampleSetId}" enctype="multipart/form-data">
                <div class="fileupload-buttonbar">
                  <label class="fileinput-button">
                    <span>Select File...</span>
                    <input type="file" name="files[]" multiple>
                  </label>
                  <button type="submit" class="start" ${editable ? "" : "disabled='true'"}>Upload</button>
                  <button type="reset" class="cancel">Cancel</button>
                  
                </div>
              </g:form>
               <div class="fileupload-content">
                <table class="files"></table>
              </div>
            </div>
          </div>

          <h5>Spreadsheet Templates</h5>
          <dl>
            <dt><span class="ext_xls">Excel Spreadsheet</span></dt>
            <dd>Microsoft Excel files - <a href="../../docs/spreadsheet_template.xlsx">Download</a></dd>
            <dt><span class="ext_csv">CSV Spreadsheet</span></dt>
            <dd>Comma-separated files - <a href="../../docs/spreadsheet_template.csv">Download</a></dd>
          </dl>

           </sec:ifLoggedIn>

            <sec:ifNotLoggedIn>

                <em>Please log in in order to upload files.</em>

            </sec:ifNotLoggedIn>

          <h5><span class="bold"><a id="samplesLink" href="#">Samples Preview</a></span></h5>
          %{--<g:if test="${sampleSetSampleInfo.sampleSetSpreadsheet && numSamples > 0}">--}%
              <table id="samplesPreview" class="zebra-striped pretty-table">
              <thead><tr>
                <th id="sampleBarcode">Sample ID</th>
                <g:each in="${headers}" var="header">
                  <th id="${header.key}">${header.displayName}</th>
                </g:each>
              </tr></thead>
              <tbody>
              <g:each in="${previewSamples}" var="previewSamp">
                <tr>
                  <td>${previewSamp.sampleBarcode}</td>
                  <g:each in="${headers}" var="header">
                    <td>${previewSamp.get("values")?.get(header.key)}</td>
                  </g:each>
                </tr>
              </g:each>
              </tbody>
              </table>
          %{--</g:if>--}%
          %{--<g:else>--}%
            %{--<span id="noSpreadsheet" class="hint">No sample data found.</span>--}%
          %{--</g:else>--}%
        </div>

      </div>
    </div>
  </div>
</div>
