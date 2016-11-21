package be.roots.taconic.pricingguide.domain;

/**
 *  This file is part of the Taconic Pricing Guide generator.  This code will
 *  generate a full featured PDF Pricing Guide by using using iText
 *  (http://www.itextpdf.com) based on JSON files.
 *
 *  Copyright (C) 2015  Roots nv
 *  Authors: Koen Dehaen (koen.dehaen@roots.be)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  For more information, please contact Roots nv at this address: support@roots.be
 *
 */

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

public class Model implements Comparable<Model> {

    private String url;
    private String modelNumber;
    private String productName;
    private String healthReport;
    private String nomenclature;
    private String species;
    private String animalType;
    private List<String> applications;
    private List<Pricing> pricing;

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getHealthReport() {
        return healthReport;
    }

    public void setHealthReport(String healthReport) {
        this.healthReport = healthReport;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private String getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(String nomenclature) {
        this.nomenclature = nomenclature;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public List<String> getApplications() {
        return applications;
    }

    public void setApplications(List<String> applications) {
        this.applications = applications;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public List<Pricing> getPricing() {
        return pricing;
    }

    public void setPricing(List<Pricing> pricing) {
        this.pricing = pricing;
    }

    @JsonIgnore
    public List<Pricing> validPricings(Contact contact) {
        final List<Pricing> validPricing = new ArrayList<>();

        if (!CollectionUtils.isEmpty(pricing)) {

            final Set<String> categoryCodes = new LinkedHashSet<>();
            final Map<String, Pricing> nonProfitPricing = new HashMap<>();
            final Map<String, Pricing> profitPricing = new HashMap<>();

            // loop trough all pricings and note all the available
            pricing
                    .stream()
                    .filter(p -> p.getCurrency().getIsoCode().equals(contact.getCurrency().getIsoCode()))
                    .forEachOrdered(p -> {
                        categoryCodes.add(p.getCategoryCode());
                        if (p.isProfit()) {
                            profitPricing.put(p.getCategoryCode(), p);
                        } else {
                            nonProfitPricing.put(p.getCategoryCode(), p);
                        }
                    });

            // for each code; look for the correct pricing
            for ( String categoryCode : categoryCodes ) {

                if ( contact.getCurrency().isNonProfit() ) {
                    if ( nonProfitPricing.containsKey(categoryCode )) {
                        validPricing.add ( nonProfitPricing.get(categoryCode) );
                    } else {
                        validPricing.add ( profitPricing.get(categoryCode) );
                    }

                } else {
                    validPricing.add ( profitPricing.get(categoryCode) );
                }

            }

        }
        return validPricing;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    @JsonIgnore
    public String getProductNameProcessed() {
        String pName = getProductName();
        pName = pName.replaceAll("<span.*?>", "");
        pName = pName.replaceAll("</span>", "");
        pName = pName.replaceAll("&reg;", "\u00AE");
        pName = pName.replaceAll("&trade;", "\u2122");
        pName = pName.trim();
        return pName;
    }

    @JsonIgnore
    private String getProductNameToSortOn() {
        // ensure that greek characters are sorted behind the letter 'Z'
        return getProductNameProcessed().replace("&", "{");
    }

    @JsonIgnore
    public String getNomenclatureParsed() {
        String strNomen = getNomenclature();
        strNomen = strNomen.replaceAll("<span.*?>", "");
        strNomen = strNomen.replaceAll("</span>", "");
        strNomen = strNomen.replaceAll("<div.*?>", "");
        strNomen = strNomen.replaceAll("</div>", "");
        strNomen = strNomen.replaceAll("<font.*?>", "");
        strNomen = strNomen.replaceAll("</font>", "");
        strNomen = strNomen.replaceAll("<p.*?>", "");
        strNomen = strNomen.replaceAll("</p>", "");
        strNomen = strNomen.trim();
        return strNomen;
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

    public List<String> getApplicationsSorted() {
        if (!CollectionUtils.isEmpty ( applications )) {
            final List<String> apps = new ArrayList<>(applications);
            Collections.sort(apps);
            return apps;
        }
        return new ArrayList<>();
    }

    @Override
    public int compareTo(@NotNull Model other) {
        return this.getProductNameToSortOn().compareToIgnoreCase(other.getProductNameToSortOn());
    }

}