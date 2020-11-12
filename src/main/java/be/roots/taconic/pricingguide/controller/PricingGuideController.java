package be.roots.taconic.pricingguide.controller;

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
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/find-your-model")
@EnableAsync
public class PricingGuideController {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PricingGuideController.class);

    @Value("${redirect.url}")
    private String redirect;

    private final PricingGuideService pricingGuideService;

    public PricingGuideController(PricingGuideService pricingGuideService) {
        this.pricingGuideService = pricingGuideService;
    }

    @RequestMapping(value = "/pricing-guide-build-request", method = RequestMethod.POST)
    public String pricingGuideBuildRequest(@RequestParam("hsID") String hsID, @RequestParam("modelList") List<String> modelList ) throws IOException {

        final long startTimestamp = System.currentTimeMillis();
        final String id = UUID.randomUUID().toString();

        LOGGER.info ( id + " - Request to start building a pricing guide for " + hsID + " based on " + modelList );

        pricingGuideService.buildPricingGuide( new Request( id, hsID, startTimestamp, modelList ) );

        LOGGER.info ( id + " - Build requested for " + hsID + " based on " + modelList + ", redirecting the user now." );

        return "redirect:" + redirect;

    }

}