package be.roots.taconic.pricingguide.service;

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
import be.roots.taconic.pricingguide.domain.Contact;
import be.roots.taconic.pricingguide.domain.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = PricingGuideApplication.class)
@WebAppConfiguration
public class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void test() throws IOException {

        final Contact contact = new Contact();
        contact.setCompany("Roots nv");
        contact.setCurrency(Currency.EUR);
        contact.setEmail("koen.dehaen@roots.be");
        contact.setLastName("Dehaen");
        contact.setFirstName("Koen");
        contact.setCountry("Belgium");
        contact.setPersona("persona_1");
        contact.setHsId(UUID.randomUUID().toString());
        contact.setRemoteIp("127.0.0.1");

        final List<String> models = new ArrayList<>();
        models.add ("model1");
        models.add ("model2");
        models.add ("model3");
        models.add ("model4");

        for ( int i = 1; i <= 10; i ++ ) {
            reportService.report( contact, models );
        }

    }
}
