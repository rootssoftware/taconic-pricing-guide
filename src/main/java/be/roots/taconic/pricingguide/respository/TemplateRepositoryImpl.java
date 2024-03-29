package be.roots.taconic.pricingguide.respository;

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

import be.roots.taconic.pricingguide.service.DefaultService;
import be.roots.taconic.pricingguide.util.HttpUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class TemplateRepositoryImpl implements TemplateRepository {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TemplateRepositoryImpl.class);

    @Value("${url.base}")
    private String urlBase;

    private final DefaultService defaultService;

    private final LoadingCache<String, byte[]> templateLoadingCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<>() {
                        public byte[] load(String key) {
                            return HttpUtil.readByteArray(key, urlBase, defaultService.getUserName(), defaultService.getPassword());
                        }
                    });

    public TemplateRepositoryImpl(DefaultService defaultService) {
        this.defaultService = defaultService;
    }

    @Override
    public byte[] findOne(String templateUrl) {
        try {
            return templateLoadingCache.get(templateUrl);
        } catch (ExecutionException e) {
            LOGGER.error ( e.getLocalizedMessage(), e);
        }
        return null;
    }

}