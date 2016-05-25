/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.admin;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.resource.ClientInitialAccessResource;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.representations.idm.ClientInitialAccessCreatePresentation;
import org.keycloak.representations.idm.ClientInitialAccessPresentation;
import org.keycloak.testsuite.util.AdminEventPaths;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class InitialAccessTokenResourceTest extends AbstractAdminTest {

    private ClientInitialAccessResource resource;

    @Before
    public void before() {
        resource = realm.clientInitialAccess();
    }

    @Test
    public void testInitialAccessTokens() {
        ClientInitialAccessCreatePresentation rep = new ClientInitialAccessCreatePresentation();
        rep.setCount(2);
        rep.setExpiration(100);

        int time = Time.currentTime();

        ClientInitialAccessPresentation response = resource.create(rep);
        assertAdminEvents.assertEvent(realmId, OperationType.CREATE, AdminEventPaths.clientInitialAccessPath(response.getId()), rep);

        assertNotNull(response.getId());
        assertEquals(new Integer(2), response.getCount());
        assertEquals(new Integer(2), response.getRemainingCount());
        assertEquals(new Integer(100), response.getExpiration());
        assertTrue(time <= response.getTimestamp() && response.getTimestamp() <= Time.currentTime());
        assertNotNull(response.getToken());

        rep.setCount(3);
        response = resource.create(rep);
        assertAdminEvents.assertEvent(realmId, OperationType.CREATE, AdminEventPaths.clientInitialAccessPath(response.getId()), rep);

        rep.setCount(4);
        response = resource.create(rep);
        String lastId = response.getId();
        assertAdminEvents.assertEvent(realmId, OperationType.CREATE, AdminEventPaths.clientInitialAccessPath(lastId), rep);

        List<ClientInitialAccessPresentation> list = resource.list();
        assertEquals(3, list.size());

        assertEquals(9, list.get(0).getCount() + list.get(1).getCount() + list.get(2).getCount());
        assertNull(list.get(0).getToken());

        // Delete last and assert it was deleted
        resource.delete(lastId);
        assertAdminEvents.assertEvent(realmId, OperationType.DELETE, AdminEventPaths.clientInitialAccessPath(lastId));

        list = resource.list();
        assertEquals(2, list.size());
        assertEquals(5, list.get(0).getCount() + list.get(1).getCount());
    }

}
