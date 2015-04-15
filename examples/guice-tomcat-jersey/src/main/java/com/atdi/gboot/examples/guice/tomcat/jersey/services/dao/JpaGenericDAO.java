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
package com.atdi.gboot.examples.guice.tomcat.jersey.services.dao;

import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.UUID;

/**
 * Abstract class implementation of ${@link GenericDAO}
 * for JPA data access
 */
public abstract class JpaGenericDAO<E> implements GenericDAO<E> {

    @Inject
    protected EntityManager entityManager;

    private final Class<E> clazz;

    public JpaGenericDAO(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Transactional
    @Override
    public E create(E entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Transactional
    @Override
    public E update(E entity) {
        return entity;
    }

    @Transactional
    @Override
    public E findById(UUID id) {
        return entityManager.find(clazz, id);
    }

    @Transactional
    @Override
    public void delete(E entity) {
        entityManager.remove(entity);
    }
}
