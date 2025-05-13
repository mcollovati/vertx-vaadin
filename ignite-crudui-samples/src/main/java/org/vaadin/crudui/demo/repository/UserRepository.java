package org.vaadin.crudui.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.crudui.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

	long countByNameContainingIgnoreCase(String name);

}
