class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

//		"/"        (view: "/landing")
		"/"		   (controller: "landing")
		"/mgmt"    (view:"/index")
		"500"      (view:'/error')
		"404"      (view:'/404')
		
		"/faq.gsp"       (view:'/faq')
		"/moduleDesc"    (view:'/moduleDesc')
		"/projectmore"    (view:'/projectmore')
        "/supported.gsp" (view:'/supported')
		"/gxbmore.gsp"   (view:'/gxbmore')
		"/gxbNews.gsp"   (view:'/gxbNews')
		"/matmore.gsp"   (view:'/matmore')
		"/matNews.gsp"   (view:'/matNews')
		"/tutorials.gsp" (view:'/tutorials')
		"/landing.gsp"   (view:'/landing')

		"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")
		"/timeout"(view: '/timeout')
        "/faq"(view: '/faq')

	}
}
