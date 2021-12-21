package be.roots.taconic.pricingguide.domain;

/*
   This file is part of the Taconic Pricing Guide generator.  This code will
   generate a full featured PDF Pricing Guide by using using iText
   (http://www.itextpdf.com) based on JSON files.

   Copyright (C) 2015  Roots nv
   Authors: Koen Dehaen (koen.dehaen@roots.be)

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   For more information, please contact Roots nv at this address: support@roots.be
 */


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Model{

    private String url;
    private String modelNumber;
    private String productName;
    private String healthReport;
    private String nomenclature;
    private String license;
    private String species;
    private String animalType;
    private List<String> applications;
    private List<Pricing> pricing;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getHealthReport() {
        return healthReport;
    }

    public void setHealthReport(String healthReport) {
        this.healthReport = healthReport;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(String nomenclature) {
        this.nomenclature = nomenclature;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getLicense() {
        return license == null ? null : license.replaceAll("\\n", "");
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getApplications() {
        return applications;
    }

    public void setApplications(List<String> applications) {
        this.applications = applications;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Pricing> getPricing() {
        return pricing;
    }

    public void setPricing(List<Pricing> pricing) {
        this.pricing = pricing;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    @Override
    public String toString() {
        return "Model{" +
                "modelNumber='" + modelNumber + '\'' +
                ", productName='" + productName + '\'' +
                ", healthReport='" + healthReport + '\'' +
                ", nomenclature='" + nomenclature + '\'' +
                ", species='" + species + '\'' +
                ", animalType='" + animalType + '\'' +
                ", applications=" + applications +
                ", pricing=" + pricing +
                '}';
    }


}