<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="MATWizard"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>

</head>

<div class="mat-container">
	<h1>Module Analysis Wizard</h1>

	<h2>Upload Signal Data</h2>

    <g:if test="${flash.message}">
		<div class="alert-message matwiz-message error message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${wizardInstance}">
    <div class="alert-message block-message matwiz-message error">
			<g:renderErrors bean="${wizardInstance}" as="list"/>
		</div>
	</g:hasErrors>
  <div id="file-progress-msg" class="alert-message matwiz-message" style="display:none;">Please wait while your file is being uploaded</div>

	<g:uploadForm action="saveUploadData" enctype="multipart/form-data" class="mat-mainform" name="wizardForm">
		<g:hiddenField name="step" value="1"/>
		<div class="mat-upload-form">

			<p>This wizard will allow you to run a module analysis using your own data and then view the results using the interactive module analysis browser.</p>
			<p>To get started you need to <strong>upload your signal data</strong>.</p>

      <p>
        <label>Analysis Name</label>
        <g:textField name="displayName" value="${wizardInstance.displayName}"/>
			</p>

			%{--<p class="last"></p>--}%
			<p><label>Signal Data:</label>
        <input type="file" name="signalDataFile" onchange="updateDisplayName();"/>
				<a id="annotation-options" class="btn expand-btn small right" href="javascript:toggleSection('annotationOptions');">Annotation options</a>
			</p>

			<p>
				<label for=fileType>File type:</label>
                <select name="fileType" id="fileType"></select>
			  <a href="${resource( file: 'supported.gsp')}" target="_blank" class="small">Supported file formats</a>
      </p>

      <p id="manufacturerDiv" style="display:none;">
        <label for="manufacturer">
          Manufacturer
        </label>
        <select name="manufacturer" id="manufacturer">
        </select>
      </p>

      <p id="chipTypeDiv" style="display:none;">
        <label for="chipType">
          Chip name
        </label>
        <select name="chipType" id="chipType">
        </select>
      </p>

			<div id="annotationOptions" style="display:none;">

				<p>
					<label>Annotation info:</label>
					<g:select name="annotationInfo" from="${wizardInstance.constraints.annotationInfo.inList}" />
				</p>

				<p>
					<label></label> <input type="file" name="annotationFile"/>
				</p>
			</div>
	<!--		<a id="helpTab" class="mathelp" href="javascript:toggleSection('helpText', 'helpTab');">help</a>  -->

			<div class="prevnext">
				<g:submitButton name="Next" value="Next" class="btn small primary next" onclick="javascript:showProgress();"/>
			</div>
		</div>
	</g:uploadForm>
	<div id="helpText" class="mat-helptext" style="display: none;">
		Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam vestibulum, quam ac imperdiet sagittis, orci ante imperdiet nisi, id fringilla lectus odio eget nisi. Sed dictum risus quis metus mollis vitae pretium ligula egestas. Fusce tristique nisl a nunc fermentum tincidunt facilisis est condimentum. Phasellus facilisis massa sed dolor congue porta. Proin dolor nisl, pellentesque et tincidunt non, congue at mi. Cras tempor nulla eget arcu eleifend pulvinar. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Fusce vel sollicitudin sapien. Nam augue enim, porta ac congue vitae, tincidunt sed leo. Donec tincidunt dignissim dolor dignissim laoreet. Nulla at velit neque. Vivamus sem velit, pellentesque vel varius at, aliquam sit amet enim. Nulla sed quam magna.
	</div>
</div>
	<g:javascript>

      function getBase() {
  	      var curPath = location.pathname;
  	      return curPath.substring(0, curPath.indexOf("/",1));
      }

      (function( )
      {
          var urlBase = getBase(),
              fileTypes = [],
              fileTypeName = "",
              chipTypes = [],
              chipTypeId = -1,
              manufacturers = [];
          
          function init( )
          {
              hideManufacturers( );
              hideChipTypes( );
              queryFileTypes( );
          }


          function addOption( select, txt, val )
          {
              var opt = $("<option/>");
              if ( val === null )
                  val = txt;
              opt.text( txt ).attr( "value", val );
              opt.appendTo( select );
          }

          
          function findFileTypeByName( name )
          {
              var grepRslt = $.grep( fileTypes,
                                   function( fileType, i )
                                     {
                                         return (fileType.name == name);
                                     } );
              if ( grepRslt.length > 0 )
                  return grepRslt[ 0 ];
              return null;
          }

          function findChipTypesByManufacturer( manufacturer )
          {
              return $.grep( chipTypes,
                             function( chipType, i )
                             {
                                 return (chipType.manufacturer == manufacturer);
                             } );
          }
          
          function findChipTypeById( id )
          {
              var grepRslt = $.grep( chipTypes,
                                   function( chipType, i )
                                     {
                                         return (chipType.id == id);
                                     } );
              if ( grepRslt.length > 0 )
                  return grepRslt[ 0 ];
              return null;
          }


          function queryFileTypes( )
          {
              if ( fileTypes.length > 0 )
                  return;
              $.getJSON(
                  urlBase + "/chipsLoaded/listFileTypes",
                  { },
                  processFileTypes
              );
          }

          function processFileTypes( ajaxResponse )
          {
              fileTypes = ajaxResponse;
              displayFileTypes( );
          }

          function displayFileTypes( )
          {
              var sel = $("#fileType");
              sel.empty( );
              addOption( sel, "Select a file type", "" );
              for ( var i = 0, max = fileTypes.length; i < max; ++i )
              {
                  var fileType = fileTypes[ i ];
                  addOption( sel, fileType.name );
              }
              sel.change( handleFileTypeSelection );
          }

          
          function queryChipTypes( )
          {
              if ( chipTypes.length > 0 )
                  return;
              $.getJSON(
                  urlBase + "/chipsLoaded/listChipTypes",
                  {
                      forMat: true
                  },
                  processChipTypes
              );
          }
          
          function processChipTypes( ajaxResponse )
          {
              chipTypes = ajaxResponse;
              getManufacturers( );
              displayManufacturers( );
              displayChipTypes( );
          }

          function getManufacturers( )
          {
              var i, max,
                  manufacturer;
              manufacturers = [];
              for ( i = 0, max = chipTypes.length; i < max; ++i )
              {
                  manufacturer = chipTypes[ i ].manufacturer;
                  if ( $.inArray( manufacturer, manufacturers ) < 0 )
                  {
                      manufacturers.push( manufacturer );
                  }
              }
              manufacturers.sort( );
          }
          
          function displayManufacturers( )
          {
              var sel = $("#manufacturer");
              sel.empty( );
              addOption( sel, "Select a manufacturer", "" );
              for ( var i = 0, max = manufacturers.length; i < max; ++i )
              {
                  var manufacturer = manufacturers[ i ];
                  addOption( sel, manufacturer );
              }
              $("#manufacturerDiv").show( );
              sel.change( handleManufacturerSelection );
          }
          
          function displayChipTypes( manufacturer )
          {
              var sel = $("#chipType");
              sel.empty( );
              addOption( sel, "Select a platform", -1 );
              for ( var i = 0, max = chipTypes.length; i < max; ++i )
              {
                  var chipType = chipTypes[ i ];
                  if ( (! manufacturer) ||
                       (chipType.manufacturer === manufacturer) )
                  {
                      addOption( sel, chipType.name, chipType.id );
                  }
              }
              $("#chipTypeDiv").show( );
              sel.change( handleChipTypeSelection );
          }

          function showManufacturers( )
          {
              if ( chipTypes.length === 0 )
              {
                  queryChipTypes( );
              }
              else
              {
                  $("#manufacturerDiv").show( );
              }
          }

          function hideManufacturers( )
          {
              $("#manufacturerDiv").hide( );
          }
          
          function showChipTypes( )
          {
              $("#chipTypeDiv").show( );
          }

          function hideChipTypes( )
          {
              $("#chipTypeDiv").hide( );
          }


          function handleFileTypeSelection( )
          {
              var fileType;
              fileTypeName = $("#fileType").val();
              if ( fileTypeName === "" )
              {
                  hideManufacturers( );
                  hideChipTypes( );
              }
              else
              {
                  fileType = findFileTypeByName( fileTypeName );
                  if ( fileType.requiresChipType )
                  {
                      showManufacturers( );
                  }
                  else
                  {
                      hideManufacturers( );
                      hideChipTypes( );
                      chipTypeId = -1;
                  }
              }
          }

          function handleManufacturerSelection( )
          {
              var manufacturer = $("#manufacturer").val();
              displayChipTypes( manufacturer );
          }

          function handleChipTypeSelection( )
          {
              chipTypeId = $("#chipType").val();
          }


          
          return {
              init: init
          };
      })().init( );

    

		function toggleSection(section, sectionHeader) {
      if (!$("a#annotation-options").hasClass("disabled")) {
        var ele = document.getElementById(section);
        if (ele.style.display != "none") {
          ele.style.display = "none";
          if (arguments.length > 1) {
            removeOpen(sectionHeader);
          }
        } else {
          ele.style.display = "";
          if (arguments.length > 1) {
            addOpen(sectionHeader);
          }
        }
      }
		}

		function addOpen(sectionId) {
			$("#" + sectionId).addClass("open");
		}

		function removeOpen(sectionId) {
			$("#" + sectionId).removeClass("open");
		}

		function submitWithProgress() {

		}

		var wizardId;


    var showProgress = function() {
      if ($.trim($("input[name='signalDataFile']").val()) !== "") {
        $("div#file-progress-msg").show();
      }
    };
    var updateDisplayName = function() {
      var inputField = $("input[name='displayName']");
      var name = $.trim(inputField.val());
      if (name === "") {
        var file = $("input[name='signalDataFile']").val();
        var filename = file.substring(0,file.lastIndexOf('.'));
        inputField.val(filename);
      }
    };
  </g:javascript>
</body>
</html>
