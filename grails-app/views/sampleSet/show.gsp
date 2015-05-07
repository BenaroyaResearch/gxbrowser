<%@ page import="common.chipInfo.GenomicDataSource; org.sagres.sampleSet.component.LookupList; grails.converters.JSON" %>
<html>
<head>
%{--<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>--}%
<meta name="layout" content="sampleSetMain"/>

<link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.fileupload-ui.css')}"/>

<title>Sample Set Annotation Tool: ${sampleSet.name}</title>

%{--<script src="//ajax.aspnetcdn.com/ajax/jquery.templates/beta1/jquery.tmpl.min.js"></script>--}%
<g:javascript src="jquery.tmpl.min.js"/>
%{--<g:javascript src="jquery.dataTables.min.js"/>--}%
<g:javascript src="jquery.summarize.js"/>
<g:javascript src="jquery.iframe-transport.js"/>
<g:javascript src="jquery.fileupload.js"/>
<g:javascript src="jquery.fileupload-ui.js"/>
<g:javascript src="jquery.tablesorter.min.js"/>
  
<g:javascript>
        $(document).ready(function() {
            $(".min-max").summarize();

            // show the tab content for the selected tab
            $("ul.tabs>li>a").click(function() {
              var tabId = $(this).attr("href");
              var oldTab = $("ul.tabs>li[class='active']").removeClass("active");
              var oldTabId = oldTab.find("a").attr("href");
              $(this).parent().addClass("active");
              $(oldTabId).hide();
              $(tabId).show(1, function() {
                $(this).scrollTop(0);
              });
            });

            $("#sampleset").delegate(".placeholder", "click", function() {
              var text = $(this).parent().find("div#text");
              if (text.attr("contenteditable") === "true")
              {
                if (!text.is(":visible"))
                {
                  text.addClass("summarizing");
                }
                text.show().trigger("focus");
                $(this).parent().find("div#summary").hide();
                $(this).detach();
              }
            });

            $("#sampleset").delegate(".summary", "click", function() {
              if ($(this).parent().find("div#text").attr("contenteditable") === "true")
              {
                $(this).hide();
                $(this).parent().find("div#text").addClass("summarizing").show().trigger("focus");
              }
            });

            $("#sampleset").delegate(".sampleset-content", "blur", function() {
              var editable = $(this);
              editable.parent().find(".min-max").show().summarize();

              var controller = "sampleSet";
              var ssId = ${sampleSet.id};
              if (editable.hasClass("annotation"))
              {
                controller = "sampleSetAnnotation";
                ssId = ${sampleSet.sampleSetAnnotation.id};
              }
              else if (editable.hasClass("sample"))
              {
                controller = "sampleSetSampleInfo";
                ssId = ${sampleSet.sampleSetSampleInfo.id};
              }
              var id = editable.parent().attr("id");
              var oldText = localStorage.getItem(id);
              var newText = $.trim(editable.html());
              if (oldText != newText)
              {
                $.post(getBase()+"/"+controller+"/setter/"+ssId, { property: id, value: newText, clean: true }, function(value) {
                  if (value == "error")
                  {
                    editable.html(oldText);
                  }
                  else if (value != "")
                  {
                    editable.html(value);
                  }
                  else
                  {
                    editable.html("");
                    editable.after("<div class='placeholder hint'><p>click here to enter text</p></div>");
//                    editable.after("<p><span class='placeholder hint'>click here to enter text</span></p>");
                  }
                  editable.parent().find(".min-max").summarize();
                });
              }

              if (editable.hasClass("summarizing")) {
                editable.removeClass("summarizing");
                editable.hide();
                editable.parent().find("div#summary").show();
              }

              if (newText == "")
              {
                if (editable.parent().find(".placeholder").length <= 0)
                {
                  editable.after("<div class='placeholder hint'><p>click here to enter text</p></div>");
//                  editable.after("<p><span class='placeholder hint'>click here to enter text</span></p>");
                }
              }
            });

             $("#samplesLink").click(function() {
              var oldTab = $("ul.tabs>li[class='active']").toggleClass("active");
              var oldTabId = oldTab.find("a").attr("href");
              $(oldTabId).toggle();
              $("ul.tabs>li>a[href='#tab-samples']").parent().toggleClass("active");
              $("#tab-samples").toggle();
            });

            $("#uploadSpreadsheetLink").click(function() {
              var oldTab = $("ul.tabs>li[class='active']").toggleClass("active");
              var oldTabId = oldTab.find("a").attr("href");
              $(oldTabId).toggle();
              $("ul.tabs>li>a[href='#tab-sample-info']").parent().toggleClass("active");
              $("#tab-sample-info").toggle();
            });

            $("#sampleset").delegate(".sampleset-content", "focus", function() {
              $(this).parent().find(".placeholder").detach();

              $(this).parent().find(".min-max").hide();
              var fullText = $.trim(this.innerHTML);
              if (this.id == "summary")
              {
                fullText = $(this).parent().find("#text").html();
              }
              var id = $(this).parent().attr("id");
              localStorage.setItem(id, fullText);
            });

            $("#sampleset").delegate(".sampleset-content", "keyup", function(e) {
              var code = (e.keyCode ? e.keyCode : e.which);
              if (code === 27)
              {
                var id = $(this).parent().attr("id");
                var oldText = localStorage.getItem(id);
                $(this).html(oldText).trigger("blur");
              }
            });

            function updateFileTagChooserAndFilesTable() {
              $.ajaxSetup({async: false, cache: false});
              // update file tag chooser
              $.getJSON(getBase()+"/sampleSet/sampleSetTags/${sampleSet.id}", function(tags) {
                var selected = $("select#fileTagChooser").val();
                var options = "";
                for (var i = 0; i < tags.length; i++)
                {
                  options += "<option value='" + tags[i] + "'>" + tags[i]+ "</option>";
                }
                $("select#fileTagChooser").html(options).val(selected).trigger("change");
              });
              $.ajaxSetup({async: true, cache: true});
            }

            function checkTag() {
              $.ajax({
                type  : "POST",
                url   : getBase()+"/sampleSetFile/checkTag",
                data  : { tag: $("input#fileTag").val() },
                cache : false,
                async : false
              });
            }

            function updateNumberOfSamples() {
              $.ajax({
                type    : "GET",
                url     : getBase()+"/sampleSetSampleInfo/getter/${sampleSet.sampleSetSampleInfo.id}",
                data    : { property: "numberOfSamples" },
                cache   : false,
                async   : false,
                success : function(result) {
                  $("div#numberOfSamples").html(result);
                }
              });
            }

            function updateSamplesEditor() {
              $.ajax({
                type: "GET",
                url: getBase()+"/sampleSet/samplesEditor/${sampleSet.id}",
                async: false,
                cache: false,
                success: function(html) {
                  $("#tab-samples").html(html);
                }
              });
            }

            /***** Generic File Uploads *****/
            $("#fileupload").bind("fileuploadstart", function() {
              checkTag();
            });

            // Initialize the jQuery File Upload widget:
            $('#fileupload').fileupload({
              autoUpload: false,
              formData: function() {
                return [ { name: "tag", value: $("input#fileTag").val() },
                    { name: "description", value: $("textarea#fileDescription").val() } ];
              }
            });

            // Load existing files:
            //$.getJSON($('#fileupload form').prop('action'), function (files) {
            //    var fu = $('#fileupload').data('fileupload');
            //    fu._adjustMaxNumberOfFiles(-files.length);
           // });

            // Open download dialogs via iframes,
            // to prevent aborting current uploads:
            $('#fileupload .files a:not([target^=_blank])').live('click', function (e) {
                e.preventDefault();
                $('<iframe style="display:none;"></iframe>')
                    .prop('src', this.href)
                    .appendTo('body');
            });

            $('#fileupload').bind('fileuploaddone', function () {
              updateNumberOfSamples();
              updateFileTagChooserAndFilesTable();
            });
            /***** END Generic File Uploads *****/

            function updateSamplesPreview() {
              var args = { sortKey: "sampleId", sortDir: 1 };
              $.get("../../sampleSet/previewSamples/${sampleSet.id}", args, function(result) {
                var html = "<thead><tr><th id='sampleId'>Sample ID</th>";
                var headers = result["headers"];
                var samples = result["samples"];
                $.each(headers, function(key, value) {
                html += "<th id="+key+">"+value.displayName+"</th>";
                });
                html += "</tr></thead><tbody>";
                $.each(samples, function(key, value) {
                html += "<tr>";
                $.each(value, function(i, v) {
                html += "<td>"+v+"</td>";
                });
                html += "</tr>";
                });
                html += "</tbody>";
                $("#noSpreadsheet").detach();
                $("table#samplesPreview").html(html);
                $("table#samplesPreview").tablesorter();
              });
            }


            /***** Spreadsheet File Upload *****/
            $('#spreadsheetupload').bind('fileuploadstart', function() {
              checkTag();
            });

            // Initialize the jQuery File Upload widget:
            $('#spreadsheetupload').fileupload({
              autoUpload: false,
              formData: function() {
                return [ { name: 'tag', value: 'Sample Set Spreadsheet' },
                    { name: 'description', value: 'Sample Set Spreadsheet' } ];
              }
            });

            // Load existing files:
//            $.getJSON($('#spreadsheetupload form').prop('action'), function (files) {
//                var fu = $('#spreadsheetupload').data('fileupload');
//                fu._adjustMaxNumberOfFiles(-files.length);
//            });

            // Open download dialogs via iframes,
            // to prevent aborting current uploads:
            $('#spreadsheetupload .files a:not([target^=_blank])').live('click', function (e) {
                e.preventDefault();
                $('<iframe style="display:none;"></iframe>')
                    .prop('src', this.href)
                    .appendTo('body');
            });

            $('#spreadsheetupload').bind('fileuploaddone', function (e, data) {
              $.ajaxSetup({cache: false});
              $("span#spreadsheetFilename").html(data.files[0].name);
              updateNumberOfSamples();
              updateFileTagChooserAndFilesTable();
              updateSamplesEditor();
              updateSamplesPreview();
              $.ajaxSetup({cache: true});
            });
            /***** END Spreadsheet File Upload *****/

            $("select#status").change(function() {
              $.post("../../sampleSet/setStatus/${sampleSet.id}", { value: $(this).val() });
            });
            $("select#study-type").change(function() {
              $.post("../../sampleSet/setStudyType/${sampleSet.id}", { value: $(this).val() });
            });

            $('.updateSampleSetInfo').focus(function() {
                  var value = $(this).parent().parent().find(".updateSampleSetInfo").val();
                localStorage.setItem("${sampleSet.sampleSetSampleInfo.id}::"+$(this).attr("id"), value);
            }).change(function() {
                var lookupList = $(this).attr("name");
                var value = $(this).val();
                var isSelect = $(this).prop("tagName").toLowerCase() === "select";
                if (value != '-select-')
                {
                    if (value == 'Other')
                    {
                        $(this).parent().find('#hiddenOther').show();
                        $(this).parent().find('#hiddenOther>input').focus();
                    }
                    else
                    {
                        $(this).parent().find('#hiddenOther').hide();
                        $.ajax({
                            type : "POST",
                            url  : "../../sampleSetSampleInfo/setter/${sampleSet.sampleSetSampleInfo.id}",
                            data : { property: lookupList, value: value, isSelect: isSelect },
                            cache: false,
                            async : false
                        });
                    }
                }
                else
                {

                  var args = { property: lookupList, value: null, isSelect: isSelect };
                  $.post(getBase()+"/sampleSetSampleInfo/setter/${sampleSet.sampleSetSampleInfo.id}", args);
                  $(this).parent().find('#hiddenOther').hide();
                }
            });

            $('.other').keyup(function(e) {
              var code = (e.keyCode ? e.keyCode : e.which);
              if (code == 13)
              {
                setOtherDetail($(this));
              }
              else if (code == 27)
              {
                revertOther($(this));
              }
            }).blur(function() {
              var value = $.trim($(this).val());
              if ($(this).is(":visible") && value != "")
              {
                setOtherDetail($(this));
              }
              else
              {
                revertOther($(this));
              }
            });

            function revertOther(field)
            {
              var id = field.parent().parent().find('.updateSampleSetInfo').attr("id");
              var oldValue = localStorage.getItem("${sampleSet.sampleSetSampleInfo.id}::"+id);
              field.parent().parent().find(".updateSampleSetInfo").val(oldValue);
              field.parent().hide();
              field.val("");
            }

            function setOtherDetail(field)
            {
              var lookupList = field.attr("id");
              var value = field.val();
              var hiddenField = field.parent();
              var newValue;
              $.ajax({
                type : "POST",
                url  : "../../sampleSetSampleInfo/setOtherDetail",
                data : { value: value, lookupListId: lookupList, id: ${sampleSet.sampleSetSampleInfo.id} },
                cache: false,
                async : false,
                success: function(result) {
                  hiddenField.find('.other').val("");
                  hiddenField.hide();
                  newValue = result;
                }
              });
              if (newValue != "duplicate")
              {
                field.parent().parent().find('.updateSampleSetInfo option[value="Other"]').remove();
                field.parent().parent().find('.updateSampleSetInfo').append(new Option(value,newValue));
                field.parent().parent().find('.updateSampleSetInfo').append(new Option("Other","Other"));
              }
              else
              {
                field.parent().parent().find(".updateSampleSetInfo option:contains('"+value+"')").each(function() {
                  if (this.text === value)
                  {
                    newValue = this.value;
                  }
                });
              }
              field.parent().parent().find('.updateSampleSetInfo').focus().val(newValue);
            }
        });
</g:javascript>

</head>

<body>

<div id="sampleset" class="sampleset-container">
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div class="errors" style="display:none"></div>

 <sec:ifNotLoggedIn> <div class="not-editable"></sec:ifNotLoggedIn>
     <div id="name" class="page-header">
    <h2 contenteditable="${editable}" class="sampleset-content" title="${sampleSet.name}">${sampleSet.name}</h2>
  </div>
 <sec:ifNotLoggedIn></div></sec:ifNotLoggedIn>


  <div class="project-status-container">
    <div class="project-status">
      <g:form>
        <fieldset>
          <div class="clearfix">
            <label for="status">Status:</label>

            <div class="input">
                <sec:ifLoggedIn>
              <g:select from="${LookupList.findByName('Status')?.lookupDetails}" name="status" optionKey="name"
                        optionValue="name" value="${sampleSet.status}" disabled="${!editable}"/>
                </sec:ifLoggedIn>

                <sec:ifNotLoggedIn>

                    <span class="notlogged-replace">${sampleSet.status} </span>

                </sec:ifNotLoggedIn>
            </div>
          </div>

          <div class="clearfix">
            <label for="study-type">Study Type:</label>

            <div class="input">
                <sec:ifLoggedIn>
              <g:select from="${LookupList.findByName('Study Type')?.lookupDetails}" name="study-type" optionKey="name"
                        optionValue="name" value="${sampleSet.studyType}" disabled="${!editable}"/>
              </sec:ifLoggedIn>

              <sec:ifNotLoggedIn>
                   <span class="notlogged-replace">${sampleSet.studyType}  </span>

              </sec:ifNotLoggedIn>


            </div>  <!--end input-->
          </div> <!--end clearfix-->
        </fieldset>
      </g:form>
    </div>

     <sec:ifNotLoggedIn> <div class="not-editable"></sec:ifNotLoggedIn><div id="description">
      <span class='min-max ui-icon-min'></span>

      <div id="text" contenteditable="${editable}" class="sampleset-content">
        ${sampleSet.description.decodeHTML()}
      </div>
    </div>
      <sec:ifNotLoggedIn></div></sec:ifNotLoggedIn>
  </div>

  <div>
    <ul class="tabs">
      <li${!params.tab || params.tab == "study-design" ? " class='active'" : ""}><a href="#tab-study-design">Study Design</a></li>
      <li${params.tab == "assay" ? " class='active'" : ""}><a href="#tab-assay">Assay</a></li>
      <li${params.tab == "sample-info" ? " class='active'" : ""}><a href="#tab-sample-info">Sample Info</a></li>
      <li${params.tab == "files" ? " class='active'" : ""}><a href="#tab-files">Files</a></li>
      <sec:ifAnyGranted roles="ROLE_USER,ROLE_ADMIN">
        <li${params.tab == "admin" ? " class='active'" : ""}><a href="#tab-admin">Admin</a></li>
        <g:if test="${sampleSet?.genomicDataSource?.name == 'ITN' && editable}">
        <li${params.tab == "itnConfig" ? " class='active'" : ""}><a href="#tab-itnConfig">ITN Config</a></li>
        </g:if>
      </sec:ifAnyGranted>
      <li${params.tab == "info" ? " class='active'" : ""}><a href="#tab-info">Info</a></li>
      <li${params.tab == "samples" ? " class='active'" : ""}><a href="#tab-samples">Samples</a></li>
      <li${params.tab == "group-sets" ? " class='active'" : ""}><a href="#tab-group-sets">Group Sets</a></li>
      <li${params.tab == "analysis" ? " class='active'" : ""}><a href="#tab-analysis">Analysis</a></li>
      <g:if test="${showTg2}">
        <li${params.tab == "tg2-quality-info" ? " class='active'" : ""}><a href="#tab-tg2-quality-info">TG2 Quality Info</a></li>
        <li${params.tab == "tg2-sample-info" ? " class='active'" : ""}><a href="#tab-tg2-sample-info">TG2 Sample Info</a></li>
      </g:if>
    </ul>

    <div class="tab-contents">
      <div class="tab-content${!params.tab || params.tab == "study-design" ? "-active" : ""}" id="tab-study-design">
        <g:render template="/sampleSetAnnotation/show"
                  model="['sampleSetOverviewComponents':sampleSetOverviewComponents, 'sampleSet':sampleSet, 'editable':editable]"/>
      </div>

      <div class="tab-content${params.tab == "assay" ? "-active" : ""}" id="tab-assay">
        <g:render template="/sampleSetPlatformInfo/show"
                  model="['sampleSetPlatFormInfo':sampleSet.sampleSetPlatformInfo, 'editable':editable]"/>
      </div>

      <div class="tab-content${params.tab == "sample-info" ? "-active" : ""}" id="tab-sample-info">
        <g:render template="/sampleSetSampleInfo/show"
                  model="['sampleSetId':sampleSet.id, 'sampleSetSampleInfo':sampleSet.sampleSetSampleInfo, 'previewSamples':previewSamples, 'headers':previewHeaders, 'numSamples':samples.size(), 'editable':editable]"/>
      </div>

      <div class="tab-content${params.tab == "files" ? "-active" : ""}" id="tab-files">
        <g:render template="/sampleSetFileInfo/show" model="['sampleSet':sampleSet, 'tags': tags, 'editable':editable]"/>
      </div>

      <sec:ifLoggedIn>
        <div class="tab-content${params.tab == "admin" ? "-active" : ""}" id="tab-admin">
          <g:render template="/sampleSetAdminInfo/show" model="['sampleSetAdminInfo':sampleSet.sampleSetAdminInfo, 'editable':editable]"/>
        </div>

        <g:if test="${sampleSet?.genomicDataSource?.name == 'ITN'  && editable}">
          <div class="tab-content${params.tab == "itnConfig" ? "-active" : ""}" id="tab-itnConfig">
            <g:render template="itnConfig" model="['sampleSet':sampleSet, 'labkeyReports':labkeyReports, 'headers':sampleHeaders, 'categoryToHeaders':categoryToHeaders, 'editable':editable]"/>
          </div>
        </g:if>

      </sec:ifLoggedIn>



      <div class="tab-content${params.tab == "info" ? "-active" : ""}" id="tab-info">
        <g:render template="info" model="['sampleSet':sampleSet, 'editable':editable]"/>
      </div>

      <div class="tab-content${params.tab == "samples" ? "-active" : ""}" id="tab-samples">
        <g:render template="/sampleSet/samplesEditorTemplate" model="['sampleSetId':sampleSet.id, 'samples':samples, 'headers':sampleHeaders, 'editable':editable]"/>
      </div>

      <div class="tab-content${params.tab == "group-sets" ? "-active" : ""}" id="tab-group-sets">
        <g:render template="groups" model="['sampleSet':sampleSet, 'editable':editable, 'signalDataTable':signalDataTable]"/>
      </div>

      <div class="tab-content${params.tab == "analysis" ? "-active" : ""}" id="tab-analysis">
        <g:render template="analysis" model="['sampleSet':sampleSet, 'editable':editable, 'groupSetToAnalyses':groupSetToAnalyses]"/>
      </div>

      <g:if test="${showTg2}">
        <div class="tab-content${params.tab == "tg2-quality-info" ? "-active" : ""}" id="tab-tg2-quality-info">
          <g:render template="tg2QualityInfo" model="['sampleSet':sampleSet, 'tg2QualityInfo':tg2QualityInfo, 'editable':editable]"/>
        </div>

        <div class="tab-content${params.tab == "tg2-sample-info" ? "-active" : ""}" id="tab-tg2-sample-info">
          <g:render template="tg2SampleInfo" model="['tg2SampleInfo':tg2SampleInfo, 'editable':editable]"/>
        </div>
      </g:if>

    </div>

  </div>

  <g:render template="addToCollection" model="['sampleSet':sampleSet]"/>

</body>
</html>
