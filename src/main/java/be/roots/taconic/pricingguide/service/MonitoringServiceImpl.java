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

import be.roots.mona.client.MonitoringClient;
import be.roots.taconic.pricingguide.domain.Contact;
import be.roots.taconic.pricingguide.domain.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class MonitoringServiceImpl implements MonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringServiceImpl.class);

    @Value("${environment}")
    private String environment;

    @Value("${region}")
    private String region;

    @Value("${mona.username}")
    private String monaUsername;

    @Value("${mona.password}")
    private String monaPassword;

    private MonitoringClient mona;
    private final Map<String, String> defaultMetaData = new HashMap<>();

    @PostConstruct
    public void init() {
        mona = new MonitoringClient( environment, monaUsername, monaPassword );
        defaultMetaData.put ( "region", region );
    }

    public void start(String name, String uuid ) {
        try {
            mona.startProcess(
                    name,
                    uuid,
                    System.currentTimeMillis(),
                    60
            );
        } catch ( Throwable e ) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public void stop(String name, String uuid, Contact contact) {
        try {
            mona.stopProcess(
                    name,
                    uuid,
                    System.currentTimeMillis(),
                    metaData(contact)
            );
        } catch ( Throwable e ) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }


    @Override
    public void iAmAlive() {
        try {
            mona.incrementCounter("i_am_alive", defaultMetaData);
        } catch ( Throwable e ) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        mona.set("set", "mlkj");
    }

    @Override
    public void incrementCounter(Model model, Contact contact) {
        try {
            final Map<String, String> metaData = metaData(contact);
            metaData.put ( "model", model.getProductNameProcessed() );
            mona.incrementCounter("model", metaData);
        } catch ( Throwable e ) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    private Map<String, String> metaData(Contact contact) {
        final Map<String, String> metaData = new HashMap<>();
        metaData.putAll ( defaultMetaData );
        metaData.put ( "country", contact.getCountry() );
        metaData.put ( "job_role", contact.getJobRole().getDescription() );
        metaData.put ( "therapeutic_area", contact.getTherapeuticArea() );
        metaData.put ( "currency", contact.getCurrency().getDescription() );
        metaData.put ( "region", region);
        if (StringUtils.hasText(contact.getEmail()) && contact.getEmail().lastIndexOf(".") >= 0) {
            metaData.put ( "email", contact.getEmail().substring(contact.getEmail().lastIndexOf(".")));
        }
        return metaData;
    }

}