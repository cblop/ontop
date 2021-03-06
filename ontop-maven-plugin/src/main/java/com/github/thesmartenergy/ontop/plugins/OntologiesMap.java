/*
 * Copyright 2016 ITEA 12004 SEAS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thesmartenergy.ontop.plugins;

import com.github.thesmartenergy.ontop.OntopException;
import java.util.HashMap;
import java.util.Map;

/**
 * A representations map is serialized into turtle, or generated from a turtle
 * document. It is generated by the plugin, then parsed by the dependency to be
 * used by jersey filters and resources.
 *
 * @author Maxime Lefrançois <maxime.lefrancois at emse.fr>
 */
public class OntologiesMap {

    /**
     * key and value are resource. paths values must be keys for RESOURCES or
     * ONTO_RESOURCES
     */
    private final Map<String, String> REDIRECTIONS = new HashMap<>();

    /**
     * key is an ontology name
     */
    private final Map<String, VersionedOntology> ONTO_RESOURCES = new HashMap<>();

    public void registerOntology(VersionedOntology ontology) throws OntopException {
        registerVersionUri(ontology);
        boolean isMostRecent = registerMainUri(ontology);
        registerResources(ontology, isMostRecent);
    }

    private void registerVersionUri(VersionedOntology ontology) throws OntopException {
        String versionPath = ontology.getVersionPath();
        if (ONTO_RESOURCES.containsKey(versionPath)) {
            throw new OntopException("An ontology with version URI " + versionPath + " already exists.");
        } else {
            ONTO_RESOURCES.put(consolidate(versionPath), ontology);
        }

    }

    private boolean registerMainUri(VersionedOntology ontology) throws OntopException {
        String ontologyPath = ontology.getOntologyPath();
        VersionedOntology other = ONTO_RESOURCES.get(ontologyPath);
        if (other == null || ontology.compareVersions(other) > 0) {
            ONTO_RESOURCES.put(consolidate(ontologyPath), ontology);
            return true;
        }
        return false;
    }

    private void registerResources(VersionedOntology ontology, boolean mostRecent) {
        for (String resource : ontology.getDefinedResources()) {
            String primaryResource = getPrimaryResourceUri(resource);
            if (mostRecent || !REDIRECTIONS.containsKey(primaryResource)) {
                REDIRECTIONS.put(primaryResource, ontology.getVersionPath());
            }
        }
    }

    public Map<String, String> getRedirections() {
        return REDIRECTIONS;
    }


    public Map<String, VersionedOntology> getOntologyResources() {
        return ONTO_RESOURCES;
    }

    private String getPrimaryResourceUri(String resourceUri) {
        int pos = resourceUri.indexOf("#");
        if (pos != -1) {
            return resourceUri.substring(0, pos);
        } else {
            return resourceUri;
        }
    }

    private String consolidate(String path) {
        if (path.equals("") || path.endsWith("/")) {
            return path + "index";
        }
        return path;
    }

}