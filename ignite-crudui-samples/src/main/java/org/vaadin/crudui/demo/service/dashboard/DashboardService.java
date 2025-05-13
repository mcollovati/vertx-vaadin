package org.vaadin.crudui.demo.service.dashboard;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final Random random;
    private final Sinks.Many<List<Metric>> sink = Sinks.many().multicast().directBestEffort();
    private final Flux<List<Metric>> flux = sink.asFlux().replay(1).autoConnect();
    private List<Metric> latestMetrics;

    public DashboardService() {
        random = new Random(1);
        latestMetrics = List.of(
                new Metric(MetricType.CURRENT_USERS.getName(), 745.0, "", 0),
                new Metric(MetricType.VIEWS.getName(), 54.6, "k", 1),
                new Metric(MetricType.CONVERSION_RATE.getName(), 18.0, "%", 1),
                new Metric(MetricType.SIGNUPS.getName(), 300.0, "", 0));

        sink.emitNext(latestMetrics, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public enum MetricType {
        CURRENT_USERS("Current users"), VIEWS("Views"), CONVERSION_RATE("Conversion rate"), SIGNUPS(
                "Signups");

        private String name;

        MetricType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Scheduled(fixedRate = 2000)
    private void updateMetrics() {
        latestMetrics = latestMetrics.stream().map(metric -> {
            var previousValue = metric.getValue();
            metric.setValue(
                    Math.round(10 * (previousValue + (random.nextGaussian()) * 0.5)) / 10.0);
            metric.setChange(100 - (100 * (metric.getValue() / previousValue)));
            return metric;
        }).collect(Collectors.toList());

        sink.emitNext(latestMetrics, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public Flux<List<Metric>> getMetrics() {
        return flux;
    }

    public List<OrderInfo> getOrderInfo() {
        return List.of(
                new OrderInfo("Berlin",
                        List.of(189, 191, 191, 196, 201, 203, 209, 212, 229, 242, 244, 247)),
                new OrderInfo("London",
                        List.of(138, 146, 148, 148, 152, 153, 163, 173, 178, 179, 185, 187)),
                new OrderInfo("New York",
                        List.of(65, 65, 66, 71, 93, 102, 108, 117, 127, 129, 135, 136)),
                new OrderInfo("Tokyo", List.of(0, 11, 17, 23, 30, 42, 48, 49, 52, 54, 58, 62)));
    }


}
