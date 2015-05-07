package common

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.encoding.PasswordEncoder

class SecUserController {

  static scaffold = SecUser

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def springSecurityService
  def mailService

  def index = {
    redirect(action: "list", params: params)
  }

  def cancel = {
    redirect(action: "list")
  }

  def list = {
    return [secUserInstanceList: SecUser.list()]
  }

  def create = {
    [secUserInstance: new SecUser(params)]
  }

  def save = {
    def user = new SecUser(params)
    if (!user.save(flush: true)) {
      render(view: "create", model: [secUserInstance: user])
      return
    } else {
      params.roles.each {
        def secRole = SecRole.get(Long.parseLong(it))
        new SecUserSecRole(secUser: user, secRole: secRole).save()
      }
    }

    // send email to user
    String keyMashup = "${user.username}${user.email}${grailsApplication.config.salt}"
    String hash = keyMashup.encodeAsMD5()

    if (grailsApplication.config.send.email.on)
    {
      try
      {
        // send email with hash key
        def link = "${grailsApplication.config.grails.serverURL}/secUser/resetPassword?resetuser=${user.username}&resetkey=${hash}"
        def message = """<p>Hi ${user.username},</p>
            <p>A new account has been created for you.</p>
            <p>Your username is: ${user.username}</p>
            <p>Your initial password is: ${params.password}</p>
            <p>If you would like to set a password, please click the link below:</p>
            <p><a href="${link}">${link}</a></p>
            <p>We recommend using a browser such as Chrome, Firefox or Safari to use our tools. Please contact IT Helpdesk for help with installing these software applications.</p>
            <p>If you believe that you have received this in error, please contact us immediately.</p>
            <p>Thanks!</p>
            <p>The BioIT Team</p>""".toString()
        mailService.sendMail {
          to "${user.email}"
          from "${grailsApplication.config.importer.defaultEMailFrom}"
          subject "Sample Set Annotation Tool: New Account"
          html "${message}"
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    flash.message = "New user created successfully"
    redirect(action: "show", id: user.id)
  }

  def show = {
    def secUserInstance = SecUser.get(params.id)
    if (!secUserInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'secUser.label', default: 'SecUser'), params.id])
      redirect(action: "list")
      return
    }

    [secUserInstance: secUserInstance]
  }

  def edit = {
    def secUserInstance = SecUser.get(params.id)
    if (!secUserInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'secUser.label', default: 'SecUser'), params.id])
      redirect(action: "list")
      return
    }

    [secUserInstance: secUserInstance]
  }

  def update = {
    def secUserInstance = SecUser.get(params.id)
    SecUserSecRole.removeAll(secUserInstance)
    params.optionsRoles.each {
      def secRole = SecRole.get(Long.parseLong(it))
      new SecUserSecRole(secUser: secUserInstance, secRole: secRole).save()
    }
    if (!secUserInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'secUser.label', default: 'SecUser'), params.id])
      redirect(action: "list")
      return
    }

    if (params.version) {
      def version = params.version.toLong()
      if (secUserInstance.version > version) {
        secUserInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
          [message(code: 'secUser.label', default: 'SecUser')] as Object[],
          "Another user has updated this SecUser while you were editing")
        render(view: "edit", model: [secUserInstance: secUserInstance])
        return
      }
    }

    secUserInstance.properties = params

    if (!secUserInstance.save(flush: true)) {
      render(view: "edit", model: [secUserInstance: secUserInstance])
      return
    }

    flash.message = message(code: 'secuser.updated.message', args: [message(code: 'secUser.label', default: 'SecUser'), secUserInstance.id, secUserInstance.username])
    redirect(action: "show", id: secUserInstance.id)
  }

  def delete = {
    def secUserInstance = SecUser.get(params.id)
    if (!secUserInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'secUser.label', default: 'SecUser'), params.id, secUserInstance.username])
      redirect(action: "list")
      return
    }

    try {
      SecUserSecRole.findAllBySecUser(secUserInstance).each { it.delete() }
      secUserInstance.delete(flush:true)
      flash.message = message(code: 'secuser.deleted.message', args: [message(code: 'secUser.label', default: 'SecUser'), params.id, secUserInstance.username])
      redirect(action: "list")
    }
    catch (DataIntegrityViolationException e) {
      flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'secUser.label', default: 'SecUser'), params.id, secUserInstance.username])
      redirect(action: "show", id: params.id)
    }
  }

  def account = {
    def user = springSecurityService.currentUser
    if (!user)
    {
//      flash.message = "An error has occurred. Please login."
      redirect(controller: 'login', action: 'auth')
    }
    [user: user]
  }

  def updatePassword = {
    if (params.password)
    {
      SecUser user = springSecurityService.currentUser
      PasswordEncoder passwordEncoder = springSecurityService.passwordEncoder
      if (!user)
      {
        flash.message = "An error has occurred. Please login."
        redirect(controller: 'login', action: 'auth')
      }

      String password = params.password
      String newPassword = params.password_new
      String newPassword2 = params.password_new2
      if (!password || !newPassword || !newPassword2)
      {
        flash.message = "Please enter your current password and a valid new password"
        return
        // redirect
      }
      if (newPassword == newPassword2)
      {
        if (!passwordEncoder.isPasswordValid(user.password, password, null))
        {
          flash.message = "Your password does not match the current password in the system"
          return
          // redirect
        }
        if (passwordEncoder.isPasswordValid(user.password, newPassword, null))
        {
          flash.message = "Please enter a new password that is different from your existing password"
          return
          // redirect
        }

        user.password = newPassword
        if (user.save(flush:true))
        {
          flash.message = "Your password has been changed successfully."
          redirect(controller: 'secUser', action: 'account')
        }
        else
        {
          flash.message = "There was an error in changing your password. Please try again."
        }
        return
      }
      else
      {
        flash.message = "Your NEW passwords do not match"
        return
        // redirect
      }
    }
  }

  def forgotPassword = {}
  def resetPasswordError = {}
  def resetPasswordSuccess = {}
  def resetPasswordSent = {}

  def sendPasswordReset = {
    SecUser user = springSecurityService.currentUser
    if (params.username)
    {
      user = SecUser.findByUsername(params.username)
    }
    else if (params.email)
    {
      user = SecUser.findByEmail(params.email)
    }
    if (user)
    {
      String keyMashup = "${user.username}${user.email}${grailsApplication.config.salt}"
      String hash = keyMashup.encodeAsMD5()

      // send email with hash key
      if (grailsApplication.config.send.email.on)
      {
        def link = "${grailsApplication.config.grails.serverURL}/secUser/resetPassword?resetuser=${user.username}&resetkey=${hash}"
        def message = """<p>Hi ${user.username},</p>
            <p>Click the link below to reset your password.</p>
            <p><a href="${link}">${link}</a></p>
            <p>If you believe that you have received this in error, please contact us immediately.</p>
            <p>Thanks!</p>
            <p>The BioIT Team</p>""".toString()
        mailService.sendMail {
          to "${user.email}"
          from "${grailsApplication.config.importer.defaultEMailFrom}"
          subject "BRI Tools: Reset your password"
          html "${message}"
        }
        redirect(action: 'resetPasswordSent')
      }
      else
      {
        redirect(action:'resetPassword', params:[ resetuser:user.username, resetkey:hash ])
      }
    }
    else
    {
      flash.message = "No account with that information matches any user in our database. Please try again."
      redirect(action: 'forgotPassword')
    }
  }

  def resetPassword = {
    if (params.resetkey && params.resetuser)
    {
      return [params: params]
    }
    else if (params.username)
    {
      SecUser user = SecUser.findByUsername(params.username)
      String keyMashup = "${user.username}${user.email}${grailsApplication.config.salt}"
      String hash = keyMashup.encodeAsMD5()
      if (params.secretKey && params.newPassword && params.newPassword2)
      {
        def password = params.newPassword
        def password2 = params.newPassword2
        if (params.secretKey == hash)
        {
          if (password == password2)
          {
            user.password = password
            if (user.save(flush:true))
            {
              redirect(action: 'resetPasswordSuccess')
            }
            else
            {
              redirect(action: 'resetPasswordError')
            }
          }
          else
          {
            flash.message = "Your new passwords do not match!"
          }
        }
        else
        {
          redirect(action: 'resetPasswordError')
        }
      }
      else
      {
        flash.message = "Please fill out all information to reset your password."
      }
      return [params: [resetuser: params.username, resetkey: params.secretKey]]
    }
    else
    {
      redirect(action: 'forgotPassword')
    }
  }

  private def String getHostName() {
    String hostName = "localhost"
    try {
      hostName = InetAddress.localHost.hostName
    } catch (UnknownHostException e) {
    }
    return hostName;
  }

}
