/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.metadata.realization;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.util.ClassUtil;
import org.apache.kylin.metadata.cachesync.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 */
public class RealizationRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RealizationRegistry.class);
    private static final ConcurrentMap<KylinConfig, RealizationRegistry> CACHE = new ConcurrentHashMap<KylinConfig, RealizationRegistry>();

    public static RealizationRegistry getInstance(KylinConfig config) {
        RealizationRegistry r = CACHE.get(config);
        if (r != null) {
            return r;
        }

        synchronized (RealizationRegistry.class) {
            r = CACHE.get(config);
            if (r != null) {
                return r;
            }
            try {
                r = new RealizationRegistry(config);
                CACHE.put(config, r);
                if (CACHE.size() > 1) {
                    logger.warn("More than one singleton exist");
                }
                return r;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to init CubeManager from " + config, e);
            }
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }

    // ============================================================================

    private Map<RealizationType, IRealizationProvider> providers;
    private KylinConfig config;

    private RealizationRegistry(KylinConfig config) throws IOException {
        logger.info("Initializing RealizationRegistry with metadata url " + config);
        this.config = config;
        init();

        Broadcaster.getInstance(config).registerListener(new Broadcaster.Listener() {
            @Override
            public void onClearAll(Broadcaster broadcaster) throws IOException {
                clearCache();
            }
        }, "");
    }

    private void init() {
        providers = Maps.newConcurrentMap();

        // use reflection to load providers
        String[] providerNames = config.getRealizationProviders();
        for (String clsName : providerNames) {
            try {
                Class<? extends IRealizationProvider> cls = ClassUtil.forName(clsName, IRealizationProvider.class);
                IRealizationProvider p = (IRealizationProvider) cls.getMethod("getInstance", KylinConfig.class).invoke(null, config);
                providers.put(p.getRealizationType(), p);

            } catch (Exception | NoClassDefFoundError e) {
                if (e instanceof ClassNotFoundException || e instanceof NoClassDefFoundError)
                    logger.warn("Failed to create realization provider " + e);
                else
                    logger.error("Failed to create realization provider", e);
            }
        }

        if (providers.isEmpty())
            throw new IllegalArgumentException("Failed to find realization provider by url: " + config.getMetadataUrl());

        logger.info("RealizationRegistry is " + providers);
    }

    public Set<RealizationType> getRealizationTypes() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    public IRealization getRealization(RealizationType type, String name) {
        IRealizationProvider p = providers.get(type);
        if (p == null) {
            logger.warn("No provider for realization type " + type);
            return null;
        }

        try {
            return p.getRealization(name);
        } catch (Exception ex) {
            // exception is possible if e.g. cube metadata is wrong
            logger.warn("Failed to load realization " + type + ":" + name, ex);
            return null;
        }
    }

}
