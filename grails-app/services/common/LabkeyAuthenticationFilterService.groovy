package common

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.*

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


class LabkeyAuthenticationFilterService extends GenericFilterBean implements ApplicationEventPublisherAware {

	def authenticationManager
	def eventPublisher
	def rememberMeServices
	def springSecurityService

	def customProvider

	AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler()
	AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler()

	void afterPropertiesSet() {
		assert authenticationManager != null, 'authenticationManager must be specified'
		assert rememberMeServices != null, 'rememberMeServices must be specified'
		def providers = authenticationManager.providers
		providers.add(customProvider)
		authenticationManager.providers = providers
	}

	void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req
		HttpServletResponse response = (HttpServletResponse) res
		boolean success = true

		if (SecurityContextHolder.getContext().getAuthentication() == null) {

			def username = request.getParameter("j_username")
			def password = request.getParameter("j_password")

			Authentication auth
			Authentication upat
			if (username && password) {

				try {
					println "Creating labkey authentication token for: ${username}"
					upat = new LabkeyAppTokenAuthentication(principal: username, credentials: password)

					if (upat != null) {
						auth = authenticationManager.authenticate(upat)
						logger.debug("Authentication success: " + auth);
						success = true
						onSuccessfulAuthentication(request, response, auth)
					}
				} catch (AuthenticationException authenticationException) {
					success = false
					println "login failed"
					//response.sendRedirect("auth")
					((HttpServletResponse) response).sendRedirect("/dm3/login/auth?message=Login%20Failed");
					 //onUnsuccessfulAuthentication(request, response, authenticationException)
				} catch (Exception e) {
					println "Exception : ${e}"
					//onUnsuccessfulAuthentication(request, response, e)
				}

			}
		}
		if (success) {
			chain.doFilter(req, res)
		}
	}

	protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
		SecurityContextHolder.getContext().setAuthentication(authResult)
		rememberMeServices.onLoginSuccess(request, response, authResult)
	}

	protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Exception failed) {
		SecurityContextHolder.clearContext();
		rememberMeServices.loginFail(request, response)
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher
	}


}
