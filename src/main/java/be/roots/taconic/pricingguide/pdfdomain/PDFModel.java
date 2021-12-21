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

import be.roots.taconic.pricingguide.domain.Contact;
import be.roots.taconic.pricingguide.domain.Model;
import be.roots.taconic.pricingguide.domain.Pricing;
import be.roots.taconic.pricingguide.util.SpecialCharactersUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class PDFModel implements Comparable<PDFModel> {

    private final Model model;
    private final Contact contact;

    private final List<PDFCategory> categories;

    public PDFModel(Model model, Contact contact) {
        this.model = model;
        this.contact = contact;

        // keep only the valid prices for this contact
        this.categories = validPricings()
                .stream()
                .filter(Objects::nonNull)
                .map(PDFCategory::new)
                .collect(toList());
    }

    public List<PDFCategory> getCategories() {
        return categories;
    }

    private String getProductName() {
        return this.model.getProductName();
    }

    public String getUrl() {
        return this.model.getUrl();
    }

    public String getModelNumber() {
        return this.model.getModelNumber();
    }

    public String getAnimalType() {
        return this.model.getAnimalType();
    }

    public String getHealthReport() {
        return this.model.getHealthReport();
    }

    public String getSpecies() {
        return this.model.getSpecies();
    }

    public String getLicense() {
        return this.model.getLicense();
    }

    public String getProductNameProcessed() {
        return SpecialCharactersUtil.encode(getProductName()
                .replaceAll("<span.*?>", "")
                .replaceAll("</span>", "")
                .trim()
        );
    }

    private String getProductNameToSortOn() {
        // ensure that greek characters are sorted behind the letter 'Z'
        return getProductNameProcessed().replace("&", "{");
    }

    @Override
    public int compareTo(@NotNull PDFModel other) {
        return this.getProductNameToSortOn().compareToIgnoreCase(other.getProductNameToSortOn());
    }

    public List<String> getApplicationsSorted() {
        if (!CollectionUtils.isEmpty(this.model.getApplications())) {
            final List<String> apps = new ArrayList<>(this.model.getApplications());
            Collections.sort(apps);
            return apps;
        }
        return new ArrayList<>();
    }

    @JsonIgnore
    public String getNomenclatureParsed() {
        return this.model.getNomenclature()
                .replaceAll("<span.*?>", "")
                .replaceAll("</span>", "")
                .replaceAll("<div.*?>", "")
                .replaceAll("</div>", "")
                .replaceAll("<font.*?>", "")
                .replaceAll("</font>", "")
                .replaceAll("<p.*?>", "")
                .replaceAll("</p>", "")
                .trim();
    }

    public List<Pricing> validPricings() {
        final List<Pricing> validPricing = new ArrayList<>();

        if (!CollectionUtils.isEmpty(model.getPricing())) {

            final Set<String> categoryCodes = new LinkedHashSet<>();
            final Map<String, Pricing> nonProfitPricing = new HashMap<>();
            final Map<String, Pricing> profitPricing = new HashMap<>();

            // loop through all pricings and note all the available and split on profitability
            model.getPricing()
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
            // if there is no non-profit category, just take the profit pricing
            for (String categoryCode : categoryCodes) {
                if (contact.getCurrency().isNonProfit() && nonProfitPricing.containsKey(categoryCode)) {
                    validPricing.add(nonProfitPricing.get(categoryCode));
                } else {
                    validPricing.add(profitPricing.get(categoryCode));
                }
            }

        }
        return validPricing;
    }

}