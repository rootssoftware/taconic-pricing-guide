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
import be.roots.taconic.pricingguide.domain.Model;
import be.roots.taconic.pricingguide.respository.ModelRepository;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PricingGuideApplication.class)
@WebAppConfiguration
public class PDFServiceTest {

    private final static Logger LOGGER = Logger.getLogger(PDFServiceTest.class);

    private final static boolean MANUAL_MODE = true;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private ModelRepository modelRepository;

    @Test
    public void testCreatePricingGuideForOneModel() throws IOException, DocumentException {

        final Model model = modelRepository.findOne("WH");

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), Arrays.asList(model)), 13);

    }

    @Test
    public void testCreatePricingGuideForSeveralModels() throws IOException, DocumentException {

        final List<String> models = new ArrayList<>();
        models.add ( "koen");
        models.add ( "WKY");
        models.add ( "10498");
        models.add ( "P53N5");
        models.add ( "P53N12");
        models.add ( "1921");
        models.add ( "SW");
        models.add ( "WH");
        models.add ( "2148");
        models.add ( "LEWIS");
        models.add ( "4026");

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), modelRepository.findFor(models)), 17);

    }

    @Test
    public void testCreatePricingGuideWithItalicSupport() throws IOException, DocumentException {

        final List<String> models = new ArrayList<>();
        models.add ( "NOG");
        models.add ("HSCFTL-NOG");

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), modelRepository.findFor(models)), 15);

    }


    @Test
    public void testCreatePricingGuideForAllModels() throws IOException, DocumentException {

        final List<String> modelIds = new ArrayList<>();
        modelIds.add ( "all");

        final List<Model> models = modelRepository.findFor(modelIds);

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), models), 104);

    }

    @Test
    public void testCreatePricingGuideForAllModelsAllCurrencies() throws IOException, DocumentException {

        final List<Model> models = modelRepository.findAll();

        final Contact contact = buildContact();

        contact.setCurrency(Currency.EUR);
        saveAndTestByteArray(pdfService.createPricingGuide(contact, models), 100);

        contact.setCurrency(Currency.NEUR);
        saveAndTestByteArray(pdfService.createPricingGuide(contact, models), 100);

        contact.setCurrency(Currency.USD);
        saveAndTestByteArray(pdfService.createPricingGuide(contact, models), 106);

        contact.setCurrency(Currency.NUSD);
        saveAndTestByteArray(pdfService.createPricingGuide(contact, models), 106);

    }

    private Contact buildContact() {
        final Contact contact = new Contact();
        contact.setSalutation("Dr.");
        contact.setFirstName("Koen");
        contact.setLastName("Dehaen");
        contact.setEmail("koen.dehaen@roots.be");
        contact.setCompany("Roots Software");
        contact.setCurrency(Currency.NEUR);
        return contact;
    }

    public static void saveAndTestByteArray(byte[] fileAsByteArray, Integer numberOfPagesExpected) throws IOException, DocumentException {

        try {

            final byte[] clone = fileAsByteArray.clone();

            assertNotNull ( clone );

            File file = File.createTempFile("result-" + Thread.currentThread().getStackTrace()[2].getMethodName() + "-", ".pdf");
            IOUtils.write(fileAsByteArray, new FileOutputStream(file));

            if ( MANUAL_MODE ) {
                LOGGER.info("Wrote test file to " + file.getAbsolutePath());
            } else {
                assertTrue ( file.exists() );
                assertTrue ( file.length() > 0 );
                file.delete();
            }

            if ( numberOfPagesExpected != null ) {
                PdfReader reader = new PdfReader(clone);
                assertEquals ( numberOfPagesExpected.intValue(), reader.getNumberOfPages() );
            }

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue ( false );
        }
    }

}