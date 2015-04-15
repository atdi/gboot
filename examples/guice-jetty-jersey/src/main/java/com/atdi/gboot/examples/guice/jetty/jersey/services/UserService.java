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
package com.atdi.gboot.examples.guice.jetty.jersey.services;

import com.atdi.gboot.examples.guice.jetty.jersey.model.User;
import com.atdi.gboot.examples.guice.jetty.jersey.services.dao.GenericDAO;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

/**
 * User service class.
 */
@Singleton
public class UserService {

    private final GenericDAO<User> userDAO;

    @Inject
    public UserService(GenericDAO<User> userRepository) {
        this.userDAO = userRepository;
    }

    @Transactional
    public User addUser(User user) {
        return userDAO.create(user);
    }

    @Transactional
    public User updateUser(User user) {
        return userDAO.update(user);
    }

    @Transactional
    public User findById(UUID id) {
        return userDAO.findById(id);
    }

    @Transactional
    public List<User> findUsers(User user) {
        return userDAO.findByCriteria(user);
    }

}
