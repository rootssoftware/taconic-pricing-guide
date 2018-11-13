package be.roots.taconic.pricingguide.util;

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

import be.roots.taconic.pricingguide.PricingGuideApplication;
import be.roots.taconic.pricingguide.domain.Currency;
import be.roots.taconic.pricingguide.domain.Line;
import be.roots.taconic.pricingguide.domain.Model;
import be.roots.taconic.pricingguide.domain.Pricing;
import be.roots.taconic.pricingguide.service.DefaultService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PricingGuideApplication.class)
@WebAppConfiguration
public class JsonUtilTest {

    @Autowired
    private DefaultService defaultService;

    @Test
    public void testAsString() throws IOException {

        final Line line = new Line();
        line.setMale("");
        line.setFemale("142.22");
        line.setQuantity("1");
        line.setAge("Female w/Litter");

        final Pricing pricing = new Pricing();
        pricing.setCurrency(Currency.USD);
        pricing.setCategory("WH (in U.S. Dollars):");
        pricing.setMessage("*Untimed pregnant requests are filled with late gestation pregnant females. Pups may be delivered in transit.");
        pricing.setProfit(true);
        pricing.setLines(Arrays.asList(line, line, line));

        final Model model = new Model();
        model.setModelNumber("WH");
        model.setHealthReport("http://ihms.taconic.com/Index/LineIndex?AnimalLine=WH");
        model.setApplications(Arrays.asList("ADMET", "Safety Assessment"));
        model.setSpecies("Rat");
        model.setNomenclature("HanTax:WH");
        model.setAnimalType("Outbred");
        model.setProductName("Wistar Hannover GALAS&trade;");
        model.setPricing(Arrays.asList(pricing, pricing, pricing));

        assertEquals (
                "{\"modelNumber\":\"WH\",\"productName\":\"Wistar Hannover GALAS&trade;\",\"healthReport\":\"http://ihms.taconic.com/Index/LineIndex?AnimalLine=WH\",\"nomenclature\":\"HanTax:WH\",\"species\":\"Rat\",\"animalType\":\"Outbred\",\"applications\":[\"ADMET\",\"Safety Assessment\"],\"pricing\":[{\"profit\":true,\"currency\":\"USD\",\"category\":\"WH (in U.S. Dollars):\",\"lines\":[{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"}],\"message\":\"*Untimed pregnant requests are filled with late gestation pregnant females. Pups may be delivered in transit.\"},{\"profit\":true,\"currency\":\"USD\",\"category\":\"WH (in U.S. Dollars):\",\"lines\":[{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"}],\"message\":\"*Untimed pregnant requests are filled with late gestation pregnant females. Pups may be delivered in transit.\"},{\"profit\":true,\"currency\":\"USD\",\"category\":\"WH (in U.S. Dollars):\",\"lines\":[{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"}],\"message\":\"*Untimed pregnant requests are filled with late gestation pregnant females. Pups may be delivered in transit.\"}],\"applicationsSorted\":[\"ADMET\",\"Safety Assessment\"]}",
                JsonUtil.asJson(model)
        );

    }

    @Test
    public void testAsObjectFromUrl() throws IOException {

        final String jsonAsString = "{\"modelNumber\":\"WH\",\"productName\":\"Wistar Hannover GALAS&trade;\",\"healthReport\":\"http://ihms.taconic.com/Index/LineIndex?AnimalLine=WH\",\"nomenclature\":\"HanTax:WH\",\"species\":\"Rat\",\"animalType\":\"Outbred\",\"applications\":[\"ADMET\",\"Safety Assessment\"],\"pricing\":[{\"profit\":true,\"currency\":\"USD\",\"category\":\"WH (in U.S. Dollars):\",\"lines\":[{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"}],\"message\":\"*Untimed pregnant requests are filled with late gestation pregnant females. Pups may be delivered in transit.\"},{\"profit\":true,\"currency\":\"USD\",\"category\":\"WH (in U.S. Dollars):\",\"lines\":[{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"}],\"message\":\"*Untimed pregnant requests are filled with late gestation pregnant females. Pups may be delivered in transit.\"},{\"profit\":true,\"currency\":\"USD\",\"category\":\"WH (in U.S. Dollars):\",\"lines\":[{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"},{\"male\":\"\",\"female\":\"142.22\",\"quantity\":\"1\",\"age\":\"Female w/Litter\"}],\"message\":\"*Untimed pregnant requests are filled with late gestation pregnant females. Pups may be delivered in transit.\"}],\"applicationsSorted\":[\"ADMET\",\"Safety Assessment\"]}"; //HttpUtil.readString("<url-to-json-file>", defaultService.getUserName(), defaultService.getPassword());

        assertNotNull ( jsonAsString );

        final Model model = JsonUtil.asObject(jsonAsString, Model.class);

        assertEquals ("Wistar Hannover GALAS&trade;", model.getProductName());
    }

}