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
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.util.StringUtils;

import java.util.List;

public class Pricing {

    private boolean profit;
    private Currency currency;
    private String category;
    private List<Line> lines;
    private String message;

    private boolean initialized = false;
    private boolean quantities = false;
    private boolean female = false;
    private boolean male = false;
    private boolean age = false;
    private int numberOfHeaderItems = 0;

    private void init() {
        if ( ! initialized ) {
            initialized = true;
            for ( Line line : lines ) {
                if ( ! StringUtils.isEmpty(line.getQuantity()) ) {
                    quantities = true;
                }
                if ( ! StringUtils.isEmpty(line.getAge()) ) {
                    age = true;
                }
                if ( ! StringUtils.isEmpty(line.getFemale()) ) {
                    female = true;
                }
                if ( ! StringUtils.isEmpty(line.getMale()) ) {
                    male = true;
                }
            }
            if ( isQuantities() ) {
                numberOfHeaderItems++;
            }
            if ( isAge() ) {
                numberOfHeaderItems++;
            }
            if ( isFemale() ) {
                numberOfHeaderItems++;
            }
            if ( isMale() ) {
                numberOfHeaderItems++;
            }
        }
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public boolean isProfit() {
        return profit;
    }

    public void setProfit(boolean profit) {
        this.profit = profit;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonIgnore
    public String getCategoryCode() {
        if ( ! StringUtils.isEmpty(category)) {
            return category.replaceAll(DefaultUtil.PRICING_CATEGORY_NON_PROFIT, "" ).toUpperCase().trim();
        }
        return category;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public boolean isQuantities() {
        init();
        return quantities;
    }

    @JsonIgnore
    public boolean isFemale() {
        init();
        return female;
    }

    @JsonIgnore
    public boolean isMale() {
        init();
        return male;
    }

    @JsonIgnore
    public boolean isAge() {
        init();
        return age;
    }

    @JsonIgnore
    public int getNumberOfHeaderItems() {
        init();
        return numberOfHeaderItems;
    }

    @Override
    public String toString() {
        return "Pricing{" +
                "profit=" + profit +
                ", currency=" + currency +
                ", category='" + category + '\'' +
                ", lines=" + lines +
                ", message='" + message + '\'' +
                ", initialized=" + initialized +
                ", quantities=" + quantities +
                ", female=" + female +
                ", male=" + male +
                ", age=" + age +
                ", numberOfHeaderItems=" + numberOfHeaderItems +
                '}';
    }

}