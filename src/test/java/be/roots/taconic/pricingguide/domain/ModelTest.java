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

import be.roots.taconic.pricingguide.util.DefaultUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ModelTest {

    @Test
    public void testValidPricings_bothPricingsAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<Pricing>());
        m.getPricing().add ( buildPricing ( true, Currency.EUR, "A" ) );
        m.getPricing().add(buildPricing(false, Currency.EUR, "A" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT));

        List<Pricing> pricings = m.validPricings(nonProfitContact());

        assertNotNull ( pricings );
        assertEquals(1, pricings.size());
        assertEquals(false, pricings.get(0).isProfit());

        pricings = m.validPricings(profitContact());

        assertNotNull ( pricings );
        assertEquals(1, pricings.size());
        assertEquals ( true, pricings.get(0).isProfit() );

    }

    @Test
    public void testValidPricings_onlyProfitPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<Pricing>());
        m.getPricing().add ( buildPricing ( true, Currency.EUR, "A" ) );

        List<Pricing> pricings = m.validPricings(nonProfitContact());

        assertNotNull ( pricings );
        assertEquals(1, pricings.size());
        assertEquals(true, pricings.get(0).isProfit());

        pricings = m.validPricings(profitContact());

        assertNotNull ( pricings );
        assertEquals(1, pricings.size());
        assertEquals ( true, pricings.get(0).isProfit() );

    }

    @Test
    public void testValidPricings_mixedPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<Pricing>());
        m.getPricing().add(buildPricing(true, Currency.EUR, "A"));
        m.getPricing().add(buildPricing(true, Currency.EUR, "B"));
        m.getPricing().add ( buildPricing ( false, Currency.EUR, "B" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT ) );

        List<Pricing> pricings = m.validPricings(nonProfitContact());

        assertNotNull(pricings);
        assertEquals(2, pricings.size());
        assertEquals("A", pricings.get(0).getCategoryCode());
        assertEquals(true, pricings.get(0).isProfit());
        assertEquals("B", pricings.get(1).getCategoryCode());
        assertEquals(false, pricings.get(1).isProfit());

        pricings = m.validPricings(profitContact());

        assertEquals(2, pricings.size());
        assertEquals("A", pricings.get(0).getCategoryCode());
        assertEquals(true, pricings.get(0).isProfit());
        assertEquals("B", pricings.get(1).getCategoryCode());
        assertEquals(true, pricings.get(1).isProfit());

    }

    @Test
    public void testValidPricings_mixedCurrencyPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<Pricing>());
        m.getPricing().add(buildPricing(true, Currency.EUR, "A"));
        m.getPricing().add(buildPricing(true, Currency.EUR, "B"));
        m.getPricing().add ( buildPricing ( false, Currency.EUR, "B" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT ) );
        m.getPricing().add(buildPricing(true, Currency.USD, "C"));
        m.getPricing().add ( buildPricing ( false, Currency.USD, "C" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT ) );
        m.getPricing().add(buildPricing(true, Currency.USD, "D"));

        List<Pricing> pricings = m.validPricings(nonProfitContact());

        assertNotNull(pricings);
        assertEquals(2, pricings.size());
        assertEquals("A", pricings.get(0).getCategoryCode());
        assertEquals(true, pricings.get(0).isProfit());
        assertEquals("B", pricings.get(1).getCategoryCode());
        assertEquals(false, pricings.get(1).isProfit());

        pricings = m.validPricings(profitContact());

        assertEquals(2, pricings.size());
        assertEquals("A", pricings.get(0).getCategoryCode());
        assertEquals(true, pricings.get(0).isProfit());
        assertEquals("B", pricings.get(1).getCategoryCode());
        assertEquals(true, pricings.get(1).isProfit());

    }

    @Test
    public void testValidPricings_noPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<Pricing>());

        List<Pricing> pricings = m.validPricings(nonProfitContact());

        assertNotNull(pricings);
        assertEquals(0, pricings.size());

        pricings = m.validPricings(profitContact());

        assertNotNull(pricings);
        assertEquals(0, pricings.size());

    }

    private Contact nonProfitContact() {
        Contact c = new Contact();
        c.setCurrency(Currency.NEUR);
        return c;
    }

    private Contact profitContact() {
        Contact c = new Contact();
        c.setCurrency(Currency.EUR);
        return c;
    }

    private Pricing buildPricing( boolean profit, Currency currency, String category ) {
        Pricing p = new Pricing();
        p.setProfit(profit);
        p.setCategory(category);
        p.setCurrency(currency);
        return p;
    }

}