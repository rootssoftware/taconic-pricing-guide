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

import org.springframework.util.StringUtils;

public class Contact {

    private String salutation;
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private String country;
    private Currency currency;
    private String hsId;
    private String persona;
    private String therapeuticArea;

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getHsId() {
        return hsId;
    }

    public void setHsId(String hsId) {
        this.hsId = hsId;
    }

    public void setPersona(String persona) {
        this.persona = persona;
    }

    public String getPersona() {
        return persona;
    }

    public String getTherapeuticArea() {
        return therapeuticArea;
    }

    public void setTherapeuticArea(String therapeuticArea) {
        this.therapeuticArea = therapeuticArea;
    }

    public JobRole getJobRole() {
        try {
            return JobRole.valueOf(persona.toUpperCase());
        } catch ( IllegalArgumentException e ) {
            return JobRole.NON_EXISTING_PERSONA;
        }
    }

    public String getFullName() {
        String fullName = "";
        if (! StringUtils.isEmpty(getSalutation())) {
            fullName += getSalutation();
        }
        if (! StringUtils.isEmpty(getFirstName())) {
            if (! StringUtils.isEmpty(fullName)) {
                fullName += " ";
            }
            fullName += getFirstName();
        }
        if (! StringUtils.isEmpty(getLastName())) {
            if (! StringUtils.isEmpty(fullName)) {
                fullName += " ";
            }
            fullName += getLastName();
        }

        return fullName;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "salutation='" + salutation + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", country='" + country + '\'' +
                ", currency=" + currency +
                ", therapeuticArea='" + therapeuticArea + '\'' +
                ", hsId='" + hsId + '\'' +
                '}';
    }

}