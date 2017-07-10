package controllers.security

import org.pac4j.core.authorization.authorizer.ProfileAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile

class StorageManagerAuthorizer extends ProfileAuthorizer[CommonProfile] {

  def isAuthorized(context: WebContext, profiles: java.util.List[CommonProfile]): Boolean = {
    return isAllAuthorized(context, profiles)
  }

  def isProfileAuthorized(context: WebContext, profile: CommonProfile): Boolean = {
    if (profile == null) {
      false
    } else {
        if (context.getPath.endsWith("read"))
            profile.getAttribute("scope").toString.equalsIgnoreCase("storage:read")
        else if (context.getPath.endsWith("write"))
            profile.getAttribute("scope").toString.equalsIgnoreCase("storage:write")
        else
          false
    }
  }
}