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

import org.codehaus.jackson.map.annotate.JsonSerialize;

public class Line {

    private String male;
    private String female;
    private String quantity;
    private String age;

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getMale() {
        return male;
    }

    public void setMale(String male) {
        this.male = male;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getFemale() {
        return female;
    }

    public void setFemale(String female) {
        this.female = female;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Line{" +
                "male='" + male + '\'' +
                ", female='" + female + '\'' +
                ", quantity='" + quantity + '\'' +
                ", age='" + age + '\'' +
                '}';
    }

}