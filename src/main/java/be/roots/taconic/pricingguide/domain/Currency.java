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

public enum Currency {

    USD("USD", "US Dollar", null),
    NUSD("USD", "US Dollar Non-Profit", USD),
    EUR("EUR", "Euro", null),
    NEUR("EUR", "Euro Non-Profit", EUR);

    private final String description;
    private final String isoCode;
    private final Currency profitCurrency;

    Currency(String isoCode, String description, Currency profitCurrency) {
        this.isoCode = isoCode;
        this.description = description;
        this.profitCurrency = profitCurrency;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getDescription() {
        return description;
    }

    public String getTitlePageDescription() {
        return getDescription().toUpperCase().replaceAll("-", "");
    }

    public Currency getProfitCurrency() {
        return profitCurrency;
    }

    public boolean isNonProfit() {
        return profitCurrency != null;
    }

    public static Currency getEnum(String value) {

        if ( !StringUtils.isEmpty(value)) {
            for ( Currency currency : Currency.values() ) {
                if ( value.toLowerCase().endsWith(currency.getDescription().toLowerCase()) ) {
                    return currency;
                }
            }
        }
        return null;
    }

}