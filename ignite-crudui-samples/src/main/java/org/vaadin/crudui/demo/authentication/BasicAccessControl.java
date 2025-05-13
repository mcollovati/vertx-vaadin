package org.vaadin.crudui.demo.authentication;

/**
 * Default mock implementation of {@link AccessControl}. This implementation
 * accepts any string as a password, and considers the user "admin" as the only
 * administrator.
 */
public class BasicAccessControl implements AccessControl {

    @Override
    public boolean signIn(String username, String password) {
        if (username == null || username.isEmpty())
            return false;

        if (isValidUser(username, password)) {
            CurrentUser.set(username);
            return true;
        }
        return false;
    }

    public boolean isValidUser(String username, String password) {
        if(!username.equals(password)){
            return false;
        }
        return true;
    }

    @Override
    public boolean isUserSignedIn() {
        return !CurrentUser.get().isEmpty();
    }

    @Override
    public boolean isUserInRole(String role) {
        if (ADMIN_ROLE_NAME.equals(role)) {
            // Only the "admin" user is in the "admin" role
            return getPrincipalName().equals(ADMIN_USERNAME);
        }

        // All users are in all non-admin roles
        return true;
    }

    @Override
    public String getPrincipalName() {
        return CurrentUser.get();
    }

}
