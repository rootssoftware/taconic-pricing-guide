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
import be.roots.taconic.pricingguide.domain.Model;
import be.roots.taconic.pricingguide.respository.ModelRepository;
import com.itextpdf.text.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PricingGuideApplication.class)
@WebAppConfiguration
public class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private ModelRepository modelRepository;

    @Test
    public void testMailSending() throws IOException, DocumentException, MessagingException {

        final Contact contact = new Contact();
        contact.setFirstName("Koen");
        contact.setLastName("Dehaen");
        contact.setEmail("koen.dehaen@roots.be");

        List<Model> models = modelRepository.findFor(Collections.singletonList("WKY"));
        byte[] guide = pdfService.createPricingGuide(contact, models);

        mailService.sendMail( contact, guide );

    }

}