package org.vaadin.crudui.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.crudui.demo.entity.Technology;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {

	List<Technology> findAllByParent(Technology parent);

	List<Technology> findAllByParentIsNull();

}
