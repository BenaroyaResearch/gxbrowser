package org.sagres.project

import org.sagres.project.Deliverable;
import org.sagres.project.Project;
import org.sagres.sampleSet.SampleSet
import org.sagres.sampleSet.component.LookupList
import org.sagres.sampleSet.component.LookupListDetail
import common.SecRole
import common.SecUser
import groovy.sql.Sql
import org.springframework.dao.DataIntegrityViolationException
import grails.converters.JSON

//
// Some assumptions:
// Task types are from the LookupList with the name 'Task'
// Task resources are from the LookupList with the name 'Resource'
// 
class ProjectController {

	def springSecurityService //injected
	def tg2QueryService
	def dataSource
	def completedProject = LookupListDetail.findByName('Complete')
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
		[params: params]
    }

	def gantt() {
		[projectInstanceList: Project.list()]
	}

	def list(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		params.action="list"
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()
		def projectInstanceList  = Project.createCriteria().list(params)
		{
			and
			{
				isNull('privateRecord')
				ne('status', completedProject)
			}
		} //  or { xx and { eq('owner', currentUser) eq('privateRecord', 1) } }
		def projectInstanceCount = projectInstanceList.totalCount
        [projectInstanceList: projectInstanceList, projectInstanceTotal: projectInstanceCount, deliverableInstanceList: Deliverable.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser]
    }

	def privateList(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		params.action="privateList"
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()
		def projectInstanceList  = Project.createCriteria().list(params)
		{
			and
			{
				eq('owner', currentUser)
				isNotNull('privateRecord')
				ne('status', completedProject)
			}
		}
		def projectInstanceCount = projectInstanceList.totalCount
		render(view: 'list', model: [projectInstanceList: projectInstanceList, projectInstanceTotal: projectInstanceCount, deliverableInstanceList: Deliverable.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser])
	}

	def allList(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		params.action="allList"
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()
		def projectInstanceList  = Project.createCriteria().list(params) {  // don't test for Complete so we can uncomplete projects
			if (currentUser) {
				if (currentUser.authorities.contains(SecRole.findByAuthority('ROLE_ADMIN'))) {
					; // admin can see all
				} else {
					or
					{
						eq('owner', currentUser)  // owner (and implied private) or public project
						isNull('privateRecord')
					}
				}
			} else {
				isNull('privateRecord') // just public projects
			}
		}
		def projectInstanceCount = projectInstanceList.totalCount

		render(view: 'list', model: [projectInstanceList: projectInstanceList, projectInstanceTotal: projectInstanceCount, deliverableInstanceList: Deliverable.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser])
	}
	
    def create() {
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()
        [projectInstance: new Project(params), deliverableInstanceList: Deliverable.list(), sampleSetInstanceList: SampleSet.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser]
    }

	def tgImport() {
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()
		tg2ProjectList.each {
			it.title = "P" + it.id + " - " + it.title
		}
		[projectInstance: new Project(params), deliverableInstanceList: Deliverable.list(), sampleSetInstanceList: SampleSet.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser]
	}

    def save() {
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()
 
		params.contactDate = params.date('contactDate', 'yyyy-MM-dd')
		params.analysisDate = params.date('analysisDate', 'yyyy-MM-dd')
		params.deliveryDate = params.date('deliveryDate', 'yyyy-MM-dd')

        def projectInstance = new Project(params)

		projectInstance.deliverables = []
		params.each {
			if (it.key.startsWith("deliverables_"))
			  projectInstance.deliverables << Deliverable.get((it.key - "deliverables_") as Integer)
		}
		
        if (!projectInstance.save(flush: true)) {
            render(view: "create", model: [projectInstance: projectInstance, deliverableInstanceList: Deliverable.list(), sampleSetInstanceList: SampleSet.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'project.label', default: 'Project'), projectInstance.id])
        redirect(action: "show", id: projectInstance.id)
    }

    def show(Long id) {
		SecUser currentUser = springSecurityService.currentUser
        def projectInstance = Project.get(id)
		def tg2ProjectList = tg2QueryService.tg2ProjectList()
        if (!projectInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), id])
            redirect(action: "list")
            return
        }

        [projectInstance: projectInstance, deliverableInstanceList: Deliverable.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser]
    }

    def edit(Long id) {
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()

		//println "sample set: " + SampleSet.list()
		//println "tg2 Project List: " + tg2ProjectList
		
        def projectInstance = Project.get(id)
		
        if (!projectInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), id])
            redirect(action: "list")
            return
        }

        [projectInstance: projectInstance, deliverableInstanceList: Deliverable.list(), sampleSetInstanceList: SampleSet.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser]
    }

    def update(Long id, Long version) {
		SecUser currentUser = springSecurityService.currentUser
		def tg2ProjectList = tg2QueryService.tg2ProjectList()

        def projectInstance = Project.get(id)
        if (!projectInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), id])
            redirect(action: "list")
            return
        }


        if (version != null) {
            if (projectInstance.version > version) {
                projectInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'project.label', default: 'Project')] as Object[],
                          "Another user has updated this Project while you were editing")
                render(view: "edit", model: [projectInstance: projectInstance])
                return
            }
        }
		params.contactDate = params.date('contactDate', 'yyyy-MM-dd')
		params.analysisDate = params.date('analysisDate', 'yyyy-MM-dd')
		params.deliveryDate = params.date('deliveryDate', 'yyyy-MM-dd')
		projectInstance.deliverables = []
		params.each {
			if (it.key.startsWith("deliverables_"))
			  projectInstance.deliverables << Deliverable.get((it.key - "deliverables_") as Integer)
		}

		projectInstance.properties = params

		if (projectInstance.isDirty('status')) {
			params.statusDate = new Date();
			projectInstance.properties = params
		}
		
        if (!projectInstance.save(flush: true)) {
            render(view: "edit", model: [projectInstance: projectInstance, deliverableInstanceList: Deliverable.list(), sampleSetInstanceList: SampleSet.list(), tg2ProjectList: tg2ProjectList, currentUser: currentUser])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'project.label', default: 'Project'), projectInstance.name])
        redirect(action: "show", id: projectInstance.id)
    }

	def tgImportSave() {
		def tg2id = params.long('tg2id')
		def tg2ProjectList = tg2QueryService.tg2ProjectSamplesList()
		
		SecUser importUser = springSecurityService.currentUser ?: SecUser.findByUsername('vgersuk')
		
		def tg2Project = tg2ProjectList.find({it.id == tg2id})
		
		if (tg2Project)
		{
//			def thisProject = Project.find({tg2id == tg2Project.id})
//			if (!thisProject)
//			{
				Map dmProject = [:]
				dmProject.put('name',  "P" + tg2Project.id)
				dmProject.put('title', 		 tg2Project.title)
				dmProject.put('hypothesis',	 tg2Project.hypothesis)
				dmProject.put('design', 	 tg2Project.design)
				dmProject.put('purpose', 	 tg2Project.purpose)
				dmProject.put('status', 	 LookupListDetail.findByName('Analysis In Progress'))
				dmProject.put('technology',	 LookupListDetail.findByName('Microarray'))
				dmProject.put('contactDate', tg2Project.date)
				dmProject.put('analyst',	 importUser)
				dmProject.put('owner', 		 importUser) // Vivian/
				dmProject.put('organism', 	 LookupListDetail.findByName('Homo sapiens'))
				dmProject.put('tg2id', 		 tg2Project.id)
				dmProject.put('statusDate',	 new Date())
				if (tg2Project.nSamples > 0) {
					dmProject.put('sampleCount', tg2Project.nSamples)
				}
				//dmProject.put('comments',	 'Inserted automatically')

				def projectInstance = new Project()
				projectInstance.properties = dmProject

				//println "New project: " + projectInstance.properties

				if (!projectInstance.save(flush: true)) {
					projectInstance.errors.each { error ->
						println "Project import error:" + error
					}
					return
				}
				flash.message = message(code: 'default.imported.message', args: ["TG2 Project", tg2Project.title])
				redirect(action: "edit", id: projectInstance.id)
				
//			}
//			else
//			{
//				flash.message = "Project for TG2: P" + tg2id + " already exists as: <a href='/dm3/project/show/${thisProject.id}'>${thisProject.title}</a>"
//				redirect(action: "tgImport")
//			}
		}
		else
		{
			flash.message = "That is odd, I cannot find a TG2 project called P:" + tg2id
			redirect(action: "tgImport")
		}
	}
	
	def taskShift() {
//		println "task shift: " + params
		def project = Project.findById(params.long('pid'))
//		println "project is: " + project.name + " task is: " + params.tid
		def operand = params.int('amount') * (params.units == 'Days' ? 1 : 7) * (params.direction == 'Forward' ? 1 : -1)
		boolean extend = params.type == 'Extend'
		
		if (project.tasks && params.long('tid') > 0)
		{
			def firstShift = false;
			def doShift = false; 
			project.tasks.sort { t1, t2 -> t1.beginDate <=> t2.beginDate }*.each { t ->
//				println "looking at task: " + t.toString() + " number: " + t.id
				if (t.id == params.long('tid')) {
//					println "found the starting task"
					firstShift = true;
					doShift = true;
				}

				if (doShift) {
					if (firstShift) {
						if (!extend) { // If we're extending we just change the endDate.
							t.beginDate = t.beginDate.plus(operand);
						}
						firstShift = false;
					} else {
						t.beginDate = t.beginDate.plus(operand);
					}
					t.endDate = t.endDate.plus(operand);
					if (params.adjust) {
						if (t.endDate > project.deliveryDate)
							project.deliveryDate = t.endDate
						if (t.beginDate < project.contactDate)
							project.contactDate = t.beginDate
					}
					
//					println "new begin date:" + t.beginDate
					if (!t.save(flush: true)) {
						t.errors.each { error ->
							println "task shift error: " + error
						}
					}
				}
			}
			if (doShift) {
				if (!project.save(flush: true)) {
					project.errors.each { error ->
						println "project shift error: " + error
					}
				}
			}
		}

		render {} as JSON
	}

	def getGanttData() {
		SecUser currentUser = springSecurityService.currentUser
		HashMap ganttDataMap = new HashMap(); 
		List<Project> projectList
		List<Task> taskList
		List<LookupListDetail> resourceList

		String ProcessingStage1 = "Sample Processing"
		String ProcessingStage2 = "Data Analysis"
		
		//Date mminDate = params.date('startDate')
		//Date mmaxDate = params.date('endDate')

		if (params.chartType == "resource") {
			//println "public resource request"
			ganttDataMap.name = params.title ?: "Resource Gantt Chart"
//			ganttDataMap.startDate = params.date('startDate', 'yyyy-MM-dd')
//			ganttDataMap.endDate   = params.date('endDate', 'yyyy-MM-dd')

			ganttDataMap.startDate = params.startDate
			ganttDataMap.endDate   = params.endDate

			taskList = Task.list()
			resourceList = LookupListDetail.findAllByLookupList(LookupList.findByName('Resource'))
			//println "Resources: " + resourceList
			
			def rList = []
			resourceList.sort { r1, r2 -> r1.name <=> r2.name }*.each { r ->
				def rMap =[:]
				def tList = []
				Date minDate = null //ganttDataMap.startDate 
				Date maxDate = null // ganttDataMap.endDate
				rMap.putAt('name', r.name)
				
				taskList.sort { t1, t2 -> t1.beginDate <=> t2.beginDate }*.each { t ->
					//println "in resource: " + r + " task: " + t.name + " resource: " + t.resource
					if (t.resource?.id == r.id && t.project.status != completedProject) {
						//println "found a task: " + t.name + " with resource: " + r.name

						def pMap = [:]
						pMap.put('name', (t.project.name + " - " + t.project.title))
						pMap.put('color', t.project.color)
						//pMap.put('href', createLink(controller: 'project', action: 'show', id: t.project.id))
						pMap.put('href', createLink(controller: 'task', action: 'show', id: t.id))
						pMap.put('startDate', t.beginDate.format('yyyy-MM-dd'))
						pMap.put('endDate',  t.endDate.format('yyyy-MM-dd'))

						if (!minDate) minDate = t.beginDate
						if (!maxDate) maxDate = t.endDate
						
						minDate = minDate < t.beginDate ? minDate : t.beginDate
						maxDate = maxDate > t.endDate ? maxDate : t.endDate
						tList.push(pMap)
					}
				}
				if (minDate) { 
					rMap.put('startDate', minDate.format('yyyy-MM-dd'))
				}
				if (maxDate) {
					rMap.put('endDate', maxDate.format('yyyy-MM-dd'))
				}
				if (tList.size > 0) {
					rMap.put('tasks', tList)
					if ((rMap.startDate > ganttDataMap.startDate && rMap.startDate < ganttDataMap.endDate) ||
						(rMap.endDate > ganttDataMap.startDate && rMap.startDate < ganttDataMap.endDate)) {
						rList.push(rMap)
					}
				}
				ganttDataMap.put('phases', rList)
			}
		}
		else
		{
		
			ganttDataMap.name = params.title ?: "Project Gantt Chart"
//			ganttDataMap.startDate = params.date('startDate', 'yyyy-MM-dd')
//			ganttDataMap.endDate   = params.date('endDate', 'yyyy-MM-dd')

			ganttDataMap.startDate = params.startDate
			ganttDataMap.endDate   = params.endDate

			// Just one project
			if (params.id) {
				//println "single project request"
				projectList = [ Project.findById(params.long('id')) ]
			// private
			} else if (currentUser && params.chartType == "private") {
				//println "private project request"
				projectList = Project.createCriteria().list() { and { isNotNull('privateRecord') eq('owner', currentUser) ne('status', completedProject)} }
//			} else if (currentUser){
//				//projectList = Project.createCriteria().list() { or { isNull('privateRecord') and { eq('owner', currentUser) isNotNull('privateRecord') } } }
//				projectList = Project.createCriteria().list() { isNull('privateRecord') }
			// Admin
//			} else if (currentUser?.authorities?.authority?.contains('ROLE_ADMIN')) {
//				println "admin project request"
//				projectList = Project.list()
			// public
			} else {
				//println "public project request"
				projectList = Project.createCriteria().list() { and { isNull('privateRecord') ne('status', completedProject) } } //  or { xx and { eq('owner', currentUser) eq('privateRecord', 1) } }
			}

	//		ganttDataMap.phases = projectList.collect { p ->
	//			return [name: (p.name + " - " + p.title), startDate: p.contactDate.format('yyyy-MM-dd'), endDate: p.deliveryDate.format('yyyy-MM-dd'),
	//					tasks: [ [name: 'Sample Gathering', startDate: p.contactDate.format('yyyy-MM-dd'), endDate: p.analysisDate.format('yyyy-MM-dd')],
	//							 [ name: 'Sample Processing', startDate: p.analysisDate.format('yyyy-MM-dd'), endDate: p.deliveryDate.format('yyyy-MM-dd')]
	//						]
	//				 ]
	//		}
			def pList = []
			projectList.each { p->
				def pMap = [:]
				def tList = []

				Date minDate = null //ganttDataMap.startDate
				Date maxDate = null // ganttDataMap.endDate

				pMap.put('name', (p.name + " - " + p.title))
				pMap.put('href', createLink(controller: 'project', action: 'show', id: p.id))
				pMap.put('startDate', p.contactDate.format('yyyy-MM-dd')) // Project begin, required
				pMap.put('endDate',   p.deliveryDate.format('yyyy-MM-dd')) // Project end, required
				if (p.tasks)
				{
					p.tasks.sort { t1, t2 -> t1.beginDate <=> t2.beginDate }*.each { t ->
						if (t.beginDate && t.endDate) {
							def tMap = [:]
							tMap.put('href', createLink(controller: 'task', action: 'show', id: t.id))
							tMap.put('name', t.toString())
							tMap.put('color', p.color)
							tMap.put('startDate', t.beginDate.format('yyyy-MM-dd'))
							tMap.put('endDate',   t.endDate.format('yyyy-MM-dd'))
							tList.push(tMap)
							
							if (!minDate) minDate = t.beginDate
							if (!maxDate) maxDate = t.endDate
							
							minDate = minDate < t.beginDate ? minDate : t.beginDate
							maxDate = maxDate > t.endDate ? maxDate : t.endDate
						}
					}
					if (!p.contactDate) {
						pMap.put('startDate', minDate.format('yyyy-MM-dd'))
					}
					if (!p.deliveryDate) {
						pMap.put('endDate', maxDate.format('yyyy-MM-dd'))
					}
				}
				// Old way
				else if (p.contactDate && p.analysisDate && p.deliveryDate)
				{
					def gMap = [:]
					gMap.put('name', ProcessingStage1)
					gMap.put('color', p.color)
					gMap.put('startDate', p.contactDate.format('yyyy-MM-dd'))
					gMap.put('endDate',   p.analysisDate.format('yyyy-MM-dd'))
					tList.push(gMap)
					
					def rMap = [:]
					rMap.put('name', ProcessingStage2)
					rMap.put('color', p.color)
					rMap.put('startDate', p.analysisDate.format('yyyy-MM-dd'))
					rMap.put('endDate',   p.deliveryDate.format('yyyy-MM-dd'))
					tList.push(rMap)
				}
				else if (p.contactDate && p.deliveryDate)
				{
				
					def rMap = [:]
					rMap.put('name', ProcessingStage1)
					rMap.put('color', p.color)
					rMap.put('startDate', p.contactDate.format('yyyy-MM-dd'))
					rMap.put('endDate',   p.deliveryDate.format('yyyy-MM-dd'))
					tList.push(rMap)
				}
				
				pMap.put('tasks', tList)
				if ((pMap.startDate > ganttDataMap.startDate && pMap.startDate < ganttDataMap.endDate) ||
					(pMap.endDate > ganttDataMap.startDate && pMap.startDate < ganttDataMap.endDate)) {
					pList.push(pMap)
				}
				ganttDataMap.put('phases', pList)
			}
		}
		render ganttDataMap as JSON
	}
	
    def delete(Long id) {
        def projectInstance = Project.get(id)
        if (!projectInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), id])
            redirect(action: "list")
            return
        }

        try {
            projectInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'project.label', default: 'Project'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'project.label', default: 'Project'), id])
            redirect(action: "show", id: id)
        }
    }
}
