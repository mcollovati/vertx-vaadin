package org.vaadin.crudui.demo.authentication;

import com.vaadin.hilla.Nonnull;
import org.vaadin.crudui.demo.entity.User;

import java.util.Collection;
import java.util.Collections;

/**
 * User information used in client-side authentication and authorization.
 * To be saved in browsers’ LocalStorage for offline support.
 */
public final class UserInfo {

    @Nonnull
    private final String name;
    @Nonnull
    private final Collection<@Nonnull String> authorities;

    private final User info;


    public UserInfo(String name, Collection<String> authorities, User info) {
        this.name = name;
        this.authorities = Collections.unmodifiableCollection(authorities);
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getAuthorities() {
        return authorities;
    }

    public User getInfo() {
        return info;
    }

}
