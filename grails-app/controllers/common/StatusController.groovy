package common

import groovy.sql.Sql
import org.sagres.util.mongo.MongoConnector
import org.sagres.mat.MatConfigService

class StatusController {

	def dataSource
	def matConfigService

	 def index = {
    redirect(action: "show", params: params)
  }

	def show = {
		Integer mysqlStatus = 1
		Integer mongoStatus = 1
		Integer applicationStatus = 0 //TODO determine something to determine application status
		Integer fileSystemStatus = 1
		Sql sql
		try {
			sql = Sql.newInstance(dataSource)
			sql.eachRow("Show table status") { row ->
				mysqlStatus = 0
			}
		} catch (Exception ex) {

		} finally {
			sql.close()
		}

		try {
			MongoConnector mc = MongoConnector.getInstance()
			mongoStatus = 0
            mc = null
		} catch (Exception ex) {
		} catch (Error er) {

		}

		try {
			def MATPath =  matConfigService.getMATWorkDirectory()
			File f = new File(MATPath)
			double totalSpace = f.getTotalSpace()
			double useableSpace = f.getUsableSpace()
			double spaceFreeRatio = useableSpace/totalSpace
			//TODO Determine acceptable ratios
			//TODO Include where app archives files - perhaps a list of all partitions?
			if (spaceFreeRatio >= 0.2) {
				fileSystemStatus = 0
			} else if (spaceFreeRatio >= 0.1) {
				fileSystemStatus = 2
			} else {
				fileSystemStatus = 1
			}
		} catch (Exception ex) {
			println "exception checking file system: ${ex.toString()}"
		}

	 return [mysqlStatus: mysqlStatus, mongoStatus: mongoStatus, applicationStatus: applicationStatus, fileSystemStatus : fileSystemStatus]

	}
}
