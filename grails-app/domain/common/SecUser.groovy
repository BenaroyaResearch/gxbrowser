package common

class SecUser {

	transient springSecurityService

  String lastVisitedPage
	String username
	String password
  String email
	int i_enabled = 1
	int i_accountExpired = 0
	int i_accountLocked = 0
	int i_passwordExpired = 0
	Boolean enabled = true
	Boolean accountExpired = false
	Boolean accountLocked = false
	Boolean passwordExpired = false
	
	static constraints = {
		username blank: false, unique: true
		password blank: false
    email blank: false, unique: true
    lastVisitedPage nullable: true
	}

	static mapping = {
		password column: '`password`'
	}

  static transients = ['enabled', 'accountExpired', 'accountLocked', 'passwordExpired']

  void setEnabled(boolean enabled) {
    i_enabled = enabled ? 1 : 0
  }

  void setAccountExpired(boolean expired) {
    i_accountExpired = expired ? 1 : 0
  }

  void setAccountLocked(boolean locked) {
    i_accountLocked = locked ? 1 : 0
  }

  void setPasswordExpired(boolean locked) {
    i_passwordExpired = locked ? 1 : 0
  }

  boolean getEnabled() {
    return (i_enabled == 1 ? true : false)
  }

  boolean getAccountExpired() {
    return (i_accountExpired == 1 ? true : false)
  }

  boolean getAccountLocked() {
    return (i_accountLocked == 1 ? true : false)
  }

  boolean getPasswordExpired() {
    return (i_passwordExpired == 1 ? true : false)
  }

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}
	
	String toString() {
		return username
	}
}
