package org.openelisglobal.systemuser.service;

import java.util.List;

import org.openelisglobal.login.valueholder.Login;
import org.openelisglobal.systemuser.valueholder.SystemUser;

public interface UserService {

    void updateLoginUser(Login loginUser, boolean loginUserNew, SystemUser systemUser, boolean systemUserNew,
            List<String> selectedRoles, String loggedOnUserId);

}
