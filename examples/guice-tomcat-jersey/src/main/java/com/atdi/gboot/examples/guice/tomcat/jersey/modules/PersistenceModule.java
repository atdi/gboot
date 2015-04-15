/*
 * Copyright 2015 the original author or authors.
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
package com.atdi.gboot.examples.guice.tomcat.jersey.modules;

import com.atdi.gboot.examples.guice.tomcat.jersey.model.Role;
import com.atdi.gboot.examples.guice.tomcat.jersey.model.User;
import com.atdi.gboot.examples.guice.tomcat.jersey.services.dao.GenericDAO;
import com.atdi.gboot.examples.guice.tomcat.jersey.services.dao.JpaRoleDAO;
import com.atdi.gboot.examples.guice.tomcat.jersey.services.dao.JpaUserDAO;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

/**
 * Persistence module binder.
 */
public class PersistenceModule extends AbstractModule {

    private final String persistenceUnit;

    public PersistenceModule(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    @Override
    protected void configure() {
        install(new JpaPersistModule(persistenceUnit));
        bind(JpaInitializer.class).asEagerSingleton();
        bind(new TypeLiteral<GenericDAO<User>>() {
        }).to(new TypeLiteral<JpaUserDAO>() {
        });
        bind(new TypeLiteral<GenericDAO<Role>>() {
        }).to(new TypeLiteral<JpaRoleDAO>() {
        });
    }
}
