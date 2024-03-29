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

import be.roots.taconic.pricingguide.domain.Model;
import be.roots.taconic.pricingguide.service.DefaultService;
import be.roots.taconic.pricingguide.util.HttpUtil;
import be.roots.taconic.pricingguide.util.JsonUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@Service
public class ModelRepositoryImpl implements ModelRepository {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ModelRepositoryImpl.class);

    @Value("${url.base}")
    private String urlBase;

    private final DefaultService defaultService;

    private final LoadingCache<String, Model> modelLoadingCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<>() {
                        public Model load(String key) throws IOException {
                            final String jsonAsString = HttpUtil.readString(defaultService.getModelUrl() + "/" + key + ".json", urlBase, defaultService.getUserName(), defaultService.getPassword());
                            if (jsonAsString != null) {
                                return JsonUtil.asObject(jsonAsString, Model.class);
                            }
                            return null;
                        }
                    });

    public ModelRepositoryImpl(DefaultService defaultService) {
        this.defaultService = defaultService;
    }

    @Override
    public Model findOne(String key) {
        try {
            return modelLoadingCache.get(key);
        } catch (RuntimeException | ExecutionException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    @Override
    public List<Model> findFor(List<String> modelList) throws IOException {

        final List<Model> models = new ArrayList<>();

        if (!CollectionUtils.isEmpty(modelList)) {

            // make the modelList a set of unique ids
            final Set<String> modelIds = new HashSet<>(modelList);

            // check if "all" is passed; if so execute findAll
            for (String modelId : modelIds) {
                if ("all".equalsIgnoreCase(modelId)) {
                    return findAll();
                }
            }

            // find the models based on their ids
            for (String modelId : modelIds) {
                final Model model = findOne(modelId);
                if (model != null) {
                    models.add(model);
                } else {
                    LOGGER.error("Couldn't find model for " + modelId);
                }
            }
        }

        return models;
    }

    @Override
    public List<Model> findAll() throws IOException {
        final String jsonAsString = HttpUtil.readString(defaultService.getModelCatalogUrl(), urlBase, defaultService.getUserName(), defaultService.getPassword());
        if (jsonAsString != null) {
            final List<HashMap> catalog = JsonUtil.asObject(jsonAsString, List.class);
            if (!CollectionUtils.isEmpty(catalog)) {
                return catalog
                        .stream()
                        .map(entry -> findOne((String) entry.get("catalog_id")))
                        .collect(toList());
            }
        }

        return new ArrayList<>();
    }

}