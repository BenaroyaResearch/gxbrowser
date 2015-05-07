package org.sagres.mat

//import grails.test.mixin.*
//import org.junit.*
import org.sagres.mat.ModuleSet
import org.sagres.mat.ModuleSetController

@TestFor(ModuleSetController)
class ModuleSetControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/moduleSet/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.moduleSetInstanceList.size() == 0
        assert model.moduleSetInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.moduleSetInstance != null
    }

    void testSave() {
        controller.save()

        assert model.moduleSetInstance != null
        assert view == '/moduleSet/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/moduleSet/show/1'
        assert controller.flash.message != null
        assert ModuleSet.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/moduleSet/list'

        populateValidParams(params)
        def moduleSet = new ModuleSet(params)

        assert moduleSet.save() != null

        params.id = moduleSet.id

        def model = controller.show()

        assert model.moduleSetInstance == moduleSet
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/moduleSet/list'

        populateValidParams(params)
        def moduleSet = new ModuleSet(params)

        assert moduleSet.save() != null

        params.id = moduleSet.id

        def model = controller.edit()

        assert model.moduleSetInstance == moduleSet
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/moduleSet/list'

        response.reset()

        populateValidParams(params)
        def moduleSet = new ModuleSet(params)

        assert moduleSet.save() != null

        // test invalid parameters in update
        params.id = moduleSet.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/moduleSet/edit"
        assert model.moduleSetInstance != null

        moduleSet.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/moduleSet/show/$moduleSet.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        moduleSet.clearErrors()

        populateValidParams(params)
        params.id = moduleSet.id
        params.version = -1
        controller.update()

        assert view == "/moduleSet/edit"
        assert model.moduleSetInstance != null
        assert model.moduleSetInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/moduleSet/list'

        response.reset()

        populateValidParams(params)
        def moduleSet = new ModuleSet(params)

        assert moduleSet.save() != null
        assert ModuleSet.count() == 1

        params.id = moduleSet.id

        controller.delete()

        assert ModuleSet.count() == 0
        assert ModuleSet.get(moduleSet.id) == null
        assert response.redirectedUrl == '/moduleSet/list'
    }
}
