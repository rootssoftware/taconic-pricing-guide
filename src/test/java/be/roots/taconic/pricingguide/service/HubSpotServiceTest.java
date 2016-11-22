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
import be.roots.taconic.pricingguide.domain.JobRole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PricingGuideApplication.class)
@WebAppConfiguration
public class HubSpotServiceTest {

    @Value("${api.key}")
    private String apiKey;

    @Autowired
    private HubSpotService hubSpotService;

    @Test
    public void testGetContactFor() throws IOException {

        final Contact contact = hubSpotService.getContactFor("0868eebe-b7c7-4758-85e3-7e8fd93bba4f");

        assertEquals ( "Dr.", contact.getSalutation() );
        assertEquals ( "Koen", contact.getFirstName() );
        assertEquals ( "Dehaen", contact.getLastName() );
        assertEquals ( "koen.dehaen@roots.be", contact.getEmail() );
        assertEquals ( "Roots nv", contact.getCompany() );
        assertEquals ( "Belgium", contact.getCountry() );
        assertEquals ( JobRole.PERSONA_2, contact.getJobRole() );
        assertEquals ( "EUR", contact.getCurrency().getIsoCode() );
        assertEquals ( "Cardiovascular/Metabolic Disease", contact.getTherapeuticArea() );
        assertEquals ( "141.135.6.193", contact.getRemoteIp() );

    }

    @Test
    public void testGetContactDetailsFor() throws IOException {

        final Contact contact = hubSpotService.getContactDetailsFor("<hubspot-url>" + apiKey);

        assertEquals ( "Roots nv", contact.getCompany() );
        assertEquals ( "Koen", contact.getFirstName() );
        assertEquals ( "Dehaen", contact.getLastName() );
        assertEquals ( "Dr.", contact.getSalutation() );
        assertEquals ( "koen.dehaen@roots.be", contact.getEmail() );
        assertEquals ( "EUR", contact.getCurrency().getIsoCode() );

    }

}