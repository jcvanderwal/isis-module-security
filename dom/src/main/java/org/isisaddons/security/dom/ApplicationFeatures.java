/*
 *  Copyright 2014 Jeroen van der Wal
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.security.dom;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;

@DomainService
public class ApplicationFeatures extends AbstractFactoryAndRepository {

    @Programmatic
    public List<ApplicationFeature> allFeatures() {
        return allMatches(new QueryDefault<ApplicationFeature>(ApplicationFeature.class, "allByName"));
    }
    
    @Programmatic
    public List<ApplicationFeature> findByPackageName(String packageName) {
        return allMatches(new QueryDefault<ApplicationFeature>(ApplicationFeature.class, "findByPackageName", "packageName", packageName));
    }

    @Programmatic
    public ApplicationFeature findFeatureByName(String name) {
        return firstMatch(new QueryDefault<ApplicationFeature>(ApplicationFeature.class, "findByName", "name", name));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Programmatic
    public List<String> findPackageName() {
        return allMatches(new QueryDefault(ApplicationFeature.class, "findPackageName"));
    }

}
