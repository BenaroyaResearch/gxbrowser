// Place your Spring DSL code here
import common.LabkeyAuthenticationFilterService
import common.LabkeyAuthenticationProviderService
beans = {
	labkeyAuthenticationFilter(LabkeyAuthenticationFilterService) {
		authenticationManager = ref("authenticationManager")
		rememberMeServices = ref("rememberMeServices")
		springSecurityService = ref("springSecurityService")
		customProvider = ref("labkeyAuthenticationProvider")
	}

	labkeyAuthenticationProvider(LabkeyAuthenticationProviderService) {
		userDetailsService = ref("userDetailsService")
		sessionFactory = ref("sessionFactory")
	}
}
