package be.roots.taconic.pricingguide.pdfdomain;

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

import be.roots.taconic.pricingguide.domain.Line;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PDFPricing {

    private List<String> quantities;
    private final List<PDFPricingLine> lines;

    private int numberOfHeaderItems;

    public PDFPricing(List<String> quantities, List<Line> lines) {

        if ( lines == null || lines.isEmpty() ) {
            this.lines = new ArrayList<>();
        } else {
            this.quantities = quantities;
            this.lines = lines
                    .stream()
                    .map(PDFPricingLine::new)
                    .collect(toList());
        }

        if (hasQuantities()) numberOfHeaderItems+=getQuantities().size();
        if (hasAge()) numberOfHeaderItems++;

    }

    public List<String> getQuantities() {
        return quantities;
    }

    public List<PDFPricingLine> getPricingLines() {
        return lines;
    }

    public int getNumberOfHeaderItems() {
        return numberOfHeaderItems;
    }

    public boolean hasQuantities() {
        return this.quantities != null && !this.quantities.isEmpty();
    }

    public boolean hasAge() {
        return this.lines
                .stream()
                .anyMatch(PDFPricingLine::hasAge);
    }

    public boolean hasLines() {
        return this.lines != null && !this.lines.isEmpty();
    }

}