package be.roots.taconic.pricingguide.service;

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

import be.roots.taconic.pricingguide.domain.Contact;
import be.roots.taconic.pricingguide.domain.Currency;
import be.roots.taconic.pricingguide.hubspot.FormSubmission;
import be.roots.taconic.pricingguide.hubspot.RecentContact;
import be.roots.taconic.pricingguide.hubspot.RecentContacts;
import be.roots.taconic.pricingguide.util.HttpUtil;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class HubSpotServiceImpl implements HubSpotService {
    
    private final static Logger LOGGER = Logger.getLogger(HubSpotService.class);

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.contact.url}")
    private String apiContactUrl;

    public Contact getContactFor ( String hsID ) throws IOException {

        LOGGER.info("Start getting the recent contact information based on conversion-id: " + hsID );

        final RestTemplate restTemplate = new RestTemplate();

        RecentContacts recentContacts = null;
        do {
            if ( recentContacts == null ) {
                // first call
                LOGGER.info("Initial call for recent contacts" );
                recentContacts = restTemplate.getForObject(apiUrl + apiKey + "&count=100", RecentContacts.class);
            } else {
                // page through the next calls
                LOGGER.info("Secondary call for recent contacts with timeOffset=" + recentContacts.getTimeOffset() + ", and vidOffset=" + recentContacts.getVidOffset() );
                recentContacts = restTemplate.getForObject(apiUrl + apiKey + "&count=100&timeOffset=" + recentContacts.getTimeOffset() + "&vidOffset=" + recentContacts.getVidOffset(), RecentContacts.class);
            }

            if ( recentContacts != null && ! CollectionUtils.isEmpty(recentContacts.getContacts())) {

                for ( RecentContact contact : recentContacts.getContacts() ) {

                    if ( ! CollectionUtils.isEmpty(contact.getFormSubmissions() ) ) {

                        for ( FormSubmission form : contact.getFormSubmissions() ) {

                            if ( hsID.equals(form.getConversionId()) ) {
                                final Contact result = getContactDetailsFor(String.format(apiContactUrl + apiKey, contact.getVid()));
                                result.setHsId ( hsID );
                                return result;

                            }

                        }

                    }

                }
            }

        } while (recentContacts != null && recentContacts.isHasMore());

        LOGGER.info("No recent contact information found for: " + hsID );

        return null;

    }

    public Contact getContactDetailsFor(String url) throws IOException {
        //Call Contact Api to get individual information
        final String response2 = HttpUtil.readString(url);
        final ObjectMapper hsContactMap = new ObjectMapper();
        final JsonNode contactjson = hsContactMap.readTree(response2);
        final JsonNode contactProperties = contactjson.get("properties");

        if ( contactProperties  != null ) {

            final Contact contact = new Contact();
            contact.setSalutation(parse(contactProperties, "salutation"));
            contact.setFirstName(parse(contactProperties, "firstname"));
            contact.setLastName(parse(contactProperties, "lastname"));
            contact.setEmail(parse(contactProperties, "email"));
            contact.setCompany(parse(contactProperties, "company"));
            contact.setCountry(parse(contactProperties, "country_dd"));
            contact.setPersona(parse(contactProperties, "hs_persona"));
            contact.setCurrency(Currency.getEnum(parse(contactProperties, "catalog_currency")));
            contact.setTherapeuticArea(parse(contactProperties,"therapeutic_area_form_submissions"));

            contact.setRemoteIp(parse(contactProperties, "ipaddress") );

            LOGGER.info("Contact: " + contact.toString());

            return contact;
        }
        return null;
    }

    private String parse ( JsonNode jsonProperties, String key ) {
        if ( jsonProperties != null
                && jsonProperties.get(key) != null
                && jsonProperties.get(key).get("value") != null ) {

            return jsonProperties.get(key).get("value").getTextValue();

        }
        return null;
    }

}