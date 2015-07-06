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
import be.roots.taconic.pricingguide.domain.Currency;
import be.roots.taconic.pricingguide.domain.JobRole;
import be.roots.taconic.pricingguide.util.HttpUtil;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

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

        if (StringUtils.isEmpty(hsID)) {
            LOGGER.error("Will not start looking for an empty hsID " + hsID);
            return null;
        }

        LOGGER.info("Start get HubSpot info");

        try {

            final String response = HttpUtil.readString(apiUrl + apiKey);
            final ObjectMapper hsMap = new ObjectMapper();
            final JsonNode json = hsMap.readTree(response);

            LOGGER.info("Looking to match " + hsID);
            if(json != null) {

                String vid = null;
                //loop over contacts nodes to find matching form hsid
                for (JsonNode thisNode : Lists.newArrayList((json.get("contacts").getElements()))) {
                    final ArrayList<JsonNode> formNodes = Lists.newArrayList(thisNode.get("form-submissions").getElements());
                    if ( ! CollectionUtils.isEmpty ( formNodes ) ) {
                        for (JsonNode innerFormNode : formNodes) {
                            if (hsID.equals(innerFormNode.get("conversion-id").getTextValue())) {
                                LOGGER.info("Match found, now resolving VID as " + thisNode.get("vid"));
                                vid = thisNode.get("vid").toString();
                            }
                        }
                    }
                }

                if ( vid != null ) {

                    final Contact contact = getContactDetailsFor(String.format(apiContactUrl + apiKey, vid));
                    contact.setHsId ( hsID );
                    return contact;

                }

            }
        } catch (MalformedURLException e) {
            LOGGER.error ( e.getLocalizedMessage(), e );
        }

        LOGGER.error ( "Not able to find contact for hsID " + hsID );

        return null;

    }

    public Contact getContactDetailsFor(String url) throws IOException {
        //Call Contact Api to get individual information
        final String response2 = HttpUtil.readString(url);
        final ObjectMapper hsContactMap = new ObjectMapper();
        final JsonNode contactjson = hsContactMap.readTree(response2);
        final JsonNode jsonProperties = contactjson.get("properties");

        if ( jsonProperties  != null ) {

            final Contact contact = new Contact();
            contact.setSalutation(parse(jsonProperties, "salutation"));
            contact.setFirstName(parse(jsonProperties, "firstname"));
            contact.setLastName(parse(jsonProperties, "lastname"));
            contact.setEmail(parse(jsonProperties, "email"));
            contact.setCompany(parse(jsonProperties, "company"));
            contact.setCountry(parse(jsonProperties, "country_dd"));
            contact.setPersona(parse(jsonProperties, "hs_persona"));
            contact.setCurrency(Currency.getEnum(parse(jsonProperties, "catalog_currency")));

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