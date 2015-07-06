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

import be.roots.taconic.pricingguide.domain.Contact;
import be.roots.taconic.pricingguide.domain.Model;
import be.roots.taconic.pricingguide.respository.ModelRepository;
import com.itextpdf.text.DocumentException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@Service
public class PricingGuideServiceImpl implements PricingGuideService {

    private final static Logger LOGGER = Logger.getLogger(PricingGuideServiceImpl.class);

    @Autowired
    private HubSpotService hubSpotService;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ReportService reportService;

    @Async
    @Override
    public void buildPricingGuide(String id, String hsID, List<String> modelList) throws IOException {

        LOGGER.info ( id + " - Received request to create pricing guide for hsID : " + hsID + ", modelList : " + modelList );

        final Contact contact = hubSpotService.getContactFor(hsID);

        if ( contact == null ) {
            LOGGER.error ( id + " - Unable to find a Contact for hsID : " + hsID );
            return;
        }

        try {
            LOGGER.info ( id + " - Report the request in the CSV record " );
            reportService.report(contact, modelList );
        } catch (IOException e) {
            LOGGER.error ( id + " - Couldn't report the request in the CSV record, still continuing with creating pricing guide ", e );
        }

        final List<Model> models = modelRepository.findFor ( modelList );

        if (CollectionUtils.isEmpty(models)) {
            LOGGER.error ( id + " - No models were found, thus no guide can be created." );
            return;
        }

        final byte[] pricingGuide;
        try {
            pricingGuide = pdfService.createPricingGuide(contact, models);
        } catch (DocumentException e) {
            LOGGER.error (id + " - Could create the pricing guide.", e);
            return;
        }

        if ( pricingGuide == null ) {
            LOGGER.error (id + " - Could create the pricing guide.");
            return;
        }

        try {
            mailService.sendMail(contact, pricingGuide);
        } catch (MessagingException e) {
            LOGGER.error(id + " - Error in sending the pricing guide to " + contact.getEmail(), e);
            return;
        }

        LOGGER.info ( id + " - Pricing guide successfully sent to  " + contact.getEmail() );
    }

}