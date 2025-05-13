package org.vaadin.crudui.demo.authentication;

import com.vaadin.hilla.BrowserCallable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.crudui.demo.entity.User;
import org.vaadin.crudui.demo.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Server endpoint that provides information about the current user to the
 * Hilla's client-side authentication.
 */
@BrowserCallable
@RolesAllowed("user")
public class UserInfoService {
    final UserService repo;

    UserInfoService(UserService repo){
        this.repo = repo;
    }

    @PermitAll
    @Nonnull
    public UserInfo getUserInfo() {
        String name = CurrentUser.get();
        User info = new User();
        info.setName(name);
        if(name!=null){
            Optional<User> userInfo = repo.findOne(info);
            if(userInfo.isPresent()) {
                return new UserInfo(name, List.of("user","vip"), info);
            }
            else{
                return new UserInfo(name, List.of("user"), info);
            }
        }
        return new UserInfo("guset", List.of("visit"), info);
    }

}
