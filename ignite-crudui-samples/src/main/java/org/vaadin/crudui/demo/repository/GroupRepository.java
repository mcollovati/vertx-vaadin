package org.vaadin.crudui.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.crudui.demo.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

}
