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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.stream.Stream;

public class PDFTemplates {

    private Logo logo;
    private List<Template> before;
    private Template model;
    private List<Template> after;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Template> getAfter() {
        return after;
    }

    public void setAfter(List<Template> after) {
        this.after = after;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Template> getBefore() {
        return before;
    }

    public void setBefore(List<Template> before) {
        this.before = before;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Template getModel() {
        return model;
    }

    public void setModel(Template model) {
        this.model = model;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Logo getLogo() {
        return logo;
    }

    public void setLogo(Logo logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "PDFTemplates{" +
                "after=" + after +
                ", logo=" + logo +
                ", before=" + before +
                ", model=" + model +
                '}';
    }

    @JsonIgnore
    public Template getTocTemplate() {
        return Stream
                .concat ( before.stream(), after.stream() )
                .filter (Template::isTocTemplate)
                .findFirst()
                .orElse(null);
    }

}