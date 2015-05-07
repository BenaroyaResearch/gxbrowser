/*
  dm.js
  Javascript applicable to whole site.
*/

//*****************************************************************************

$(document).ready( Start );

//.............................................................................

function Start()
{
	$(".Button").button( );
	
	$("div.TabSet").tabs(
        {
            fx: { opacity: 'toggle', duration: 'fast' }
        } );

	$("div.Dialog").dialog(
		{
			autoOpen: false,
			overlay: { background: '#ffffff',
					   opacity: '0.5' },
			width: 'auto'
		} );

	$("div.ModalDialog").dialog(
		{
			autoOpen: false,
			modal: true,
			overlay: { background: '#ffffff',
					   opacity: '0.5' },
			width: 'auto'
		} );

    $("input.Date").datepicker(
        {
            showOn: 'button',
            dateFormat: 'yy-mm-dd',
            appendText: ' (yyyy-mm-dd)',
            yearRange: '-10:+0',
            changeMonth: true,
            changeYear: true
        } );
    

	if ( $.browser.mozilla )
	{
        $("form").attr( "autocomplete", "off" );
	}
}

//=============================================================================

var siteUrlBase = "";

//.............................................................................

function setUrlBase( urlBase )
{
	siteUrlBase = urlBase;
}

//.............................................................................

function getUrlBase( )
{
	return siteUrlBase;
}


//*****************************************************************************
