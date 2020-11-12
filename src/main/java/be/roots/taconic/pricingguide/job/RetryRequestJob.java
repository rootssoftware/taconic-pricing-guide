package be.roots.taconic.pricingguide.job;

/*
   This file is part of the Taconic Pricing Guide generator.  This code will
   generate a full featured PDF Pricing Guide by using using iText
   (http://www.itextpdf.com) based on JSON files.

   Copyright (C) 2015  Roots nv
   Authors: Koen Dehaen (koen.dehaen@roots.be)

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   For more information, please contact Roots nv at this address: support@roots.be
 */

import be.roots.taconic.pricingguide.domain.Request;
import be.roots.taconic.pricingguide.service.PricingGuideService;
import be.roots.taconic.pricingguide.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class RetryRequestJob {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RetryRequestJob.class);

    private final PricingGuideService pricingGuideService;

    @Value("${request.retry.location}")
    private String requestRetryLocation;

    public RetryRequestJob(PricingGuideService pricingGuideService) {
        this.pricingGuideService = pricingGuideService;
    }

    @Scheduled(fixedDelay = 30000)
    public void retryPricingGuideRequestForUnfoundContact() {

        final List<Request> unsendRequests = getUnsendRequests();
        for ( Request unsendRequest : unsendRequests ) {
            LOGGER.info(unsendRequest.getId() + " - Retrying to send request for hsID " + unsendRequest.getHsId() + " a " + unsendRequest.getRetryCount() + " time");
            unsendRequest.increaseRetryCount();
            try {
                pricingGuideService.buildPricingGuide(unsendRequest);
            } catch (IOException e) {
                LOGGER.error ( unsendRequest.getId() + " - " + e.getLocalizedMessage(), e );
            }
        }

    }

    private List<Request> getUnsendRequests() {

        final File[] files = new File(requestRetryLocation).listFiles();
        final List<Request> result = new ArrayList<>();
        if ( files != null ) {
            for ( File file : files ) {
                if ( file.isFile() && file.getName().endsWith(".txt") ) {
                    try {
                        final Request request = JsonUtil.asObject(FileUtils.readFileToString(file), Request.class);
                        result.add (request);
                        file.delete();
                    } catch (IOException e) {
                        LOGGER.error ( e.getLocalizedMessage(), e );
                    }
                }
            }
        }

        return result;
    }

}