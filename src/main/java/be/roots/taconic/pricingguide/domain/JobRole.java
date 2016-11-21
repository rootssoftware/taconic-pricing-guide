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

public enum JobRole {

    PERSONA_1("Biotech Group Director"),
    PERSONA_2("Biotech Researcher"),
    PERSONA_3("Academic/Government Researcher"),
    PERSONA_4("Not-For-Profit Researcher"),
    PERSONA_5("Procurement Officer"),
    PERSONA_6("Veterinarian"),
    PERSONA_7("CRO Director"),
    PERSONA_8("CRO Research Associate"),
    PERSONA_9("Pharma Director"),
    PERSONA_10("Pharma Researcher"),

    NON_EXISTING_PERSONA("-");

    private final String description;

    JobRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}