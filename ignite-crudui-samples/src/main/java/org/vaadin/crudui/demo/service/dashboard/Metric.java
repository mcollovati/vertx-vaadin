package org.vaadin.crudui.demo.service.dashboard;

import com.vaadin.hilla.Nonnull;

public class Metric {
    @Nonnull
    private String name;
    @Nonnull
    private Double value;
    @Nonnull
    private String unit;
    @Nonnull
    private Double change;
    @Nonnull
    private Integer fractionDigits;


    public Metric(String name, Double value, String unit, Integer fractionDigits) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.change = 0.0;
        this.fractionDigits = fractionDigits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Integer getFractionDigits() {
        return fractionDigits;
    }

    public void setFractionDigits(Integer fractionDigits) {
        this.fractionDigits = fractionDigits;
    }
}
