package org.sagres

import java.security.MessageDigest
import grails.converters.JSON
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import common.SecUser
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class MiniURLController
{
	def scaffold = true
	def dataSource
	def tg2QueryService
	def mailService
	def springSecurityService
	def mongoDataService
	def grailsApplication


	def tg2ProjectListAsJson =
	{
		def jsonResults = tg2QueryService.projectListAsJson()
		render jsonResults as JSON
	}

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		def jsonResults = tg2QueryService.projectListAsJson()
		[miniURLInstanceList: MiniURL.list(params), miniURLInstanceTotal: MiniURL.count()]
	}

	private static int baseNum = 62
	private static String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
	private static String base62ToString(long fromValue)
	{
		String toValue = fromValue == 0 ? "0" : ""
		int mod = 0
		while (fromValue != 0)
		{
			mod = (int)(fromValue % baseNum)
			def ch = baseDigits.substring(mod, mod + 1)
			toValue = ch + toValue
			fromValue = fromValue / baseNum
		}
		return toValue
	}

	def create =
	{
		if (params._controller == null)
	    return

//		def url = params.controller + "/" + params.action + "/" + params.sampleSetId + "?"
		def url = "${params._controller}/${params._action}/${params._id}?"
		def args = []
		params.each { key, value ->
			if (key != "_controller" && key != "_action" && !(value instanceof GrailsParameterMap)) {
				if (value != null && value != "") {
				  args.push("${key}=${value}")
				}
			}
		}
		
		String serializedArgs = args.join("&")
		url += serializedArgs

		def digest = MessageDigest.getInstance("MD5")
		def md5hash = new BigInteger(1,digest.digest(url.getBytes())).toString(16).padLeft(32,"0")

		def jsonReturn = [:]
		def existingURL = MiniURL.findByFullURLHash(md5hash)
		def urlLink = null
		if (existingURL != null)
		{
			urlLink = existingURL.miniURL
//			jsonReturn['link'] = existingURL.miniURL
		}
		else
		{
			def newURL = new MiniURL()
			newURL.fullURL = url
			newURL.fullURLHash = md5hash
			newURL.miniURL = ""
			newURL.controller = params._controller
			newURL.action = params._action
			newURL.args = serializedArgs
			newURL.paramsID = params._id
			newURL.save()
			newURL.miniURL = base62ToString(newURL.id)
			newURL.save()

			urlLink = newURL.miniURL
//			jsonReturn['link'] = newURL.miniURL
		}

		String appName = grailsApplication.metadata['app.name']

		//def baseUrl = """http://${request.getServerName()}:${request.getServerPort()}/${appName}"""
		def baseUrl = """${grailsApplication.config.grails.serverURL}""" // includes appName
		if (System.getenv("DM3_AUTHENTICATE_VIA_LABKEY"))
		{
			baseUrl = """https://${request.getServerName()}/${appName}"""
		}
		
		String fullLink = createLink(controller:"miniURL", action:"view", id:urlLink, base: baseUrl.toString())
    	jsonReturn.link = fullLink

		render jsonReturn as JSON
	}

	def view = {
		Sql sql = Sql.newInstance(dataSource)
		def row = sql.firstRow("SELECT id FROM miniurl WHERE miniurl RLIKE BINARY '${params.id}'".toString())
		sql.close()
		def miniUrl = MiniURL.get(row.id)
		if (miniUrl)
		{
			def args = [:]
			if (miniUrl.paramsID)
			{
				args.put("id", miniUrl.paramsID)
			}
			args.put("controller",  miniUrl.controller)
			args.put("action", miniUrl.action)
			//args.put("fromMini", true)
			//println "MiniURL: '" + miniUrl.miniURL + "' contoller: '" + miniUrl.controller + "' action: '" + miniUrl.action +"'"
			miniUrl.args.split("&").each {
				def keyValue = it.split("=")
				args.put(keyValue[0],keyValue[1])
			}
			//println "miniURL view params: " + args
			redirect(controller:miniUrl.controller, action:miniUrl.action, params:args)
		}
		else
		{
			redirect(controller:"geneBrowser", action:"list")
		}
	}

	def save = {
		def miniURLInstance = new MiniURL(params)
		if (miniURLInstance.save(flush: true))
		{
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), miniURLInstance.id])}"
			redirect(action: "show", id: miniURLInstance.id)
		}
		else
		{
			render(view: "create", model: [miniURLInstance: miniURLInstance])
		}
	}

	def show = {
		def miniURLInstance = MiniURL.get(params.id)
		if (!miniURLInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			[miniURLInstance: miniURLInstance]
		}
	}

	def edit = {
		def miniURLInstance = MiniURL.get(params.id)
		if (!miniURLInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			return [miniURLInstance: miniURLInstance]
		}
	}

	def update = {
		def miniURLInstance = MiniURL.get(params.id)
		if (miniURLInstance)
		{
			if (params.version)
			{
				def version = params.version.toLong()
				if (miniURLInstance.version > version)
				{

					miniURLInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'miniURL.label', default: 'MiniURL')] as Object[], "Another user has updated this MiniURL while you were editing")
					render(view: "edit", model: [miniURLInstance: miniURLInstance])
					return
				}
			}
			miniURLInstance.properties = params
			if (!miniURLInstance.hasErrors() && miniURLInstance.save(flush: true))
			{
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), miniURLInstance.id])}"
				redirect(action: "show", id: miniURLInstance.id)
			}
			else
			{
				render(view: "edit", model: [miniURLInstance: miniURLInstance])
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def miniURLInstance = MiniURL.get(params.id)
		if (miniURLInstance)
		{
			try
			{
				miniURLInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e)
			{
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'miniURL.label', default: 'MiniURL'), params.id])}"
			redirect(action: "list")
		}
	}

  def emailLink = {
    if (grailsApplication.config.send.email.on)
    {
      String recipients = params.recipients
      def message = params.message
      def subj = params.subject
      def sender = params.sender
      def ccemail = params.ccEmail
      try
      {
        // check for multiple recipients
        String[] multiRecipients = recipients.split(",|;|\\s+")
        if (ccemail) {
          mailService.sendMail {
            to multiRecipients
            cc "${ccemail}"
            from "${sender}"
            subject "${subj}"
            body "${message}"
          }
        } else {
          mailService.sendMail {
            to multiRecipients
            from "${sender}"
            subject "${subj}"
            body "${message}"
          }
        }

        def returnMap = [message:"Your email has been sent!"]
        render returnMap as JSON
      }
      catch (Exception e)
      {
        e.printStackTrace();
        def returnMap = [message:"There was a problem sending your email.", error:true]
        render returnMap as JSON
      }
    }
    else
    {
      def returnMap = [message:"Emailing is not enabled on this system.", error:true]
      render returnMap as JSON
    }
  }

}
