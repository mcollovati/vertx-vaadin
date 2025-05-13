package org.vaadin.crudui.demo.endpoints;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;
import org.vaadin.crudui.demo.service.dashboard.DashboardService;
import org.vaadin.crudui.demo.service.dashboard.Metric;
import org.vaadin.crudui.demo.service.dashboard.OrderInfo;
import reactor.core.publisher.Flux;

import java.util.List;

@Endpoint
@AnonymousAllowed
class DashboardEndpoint {
  private DashboardService service;

  DashboardEndpoint(DashboardService service) {
    this.service = service;
  }

  public @Nonnull List<@Nonnull OrderInfo> getOrderInfo() {
    return service.getOrderInfo();
  }

  public @Nonnull Flux<List<@Nonnull Metric>> getMetrics() {
    return service.getMetrics();
  }
}
