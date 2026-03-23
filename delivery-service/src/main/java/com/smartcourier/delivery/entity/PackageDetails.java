package com.smartcourier.delivery.entity;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
@Embeddable
public class PackageDetails {

    private String parcelType;
    private BigDecimal weightInKg;
    private BigDecimal declaredValue;
    private String dimensions;
    private String notes;

    public String getParcelType() {
        return parcelType;
    }

    public void setParcelType(String parcelType) {
        this.parcelType = parcelType;
    }

    public BigDecimal getWeightInKg() {
        return weightInKg;
    }

    public void setWeightInKg(BigDecimal weightInKg) {
        this.weightInKg = weightInKg;
    }

    public BigDecimal getDeclaredValue() {
        return declaredValue;
    }

    public void setDeclaredValue(BigDecimal declaredValue) {
        this.declaredValue = declaredValue;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
