<%--
  Created by IntelliJ IDEA.
  User: charliequinn
  Date: 9/26/11
  Time: 15:19 
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head><title>Simple GSP page</title></head>
  <body>
    <p><strong>Number of Samples:</strong> ${results.noSamples}</p>
    <p><strong>Number not processed:</strong> ${results.noSamplesNotProcessed}</p>
    <g:if test="${results.noSamplesNotProcessed > 0}">
    <p><strong>Bad Barcodes</strong></p>
    <g:each in="${results.badBarcodes}" var="barcode">
	    ${barcode}<br/>
    </g:each>
	   </g:if>
  </body>
</html>