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
import be.roots.taconic.pricingguide.domain.Request;
import be.roots.taconic.pricingguide.respository.ModelRepository;
import be.roots.taconic.pricingguide.util.JsonUtil;
import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    @Autowired
    private MonitoringService monitoringService;

    @Value("${request.retry.location}")
    private String requestRetryLocation;

    @Value("${request.error.location}")
    private String requestErrorLocation;

    @Async
    @Override
    public void buildPricingGuide(Request request) throws IOException {

        LOGGER.info ( request.getId() + " - Received request to create pricing guide for hsID : " + request.getHsId() + ", modelList : " + request.getModelList() );

        final Contact contact = hubSpotService.getContactFor(request.getHsId());
        request.setRemoteIp(contact.getRemoteIp());

        monitoringService.start("pricing_guide_build_request", request.getId(), contact.getRemoteIp(), request.getStartTimestamp());

        if ( contact == null ) {
            LOGGER.error(request.getId() + " - Unable to find a Contact for hsID : " + request.getHsId() + ", will save and retry later");
            saveRequestForRetry ( request );
            return;
        }

        try {
            LOGGER.info ( request.getId() + " - Report the request in the CSV record " );
            reportService.report(contact, request.getModelList());
        } catch (IOException e) {
            LOGGER.error ( request.getId() + " - Couldn't report the request in the CSV record, still continuing with creating pricing guide ", e );
        }

        final List<Model> models = modelRepository.findFor ( request.getModelList() );

        if (CollectionUtils.isEmpty(models)) {
            LOGGER.error ( request.getId() + " - No models were found, thus no guide can be created." );
            return;
        }

        final byte[] pricingGuide;
        try {
            pricingGuide = pdfService.createPricingGuide(contact, models);
        } catch (DocumentException e) {
            LOGGER.error ( request.getId() + " - Could create the pricing guide.", e);
            return;
        }

        if ( pricingGuide == null ) {
            LOGGER.error ( request.getId() + " - Could create the pricing guide.");
            return;
        }

        try {
            mailService.sendMail(contact, pricingGuide);
        } catch (MessagingException e) {
            LOGGER.error( request.getId() + " - Error in sending the pricing guide to " + contact.getEmail(), e);
            return;
        }

        LOGGER.info ( request.getId() + " - Pricing guide successfully sent to  " + contact.getEmail() );

        monitoringService.stop("pricing_guide_build_request", request.getId(), request.getRemoteIp(), contact);

        if (!CollectionUtils.isEmpty(models) && monitoringService.shouldBeMonitored(contact.getRemoteIp())) {
            models.forEach( model -> monitoringService.incrementCounter(model, contact));
        }

    }

    private void saveRequestForRetry(Request request) {

        String location = requestRetryLocation;

        if ( request.getRetryCount() > 5 ) {
            location = requestErrorLocation;
        }

        try {
            final String asJson = JsonUtil.asJson(request);

            FileUtils.writeStringToFile(new File(location, UUID.randomUUID().toString() + ".txt"), asJson);
        } catch (IOException e) {
            LOGGER.error ( request.getId() +  " - " + e.getLocalizedMessage(), e);
        }

    }

}