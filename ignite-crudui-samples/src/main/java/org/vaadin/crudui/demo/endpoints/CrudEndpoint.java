package org.vaadin.crudui.demo.endpoints;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;
import org.vaadin.crudui.demo.authentication.CurrentUser;
import org.vaadin.crudui.demo.entity.User;
import org.vaadin.crudui.demo.service.UserService;

import java.util.List;

@Endpoint
@AnonymousAllowed
class CrudEndpoint {
  private UserService repo;


  CrudEndpoint(UserService repo) {
    this.repo = repo;
  }

  public @Nonnull List<@Nonnull User> findAll() {
    return repo.findAll();
  }

  public User save(User person) {
    String user = CurrentUser.get();
    return repo.save(person);
  }
}
