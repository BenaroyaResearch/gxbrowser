package org.sagres.mat

import grails.plugins.springsecurity.Secured
import org.springframework.security.core.GrantedAuthority
import groovy.sql.Sql
import common.chipInfo.ChipType


class VersionController {

	def springSecurityService
	def sampleSetService
	def dataSource //injected



	def getChipTypes() {

		def chipTypes = [:]
		Sql db = Sql.newInstance(dataSource)
		//select ct.id, ct.active, ct.name, ct.chip_data_id, cd.manufacturer, cd.name, cd.model from chip_type ct, chip_data cd where ct.chip_data_id = cd.id and model is not null;
		String sqlS = "select ct.id, ct.name,  " +
			"cd.manufacturer, cd.name, ct.module_version_id, ct.module_gen3mapping_id from chip_type ct, chip_data cd " +
			"where ct.chip_data_id = cd.id and model is not null and ct.active >= 0"
		db.eachRow(sqlS) {
			def parms = [chipTypeName: it.getAt(1), manufactorer: it.getAt(2), chipName: it.getAt(3), versionId: it.getAt(4), gen3Id: it.getAt(5)]
			chipTypes.put(it.getAt(0),parms)
		}
		db.close()
		return chipTypes
	}

	  def associate = {
			def chipTypes = getChipTypes()
			def versions = Version.list(params)
			return [chipTypes: chipTypes, versions: versions]
		}

	def saveAssociations = {
		params.each { key, value ->
			if (key.toString().startsWith("chip_id_") && value != null ) {
				def chipId = key.toString().substring("chip_id_".length())
				try {
					ChipType ct = ChipType.findById(chipId)
					def versionId = Long.parseLong(value)
					ct.setModuleVersionId(versionId)
					ct.save()
				}  catch (Exception ex) {
					 println "Exception finding chip Type for id ${chipId}: ${ex.toString()}"
				}
			} else if (key.toString().startsWith("g3_chip_id_") && value != null) {
				def chipId = key.toString().substring("g3_chip_id_".length())
				try {
					ChipType ct = ChipType.findById(chipId)
					def versionId = Long.parseLong(value)
					ct.setModuleGen3MappingId(versionId)
					ct.save()
				} catch (Exception ex) {
					println "Exception finding chip type for id ${chipId}: ${ex.toString()}"
				}

			}
		}
		redirect(action: "list", params: params)

	}


    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [moduleVersionFileInstanceList: Version.list(params), moduleVersionFileInstanceTotal: Version.count()]
    }

	  @Secured(['ROLE_ADMIN'])
    def create = {
        def moduleVersionFileInstance = new Version()
        moduleVersionFileInstance.properties = params
				def chipTypes = getChipTypes()
        return [moduleVersionFileInstance: moduleVersionFileInstance, chipTypes:chipTypes]
    }

	 @Secured(['ROLE_ADMIN'])
    def save = {
       // def moduleVersionFileInstance = new ModuleVersionFile(params)
			def uploadedFile = request.getFile("versionFile")
			def doc = Version.fromVersionUpload(request.getFile("versionFile"))
			doc.loadFunctionFile(request.getFile("functionFile"))
			doc.setVersionName(params.versionName)
        if (doc.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'Version.label', default: 'Version'), doc.id])}"
            redirect(action: "show", id: doc.id)
        }
        else {
            render(view: "create", model: [moduleVersionFileInstance: doc])
        }
    }

    def show = {
        def moduleVersionFileInstance = Version.get(params.id)
        if (!moduleVersionFileInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'Version.label', default: 'Version'), params.id])}"
            redirect(action: "list")
        }
        else {
            [moduleVersionFileInstance: moduleVersionFileInstance]
        }
    }

	@Secured(['ROLE_ADMIN'])
	def edit = {
		def moduleVersionFileInstance = Version.get(params.id)
		def chipTypes = getChipTypes()
		if (!moduleVersionFileInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'Version.label', default: 'Version'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [moduleVersionFileInstance: moduleVersionFileInstance, chipTypes:chipTypes]
		}
	}

	@Secured(['ROLE_ADMIN'])
    def update = {
        def moduleVersionFileInstance = Version.get(params.id)
        if (moduleVersionFileInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (moduleVersionFileInstance.version > version) {

                    moduleVersionFileInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'Version.label', default: 'Version')] as Object[], "Another user has updated this Version while you were editing")
                    render(view: "edit", model: [moduleVersionFileInstance: moduleVersionFileInstance])
                    return
                }
            }
            moduleVersionFileInstance.properties = params
            if (!moduleVersionFileInstance.hasErrors() && moduleVersionFileInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'Version.label', default: 'Version'), moduleVersionFileInstance.id])}"
                redirect(action: "show", id: moduleVersionFileInstance.id)
            }
            else {
                render(view: "edit", model: [moduleVersionFileInstance: moduleVersionFileInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'moduleVersionFile.label', default: 'ModuleVersionFile'), params.id])}"
            redirect(action: "list")
        }
    }

	  @Secured(['ROLE_ADMIN'])
    def delete = {
        def moduleVersionFileInstance = Version.get(params.id)
        if (moduleVersionFileInstance) {
            try {
                moduleVersionFileInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'Version.label', default: 'Version'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'Version.label', default: 'Version'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'Version.label', default: 'Version'), params.id])}"
            redirect(action: "list")
        }
    }
}
