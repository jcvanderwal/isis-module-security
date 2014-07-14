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
package org.isisaddons.security.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.isisaddons.security.dom.ApplicationFeature;
import org.isisaddons.security.dom.ApplicationFeatures;
import org.isisaddons.security.dom.ApplicationPermission;
import org.isisaddons.security.dom.ApplicationRole;
import org.isisaddons.security.dom.ApplicationSecurityManager;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.services.devutils.MetaModelRow;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@Named("Security")
@DomainService
public class ApplicationSecurityService extends AbstractFactoryAndRepository implements SpecificationLoaderSpiAware {

    public ApplicationSecurityManager manage() {
        return manage(null);
    }

    @Programmatic
    public ApplicationFeature addFeature(MetaModelRow row) {
        final String name = row.getPackageName().concat(".").concat(row.getClassName()).concat(".").concat(row.getMemberName());

        final ApplicationFeature feature = features.findFeatureByName(name);
        if (feature != null) {
            return feature;
        }
        ApplicationFeature newFeature = newTransientInstance(ApplicationFeature.class);
        newFeature.setName(name);
        newFeature.setClassName(row.getClassName());
        newFeature.setPackageName(row.getPackageName());
        newFeature.setClassType(row.getClassType());
        newFeature.setMemberName(row.getMemberName());
        newFeature.setMemberType(row.getType());

        persist(newFeature);
        return newFeature;
    }

    public List<ApplicationRole> allRoles() {
        return allInstances(ApplicationRole.class);
    }

    public List<ApplicationPermission> permisionsForRole(ApplicationRole role) {
        return allInstances(ApplicationPermission.class);
    }

    public List<ApplicationPermission> permisionsForFeature(ApplicationFeature feature) {
        return allInstances(ApplicationPermission.class);
    }

    @PostConstruct
    @Programmatic
    public void init(Map<String, String> properties) {
        
    }

    public void loadMetaModel() {

        final Collection<ObjectSpecification> specifications = specificationLoader.allSpecifications();

        for (ObjectSpecification spec : specifications) {
            if (exclude(spec)) {
                continue;
            }
            final List<ObjectAssociation> properties = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES);
            for (ObjectAssociation property : properties) {
                final OneToOneAssociation otoa = (OneToOneAssociation) property;
                if (exclude(otoa)) {
                    continue;
                }
                addFeature(new MetaModelRow(spec, otoa));
            }
            final List<ObjectAssociation> associations = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.COLLECTIONS);
            for (ObjectAssociation collection : associations) {
                final OneToManyAssociation otma = (OneToManyAssociation) collection;
                if (exclude(otma)) {
                    continue;
                }
                addFeature(new MetaModelRow(spec, otma));
            }
            final List<ObjectAction> actions = spec.getObjectActions(Contributed.INCLUDED);
            for (ObjectAction action : actions) {
                if (exclude(action)) {
                    continue;
                }
                addFeature(new MetaModelRow(spec, action));
            }
        }

    }

    protected boolean exclude(OneToOneAssociation property) {
        return true;
    }

    protected boolean exclude(OneToManyAssociation collection) {
        return true;
    }

    protected boolean exclude(ObjectAction action) {
        return false;
    }

    protected boolean exclude(ObjectSpecification spec) {
        return isBuiltIn(spec) || spec.isAbstract();
    }

    protected boolean isBuiltIn(ObjectSpecification spec) {
        final String className = spec.getFullIdentifier();
        return className.startsWith("java") || className.startsWith("org.joda");
    }

    // //////////////////////////////////////

    public ApplicationSecurityManager manage(ApplicationSecurityManager manager) {
        final String viewModelMemento = manager == null ? null : manager.viewModelMemento();
        return getContainer().newViewModelInstance(ApplicationSecurityManager.class, viewModelMemento);
    }

    // //////////////////////////////////////

    private SpecificationLoaderSpi specificationLoader;

    @Programmatic
    @Override
    public void setSpecificationLoaderSpi(SpecificationLoaderSpi specificationLoader) {
        this.specificationLoader = specificationLoader;
    }
    
    @Inject
    private ApplicationFeatures features;

}
