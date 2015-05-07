package org.sagres.sampleSet

import java.awt.Toolkit
import javax.swing.ImageIcon
import org.apache.batik.dom.svg.SAXSVGDocumentFactory
import org.apache.batik.dom.svg.SVGDOMImplementation
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.sagres.util.ImageSelection
import org.w3c.dom.Document

class ChartToImageService {

  def servletContext

  def byte[] getClipboardImage(def img, String type)
  {
    byte[] image
    if (type == "img")
    {
      image = img.substring(img.indexOf(',')+1).decodeBase64()
    }
    else if (type == "svg")
    {
      String parser = XMLResourceDescriptor.getXMLParserClassName();
      SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
      String svgNs = SVGDOMImplementation.SVG_NAMESPACE_URI
      Document document = f.createDocument(svgNs, 'svg', null, new ByteArrayInputStream(((String)img).getBytes()))

      PNGTranscoder t = new PNGTranscoder();
      TranscoderInput input = new TranscoderInput(document);

      ByteArrayOutputStream ostream = new ByteArrayOutputStream()
      TranscoderOutput output = new TranscoderOutput(ostream);
      t.transcode(input, output);
      ostream.flush();

      image = ostream.toByteArray()
    }
    return image
  }

  /**
   * Copy an image to clipboard
   * @param img The image bytes
   */
  def copyToClipboard(img)
  {
    if (img)
    {
      byte[] imageBytes = img.substring(img.indexOf(',')+1).decodeBase64()
      toClipboard(imageBytes)
    }
  }

  def copySVGToClipboard(svg)
  {
    def id = saveSVG(svg)

    def tempFile = new File(getWebRootDir(), "temp${id}.png")
    if (tempFile.exists()) {
      byte[] imageBytes = tempFile.bytes
      toClipboard(imageBytes)
      tempFile.delete()
    }
  }

  private def toClipboard(byte[] imageBytes)
  {
    def image = new ImageIcon(imageBytes).getImage()
    def value = new ImageSelection(image)
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(value, null)
  }

  int saveSVG(svg)
  {
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
    String svgNs = SVGDOMImplementation.SVG_NAMESPACE_URI
    Document document = f.createSVGDocument(null, new ByteArrayInputStream(((String)svg).getBytes("UTF8")))
    //Document(svgNs, 'svg', null, new ByteArrayInputStream(((String)svg).getBytes()))

    // fix for SVG preserveAspectRatio bug
    def svgNode = document.getFirstChild()
    if (svgNode.hasAttributes()) {
      def preserveAspectRatio = svgNode.getAttributes().getNamedItem("preserveAspectRatio")
      if (preserveAspectRatio.getNodeValue() == "meet") {
        preserveAspectRatio.setNodeValue("xMinYMin meet")
      }
    }

    def id = new Random().nextInt(100000)

    PNGTranscoder t = new PNGTranscoder();
    TranscoderInput input = new TranscoderInput(document);
    def file = new File(getWebRootDir(), "temp${id}.png")
    OutputStream ostream = new FileOutputStream(file)
    TranscoderOutput output = new TranscoderOutput(ostream);
    t.transcode(input, output);
    ostream.flush();
    ostream.close();

    return id
  }

  /**
   * Save the image as a temporary file
   * @param img The image bytes
   * @return id The temporary file id to be used for downloadImg()
   */
  int saveImg(img)
  {
    def id = new Random().nextInt(100000)

    // upload spreadsheet file
    def file = new File(getWebRootDir(), "temp${id}.png")
    FileOutputStream fos = new FileOutputStream(file)
    fos.write(img.substring(img.indexOf(',')+1).decodeBase64())
    fos.close()

    return id
  }

  /**
   * Down the temporary image file
   * @param id The temporary file id from saveImg()
   */
  def downloadImg(String id, response, String filename)
  {
    if (id)
    {
      def file = new File(getWebRootDir(), "temp${id}.png")
      def name = filename ? "${filename.replaceAll("\\s+","_")}.png" : file.name
      if (file.exists()) {
        response.setContentType("application/octet-stream")
        response.setHeader("Content-Disposition", "attachment; filename=${name}")
        response.outputStream << file.bytes
        file.delete()
      }
    }
  }

  def deleteTempImg(String id)
  {
    if (id)
    {
      def file = new File(getWebRootDir(), "temp${id}.png")
      if (file.exists()) {
        file.delete()
      }
    }
  }

  private def File getWebRootDir()
  {
    def webRootDir = servletContext.getRealPath("/")
    def dir = new File(webRootDir, "/tempImages")
    dir.mkdirs()
    return dir
  }
}
