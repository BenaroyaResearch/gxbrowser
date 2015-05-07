package common

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * Created by IntelliJ IDEA.
 * User: bzeitner
 * Date: 2/15/12
 * Time: 3:56 PM
 * 
 */
class LabkeyAppTokenAuthentication implements Authentication{
	String name
	java.util.Collection<GrantedAuthority> authorities

	String getName() {
		return name
	}

	void setName(String name) {
		this.name = name
	}

	java.util.Collection<GrantedAuthority> getAuthorities() {
		return authorities
	}

	void setAuthorities(java.util.Collection<GrantedAuthority> authorities) {
		this.authorities = authorities
	}

	Object getCredentials() {
		return credentials
	}

	void setCredentials(Object credentials) {
		this.credentials = credentials
	}

	Object getDetails() {
		return details
	}

	void setDetails(Object details) {
		this.details = details
	}

	Object getPrincipal() {
		return principal
	}

	void setPrincipal(Object principal) {
		this.principal = principal
	}

	boolean isAuthenticated() {
		return authenticated
	}

	void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated
	}


	//TODO make this

	Object credentials
 	Object details
 	Object principal
 	boolean authenticated
	Object labkeySessionId
	List labkeySetsViewable = []

}
