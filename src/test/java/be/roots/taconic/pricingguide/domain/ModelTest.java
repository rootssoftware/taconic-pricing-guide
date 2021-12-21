package be.roots.taconic.pricingguide.domain;

/**
 * This file is part of the Taconic Pricing Guide generator.  This code will
 * generate a full featured PDF Pricing Guide by using using iText
 * (http://www.itextpdf.com) based on JSON files.
 * <p>
 * Copyright (C) 2015  Roots nv
 * Authors: Koen Dehaen (koen.dehaen@roots.be)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * For more information, please contact Roots nv at this address: support@roots.be
 */

import be.roots.taconic.pricingguide.PricingGuideApplication;
import be.roots.taconic.pricingguide.service.ModelService;
import be.roots.taconic.pricingguide.util.DefaultUtil;
import be.roots.taconic.pricingguide.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PricingGuideApplication.class)
@WebAppConfiguration
public class ModelTest {

    @Autowired
    private ModelService modelService;

    @Test
    public void testValidPricings_bothPricingsAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<>());
        m.getPricing().add(buildPricing(true, Currency.EUR, "A"));
        m.getPricing().add(buildPricing(false, Currency.EUR, "A" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT));

        List<Pricing> pricings = modelService.convert(m, nonProfitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(1, pricings.size());
        assertFalse(pricings.get(0).isProfit());

        pricings = modelService.convert(m, profitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(1, pricings.size());
        assertTrue(pricings.get(0).isProfit());

    }

    @Test
    public void testValidPricings_onlyProfitPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<>());
        m.getPricing().add(buildPricing(true, Currency.EUR, "A"));

        List<Pricing> pricings = modelService.convert(m, nonProfitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(1, pricings.size());
        assertTrue(pricings.get(0).isProfit());

        pricings = modelService.convert(m, profitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(1, pricings.size());
        assertTrue(pricings.get(0).isProfit());

    }

    @Test
    public void testValidPricings_mixedPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<>());
        m.getPricing().add(buildPricing(true, Currency.EUR, "A"));
        m.getPricing().add(buildPricing(true, Currency.EUR, "B"));
        m.getPricing().add(buildPricing(false, Currency.EUR, "B" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT));

        List<Pricing> pricings = modelService.convert(m, nonProfitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(2, pricings.size());
        assertEquals("A-M CATEGORY", pricings.get(0).getCategoryCode());
        assertTrue(pricings.get(0).isProfit());
        assertEquals("B-M CATEGORY", pricings.get(1).getCategoryCode());
        assertFalse(pricings.get(1).isProfit());

        pricings = modelService.convert(m, profitContact()).validPricings();

        assertEquals(2, pricings.size());
        assertEquals("A-M CATEGORY", pricings.get(0).getCategoryCode());
        assertTrue(pricings.get(0).isProfit());
        assertEquals("B-M CATEGORY", pricings.get(1).getCategoryCode());
        assertTrue(pricings.get(1).isProfit());

    }

    @Test
    public void testValidPricings_mixedCurrencyPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<>());
        m.getPricing().add(buildPricing(true, Currency.EUR, "A"));
        m.getPricing().add(buildPricing(true, Currency.EUR, "B"));
        m.getPricing().add(buildPricing(false, Currency.EUR, "B" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT));
        m.getPricing().add(buildPricing(true, Currency.USD, "C"));
        m.getPricing().add(buildPricing(false, Currency.USD, "C" + DefaultUtil.PRICING_CATEGORY_NON_PROFIT));
        m.getPricing().add(buildPricing(true, Currency.USD, "D"));

        List<Pricing> pricings = modelService.convert(m, nonProfitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(2, pricings.size());
        assertEquals("A-M CATEGORY", pricings.get(0).getCategoryCode());
        assertTrue(pricings.get(0).isProfit());
        assertEquals("B-M CATEGORY", pricings.get(1).getCategoryCode());
        assertFalse(pricings.get(1).isProfit());

        pricings = modelService.convert(m, profitContact()).validPricings();

        assertEquals(2, pricings.size());
        assertEquals("A-M CATEGORY", pricings.get(0).getCategoryCode());
        assertTrue(pricings.get(0).isProfit());
        assertEquals("B-M CATEGORY", pricings.get(1).getCategoryCode());
        assertTrue(pricings.get(1).isProfit());

    }

    @Test
    public void testValidPricings_noPricingAvailable() {

        Model m = new Model();

        m.setPricing(new ArrayList<>());

        List<Pricing> pricings = modelService.convert(m, nonProfitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(0, pricings.size());

        pricings = modelService.convert(m, profitContact()).validPricings();

        assertNotNull(pricings);
        assertEquals(0, pricings.size());

    }

    @Test
    public void testJsonParsing() throws IOException {

        final String file = FileUtils.readFileToString(new ClassPathResource("b6-new.json").getFile(), StandardCharsets.UTF_8);

        assertTrue(StringUtils.hasText(file));

        final Model model = JsonUtil.asObject(file, Model.class);

        assertNotNull(model);

        assertTrue(model.getApplications().contains("ADMET"));
        assertEquals("https://etaconic.com/health/health-monitoring-index-for-animal-model?modelno=B6", model.getHealthReport());
        assertEquals("This model is sold subject to Taconic's Terms and Conditions.", model.getLicense());

        assertNotNull(model.getPricing());
        assertFalse(model.getPricing().isEmpty());

        final Pricing pricing = model.getPricing().get(0);

        assertTrue(pricing.isProfit());
        assertEquals(Currency.USD, pricing.getCurrency());
        assertNotNull(pricing.getLines());
        assertFalse(pricing.getLines().isEmpty());

        assertTrue(StringUtils.hasText(pricing.getMessage()));
        assertTrue(StringUtils.hasText(pricing.getCategory()));

    }

    private Contact nonProfitContact() {
        final Contact c = new Contact();
        c.setCurrency(Currency.NEUR);
        return c;
    }

    private Contact profitContact() {
        final Contact c = new Contact();
        c.setCurrency(Currency.EUR);
        return c;
    }

    private Pricing buildPricing(boolean profit, Currency currency, String category) {
        final Pricing p = new Pricing();
        p.setProfit(profit);
        p.setCategory(category + "-M category");
        p.setCurrency(currency);
        return p;
    }

}