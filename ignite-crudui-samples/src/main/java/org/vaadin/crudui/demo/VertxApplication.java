package org.vaadin.crudui.demo;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point of the Spring Boot application.
 */

public class VertxApplication {
	private static Logger log = LoggerFactory.getLogger(VertxApplication.class);

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		IgniteCurdUIVerticle verticle = new IgniteCurdUIVerticle();
		Future<String> msg = vertx.deployVerticle(verticle);
	}

}
