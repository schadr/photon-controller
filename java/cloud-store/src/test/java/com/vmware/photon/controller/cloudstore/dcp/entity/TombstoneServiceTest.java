/*
 * Copyright 2015 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, without warranties or
 * conditions of any kind, EITHER EXPRESS OR IMPLIED.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vmware.photon.controller.cloudstore.dcp.entity;

import com.vmware.dcp.common.Operation;
import com.vmware.dcp.common.Service;
import com.vmware.dcp.common.UriUtils;
import com.vmware.photon.controller.common.dcp.BasicServiceHost;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.Field;
import java.util.EnumSet;

/**
 * Tests {@link TombstoneService}.
 */
public class TombstoneServiceTest {

  private BasicServiceHost host;
  private TombstoneService service;

  private TombstoneService.State buildValidStartState() {
    TombstoneService.State state = new TombstoneService.State();
    state.entityId = "entity-id";
    state.entityKind = "entity-kind";
    state.tombstoneTime = System.currentTimeMillis();

    return state;
  }

  /**
   * Dummy test case to make Intellij recognize this as a test class.
   */
  @Test(enabled = false)
  private void dummy() {
  }

  /**
   * Tests for the constructors.
   */
  public class InitializationTest {
    @BeforeMethod
    public void setUp() {
      service = new TombstoneService();
    }

    /**
     * Test that the service starts with the expected options.
     */
    @Test
    public void testServiceOptions() {
      EnumSet<Service.ServiceOption> expected = EnumSet.of(
          Service.ServiceOption.PERSISTENCE,
          Service.ServiceOption.REPLICATION,
          Service.ServiceOption.OWNER_SELECTION,
          Service.ServiceOption.INSTRUMENTATION);
      assertThat(service.getOptions(), is(expected));
    }
  }

  /**
   * Tests for the handleStart method.
   */
  public class HandleStartTest {
    @BeforeMethod
    public void setUp() throws Throwable {
      service = new TombstoneService();
      host = BasicServiceHost.create(BasicServiceHost.BIND_ADDRESS, BasicServiceHost.BIND_PORT,
          null, BasicServiceHost.SERVICE_URI, 10, 10);
    }

    @AfterMethod
    public void tearDown() throws Throwable {
      if (host != null) {
        BasicServiceHost.destroy(host);
      }

      service = null;
    }

    /**
     * Test start of service with valid start state.
     *
     * @throws Throwable
     */
    @Test
    public void testStartState() throws Throwable {
      TombstoneService.State startState = buildValidStartState();

      Operation result = host.startServiceSynchronously(service, startState);
      assertThat(result.getStatusCode(), is(200));

      TombstoneService.State savedState =
          host.getServiceState(TombstoneService.State.class);
      assertThat(savedState.entityId, is(equalTo(startState.entityId)));
      assertThat(savedState.entityKind, is(equalTo(startState.entityKind)));
      assertThat(savedState.tombstoneTime, is(equalTo(startState.tombstoneTime)));
    }

    /**
     * Tests that exception is raised for all fields that expect a positive value.
     *
     * @param fieldName
     * @throws Throwable
     */
    @Test(dataProvider = "NotNullableFields",
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = ".* cannot be null")
    public void testNotNullableFields(String fieldName) throws Throwable {
      TombstoneService.State startState = buildValidStartState();

      Field fieldObj = startState.getClass().getField(fieldName);
      fieldObj.set(startState, null);

      host.startServiceSynchronously(service, startState);
    }

    @DataProvider(name = "NotNullableFields")
    public Object[][] getNotNullableFieldsParams() {
      return new Object[][]{
          {"entityId"},
          {"entityKind"},
      };
    }

    /**
     * Tests that exception is raised for all fields that expect a positive value.
     *
     * @param fieldName
     * @param value
     * @throws Throwable
     */
    @Test(dataProvider = "PositiveFields",
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = ".* must be greater than zero")
    public void testPositiveFields(String fieldName, Object value) throws Throwable {
      TombstoneService.State startState = buildValidStartState();

      Field fieldObj = startState.getClass().getField(fieldName);
      fieldObj.set(startState, value);

      host.startServiceSynchronously(service, startState);
    }

    @DataProvider(name = "PositiveFields")
    public Object[][] getPositiveFieldsData() {
      return new Object[][]{
          {"tombstoneTime", 0L},
          {"tombstoneTime", -10L},
      };
    }
  }

  /**
   * Tests for handlePatch method.
   */
  public class HandlePatchTest {

    @BeforeMethod
    public void setUp() throws Throwable {
      host = BasicServiceHost.create(BasicServiceHost.BIND_ADDRESS, BasicServiceHost.BIND_PORT,
          null, BasicServiceHost.SERVICE_URI, 10, 10);

      TombstoneService.State startState = buildValidStartState();
      host.startServiceSynchronously(new TombstoneService(), startState);
    }

    @AfterMethod
    public void tearDown() throws Throwable {
      if (null != host) {
        BasicServiceHost.destroy(host);
      }
    }

    /**
     * Tests that exception is raised for all fields that are immutable.
     *
     * @throws Throwable
     */
    @Test(dataProvider = "ImmutableFields",
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = ".* is immutable")
    public void testImmutableFields(String field, Object value) throws Throwable {
      TombstoneService.State patch = new TombstoneService.State();

      Field fieldObj = patch.getClass().getField(field);
      fieldObj.set(patch, value);

      Operation op = Operation
          .createPatch(UriUtils.buildUri(host, BasicServiceHost.SERVICE_URI, null))
          .setBody(patch);

      host.sendRequestAndWait(op);
    }

    @DataProvider(name = "ImmutableFields")
    private Object[][] getImmutableFieldsData() {
      return new Object[][]{
          {"entityId", "new-id"},
          {"entityKind", "new-kind"},
          {"tombstoneTime", 10L}
      };
    }
  }
}
