package org.sagres.mat

import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob
//import org.hibernate.lob.BlobImpl

class Version {

	static constraints = {
	}

	String versionName
	Date dateCreated
	Date lastUpdated
	Blob binaryFileVersion
	Blob binaryFileFunction





	def setVersionFile(InputStream inFile, long length) {
		binaryFileVersion = new SerialBlob(inFile.bytes)
	}

	def setFunctionFile(InputStream inFile, long length) {
		binaryFileFunction = new SerialBlob(inFile.bytes)
	}

	def getVersionFileName() {
		return "${versionName}.csv"
	}

	def getFunctionFileName() {
		return "${versionName}_function.csv"
	}

	def getVersionData() {
		return binaryFileVersion?.binaryStream
	}

	def getFunctionData() {
		return binaryFileFunction?.binaryStream
	}

	static transients = ['functionSize', 'file', 'versionSize', 'versionFileName', 'functionFileName']

	Long getVersionSize() {
		return binaryFileVersion?.length() ?: 0
	}

	Long getFunctionSize() {
		return binaryFileFunction?.length() ?: 0
	}

	def render(def response) {
		response.contentType = "application/octet-stream"
	}

	def loadFunctionFile(def file) {
		if (!file) {
			return false;
		}
		this.setFunctionFile(file?.inputStream, file?.size)
	}

	static fromVersionUpload(def file) {
		if (!file) {
			return new Version()
		}
		def originalFileName = file.originalFilename
		def slashIndex = Math.max(originalFileName.lastIndexOf("/"), originalFileName.lastIndexOf("\\"))
		if (slashIndex > -1) {
			originalFileName = originalFileName.substring(slashIndex + 1)
		}
		def doc = new Version(versionName: originalFileName)
		doc.setVersionFile(file?.inputStream, file?.size)
		return doc
	}

}
