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

import be.roots.taconic.pricingguide.util.DefaultUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pricing {

    private boolean profit;
    private HealthStatus healthstatus;
    private List<Line> lines;
    private List<String> quantities;
    private String message;
    private Currency currency;
    private List<Line> linesSpecialized;
    private List<String> quantitiesSpecialized;
    private String category;
    private String gender;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public boolean isProfit() {
        return profit;
    }

    public void setProfit(boolean profit) {
        this.profit = profit;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public HealthStatus getHealthstatus() {
        return healthstatus;
    }

    public void setHealthstatus(HealthStatus healthstatus) {
        this.healthstatus = healthstatus;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonIgnore
    public String getCategoryCode() {
        if (hasText(category)) {
            return category.replaceAll(DefaultUtil.PRICING_CATEGORY_NON_PROFIT, "").toUpperCase().trim();
        }
        return category;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<String> quantities) {
        this.quantities = quantities;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Line> getLinesSpecialized() {
        return linesSpecialized;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getQuantitiesSpecialized() {
        return quantitiesSpecialized;
    }

    public void setQuantitiesSpecialized(List<String> quantitiesSpecialized) {
        this.quantitiesSpecialized = quantitiesSpecialized;
    }

    public void setLinesSpecialized(List<Line> linesSpecialized) {
        this.linesSpecialized = linesSpecialized;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Pricing{" +
                "profit=" + profit +
                ", lines=" + lines +
                ", message='" + message + '\'' +
                ", currency=" + currency +
                ", linesSpecialized=" + linesSpecialized +
                ", category='" + category + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }

}