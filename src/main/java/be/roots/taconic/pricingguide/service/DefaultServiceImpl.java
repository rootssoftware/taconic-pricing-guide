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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DefaultServiceImpl implements DefaultService {

    @Value("${url.base}")
    private String baseUrl;

    @Value("${url.username}")
    private String userName;

    @Value("${url.password}")
    private String password;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getModelUrl() {
        return getBaseUrl() + "/models";
    }

    @Override
    public String getModelCatalogUrl() {
        return getBaseUrl() + "/models/catalog_models.json";
    }

}