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
import be.roots.taconic.pricingguide.pdfdomain.PDFModel;
import be.roots.taconic.pricingguide.respository.ModelRepository;
import com.itextpdf.text.DocumentException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = PricingGuideApplication.class)
@WebAppConfiguration
public class PDFServiceTest {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PDFServiceTest.class);

    private final static boolean MANUAL_MODE = true;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelService modelService;

    @Test
    public void testCreatePricingGuideForOneModel() throws IOException, DocumentException {

        final PDFModel model = modelService.convert(modelRepository.findOne("ABBN12"), buildContact());

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), Collections.singletonList(model)), 13);

    }

    @Test
    public void testCreatePricingGuideForToc() throws IOException, DocumentException {

        final PDFModel model = modelService.convert(modelRepository.findOne("WH"), buildContact());

        List<PDFModel> models = new ArrayList<>();

        for ( int i = 0; i < 7 ; i ++ ) {             models.add (model);         }
        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), models), 7);

        models.clear();
        for ( int i = 0; i < 8 ; i ++ ) {             models.add (model);         }
        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), models), 8);

        models.clear();
        for ( int i = 0; i < 7 + 38 ; i ++ ) {             models.add (model);         }
        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), models), 7 + 38);

        models.clear();
        for ( int i = 0; i < 7 + 39 ; i ++ ) {             models.add (model);         }
        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), models),  7 + 39);

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

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), modelService.convert(modelRepository.findFor(models), buildContact())), 17);

    }

    @Test
    public void testCreatePricingGuideForModelsWithMultiplePageTables() throws IOException, DocumentException {

        final List<String> models = new ArrayList<>();
        // The SD model should produce a table larger than one page that will be split up by iText
        models.add ( "SD");
        // The SW model should have different price models that span multiple pages
        models.add ( "SW");

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), modelService.convert(modelRepository.findFor(models), buildContact())), 17);

    }

    @Test
    public void testCreatePricingGuideWithItalicSupport() throws IOException, DocumentException {

        final List<String> models = new ArrayList<>();
        models.add ( "NOG");
        models.add ("HSCFTL-NOG");

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), modelService.convert(modelRepository.findFor(models), buildContact())), 15);

    }


    @Test
    public void testCreatePricingGuideForAllModels() throws IOException, DocumentException {

        final List<String> modelIds = new ArrayList<>();
        modelIds.add ( "all");

        final List<Model> models = modelRepository.findFor(modelIds);

        saveAndTestByteArray(pdfService.createPricingGuide(buildContact(), modelService.convert(models, buildContact())), 104);

    }

    @Test
    public void testCreatePricingGuideForAllModelsAllCurrencies() throws IOException, DocumentException {

        final Contact contact = buildContact();

        final List<PDFModel> models = modelService.convert(modelRepository.findAll(), buildContact());

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

    public static void saveAndTestByteArray(byte[] fileAsByteArray, Integer numberOfPagesExpected) {

        try {

            final byte[] clone = fileAsByteArray.clone();

            assertNotNull ( clone );

            final File tempFile = File.createTempFile(Thread.currentThread().getStackTrace()[2].getMethodName() + "-" + numberOfPagesExpected+ "-", ".pdf");
            final File tempDir = new File(tempFile.getParent(), "_taconic");
            if (!tempDir.exists()) tempDir.mkdirs();
            final File file = new File (tempDir, tempFile.getName());
            IOUtils.write(fileAsByteArray, new FileOutputStream(file));

            if ( MANUAL_MODE ) {
                LOGGER.info("Wrote test file to " + file.getAbsolutePath());
            } else {
                assertTrue ( file.exists() );
                assertTrue ( file.length() > 0 );
                file.delete();
            }
            tempFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}