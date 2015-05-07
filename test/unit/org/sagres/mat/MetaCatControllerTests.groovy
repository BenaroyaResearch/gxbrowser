package org.sagres.mat

//import org.junit.*
//import grails.test.mixin.*
import org.sagres.mat.MetaCat
import org.sagres.mat.MetaCatController

@TestFor(MetaCatController)
@Mock(MetaCat)
class MetaCatControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/metaCat/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.metaCatInstanceList.size() == 0
        assert model.metaCatInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.metaCatInstance != null
    }

    void testSave() {
        controller.save()

        assert model.metaCatInstance != null
        assert view == '/metaCat/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/metaCat/show/1'
        assert controller.flash.message != null
        assert MetaCat.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/metaCat/list'

        populateValidParams(params)
        def metaCat = new MetaCat(params)

        assert metaCat.save() != null

        params.id = metaCat.id

        def model = controller.show()

        assert model.metaCatInstance == metaCat
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/metaCat/list'

        populateValidParams(params)
        def metaCat = new MetaCat(params)

        assert metaCat.save() != null

        params.id = metaCat.id

        def model = controller.edit()

        assert model.metaCatInstance == metaCat
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/metaCat/list'

        response.reset()

        populateValidParams(params)
        def metaCat = new MetaCat(params)

        assert metaCat.save() != null

        // test invalid parameters in update
        params.id = metaCat.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/metaCat/edit"
        assert model.metaCatInstance != null

        metaCat.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/metaCat/show/$metaCat.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        metaCat.clearErrors()

        populateValidParams(params)
        params.id = metaCat.id
        params.version = -1
        controller.update()

        assert view == "/metaCat/edit"
        assert model.metaCatInstance != null
        assert model.metaCatInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/metaCat/list'

        response.reset()

        populateValidParams(params)
        def metaCat = new MetaCat(params)

        assert metaCat.save() != null
        assert MetaCat.count() == 1

        params.id = metaCat.id

        controller.delete()

        assert MetaCat.count() == 0
        assert MetaCat.get(metaCat.id) == null
        assert response.redirectedUrl == '/metaCat/list'
    }
}
