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

package com.vmware.photon.controller.cloudstore.dcp.task;

import com.vmware.dcp.common.Operation;
import com.vmware.dcp.common.Service;
import com.vmware.dcp.common.TaskState;
import com.vmware.dcp.common.UriUtils;
import com.vmware.dcp.common.Utils;
import com.vmware.photon.controller.cloudstore.dcp.entity.EntityLockService;
import com.vmware.photon.controller.cloudstore.dcp.entity.EntityLockServiceFactory;
import com.vmware.photon.controller.cloudstore.dcp.entity.TaskService;
import com.vmware.photon.controller.cloudstore.dcp.entity.TaskServiceFactory;
import com.vmware.photon.controller.cloudstore.dcp.helpers.TestEnvironment;
import com.vmware.photon.controller.common.dcp.BasicServiceHost;
import com.vmware.photon.controller.common.dcp.ServiceUtils;
import static com.vmware.photon.controller.cloudstore.dcp.entity.TaskService.State.TaskState.COMPLETED;
import static com.vmware.photon.controller.cloudstore.dcp.entity.TaskService.State.TaskState.ERROR;
import static com.vmware.photon.controller.cloudstore.dcp.entity.TaskService.State.TaskState.QUEUED;
import static com.vmware.photon.controller.cloudstore.dcp.entity.TaskService.State.TaskState.STARTED;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.testng.Assert.fail;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests {@link EntityLockCleanerService}.
 */
public class EntityLockCleanerServiceTest {

  private BasicServiceHost host;
  private EntityLockCleanerService service;

  private EntityLockCleanerService.State buildValidStartupState() {
    EntityLockCleanerService.State state = new EntityLockCleanerService.State();
    state.isSelfProgressionDisabled = true;
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
    public void setUp() throws Throwable {
      service = new EntityLockCleanerService();
    }

    /**
     * Test that the service starts with the expected capabilities.
     */
    @Test
    public void testServiceOptions() {
      // Factory capability is implicitly added as part of the factory constructor.
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
      service = new EntityLockCleanerService();
      host = BasicServiceHost.create();
    }

    @AfterMethod
    public void tearDown() throws Throwable {
      if (host != null) {
        BasicServiceHost.destroy(host);
      }

      service = null;
    }

    /**
     * Test start of service.
     *
     * @throws Throwable
     */
    @Test
    public void testStartState() throws Throwable {
      EntityLockCleanerService.State startState = buildValidStartupState();
      Operation startOp = host.startServiceSynchronously(service, startState);
      assertThat(startOp.getStatusCode(), is(200));

      EntityLockCleanerService.State savedState = host.getServiceState(EntityLockCleanerService.State.class);
      assertThat(savedState.documentSelfLink, is(BasicServiceHost.SERVICE_URI));
      assertThat(new BigDecimal(savedState.documentExpirationTimeMicros),
          is(closeTo(new BigDecimal(ServiceUtils.computeExpirationTime(ServiceUtils.DEFAULT_DOC_EXPIRATION_TIME)),
              new BigDecimal(TimeUnit.SECONDS.toMicros(10)))));
    }

    /**
     * Tests that exception is raised for all fields that expect a positive value.
     *
     * @param fieldName
     * @param value
     * @throws Throwable
     */
    @Test(dataProvider = "AutoInitializedFields")
    public void testAutoInitializedFields(String fieldName, Object value) throws Throwable {
      EntityLockCleanerService.State startState = buildValidStartupState();
      Field fieldObj = startState.getClass().getField(fieldName);
      fieldObj.set(startState, null);

      Operation startOp = host.startServiceSynchronously(service, startState);
      assertThat(startOp.getStatusCode(), is(200));

      EntityLockCleanerService.State savedState = host.getServiceState(EntityLockCleanerService.State.class);
      if (fieldObj.getType().equals(TaskState.class)) {
        assertThat(Utils.toJson(fieldObj.get(savedState)), is(Utils.toJson(value)));
      } else {
        assertThat(fieldObj.get(savedState), is(value));
      }
    }

    @DataProvider(name = "AutoInitializedFields")
    public Object[][] getAutoInitializedFieldsParams() {
      TaskState state = new TaskState();
      state.stage = TaskState.TaskStage.STARTED;

      return new Object[][]{
          {"taskState", state},
          {"isSelfProgressionDisabled", false},
          {"unreleasedEntityLocks", 0},
          {"deletedEntityLocks", 0}
      };
    }

    /**
     * Test expiration time settings.
     *
     * @param time
     * @param expectedTime
     * @param delta
     * @throws Throwable
     */
    @Test(dataProvider = "ExpirationTime")
    public void testExpirationTimeInitialization(long time,
                                                 BigDecimal expectedTime,
                                                 BigDecimal delta) throws Throwable {
      EntityLockCleanerService.State startState = buildValidStartupState();
      startState.documentExpirationTimeMicros = time;

      Operation startOp = host.startServiceSynchronously(service, startState);
      assertThat(startOp.getStatusCode(), is(200));

      EntityLockCleanerService.State savedState = host.getServiceState(EntityLockCleanerService.State.class);
      assertThat(new BigDecimal(savedState.documentExpirationTimeMicros), is(closeTo(expectedTime, delta)));
    }

    @DataProvider(name = "ExpirationTime")
    public Object[][] getExpirationTime() {
      long expTime = ServiceUtils.computeExpirationTime(TimeUnit.HOURS.toMillis(1));

      return new Object[][]{
          {
              -10L,
              new BigDecimal(ServiceUtils.computeExpirationTime(ServiceUtils.DEFAULT_DOC_EXPIRATION_TIME)),
              new BigDecimal(TimeUnit.SECONDS.toMicros(10))
          },
          {
              0L,
              new BigDecimal(ServiceUtils.computeExpirationTime(ServiceUtils.DEFAULT_DOC_EXPIRATION_TIME)),
              new BigDecimal(TimeUnit.SECONDS.toMicros(10))
          },
          {
              expTime,
              new BigDecimal(expTime),
              new BigDecimal(0)
          }
      };
    }
  }

  /**
   * Tests for the handlePatch method.
   */
  public class HandlePatchTest {
    EntityLockCleanerService.State serviceState;

    @BeforeMethod
    public void setUp() throws Throwable {
      host = BasicServiceHost.create();

      service = new EntityLockCleanerService();
      serviceState = buildValidStartupState();
      host.startServiceSynchronously(service, serviceState);
    }

    @AfterMethod
    public void tearDown() throws Throwable {
      if (host != null) {
        BasicServiceHost.destroy(host);
      }

      service = null;
    }

    /**
     * Test patch operation with invalid payload.
     *
     * @throws Throwable
     */
    @Test
    public void testInvalidPatch() throws Throwable {
      Operation op = Operation
          .createPatch(UriUtils.buildUri(host, BasicServiceHost.SERVICE_URI, null))
          .setBody("invalid body");

      try {
        host.sendRequestAndWait(op);
        fail("handlePatch did not throw exception on invalid patch");
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage(),
            startsWith("Unparseable JSON body: java.lang.IllegalStateException: Expected BEGIN_OBJECT"));
      }
    }
  }

  /**
   * Tests for end-to-end scenarios.
   */
  public class EndToEndTest {

    private TestEnvironment machine;
    private EntityLockCleanerService.State request;
    private List<String> testSelfLinks = new ArrayList<>();

    @BeforeMethod
    public void setUp() throws Throwable {
      // Build input.
      request = buildValidStartupState();
      request.isSelfProgressionDisabled = false;
    }

    @AfterMethod
    public void tearDown() throws Throwable {
      if (machine != null) {
        machine.stop();
      }
    }

    /**
     * Default provider to control host count.
     *
     * @return
     */
    @DataProvider(name = "hostCount")
    public Object[][] getHostCount() {
      return new Object[][]{
          {1},
          {TestEnvironment.DEFAULT_MULTI_HOST_COUNT}
      };
    }

    /**
     * Tests clean success scenarios.
     *
     * @param hostCount
     * @throws Throwable
     */
    @Test(dataProvider = "Success")
    public void testSuccess(int totalEntityLocks, int unreleasedEntityLocks, int hostCount) throws Throwable {
      machine = TestEnvironment.create(hostCount);
      seedTestEnvironment(machine, totalEntityLocks, unreleasedEntityLocks);

      EntityLockCleanerService.State response = machine.callServiceAndWaitForState(
          EntityLockCleanerFactoryService.SELF_LINK,
          request,
          EntityLockCleanerService.State.class,
          (EntityLockCleanerService.State state) -> state.taskState.stage == TaskState.TaskStage.FINISHED);

      assertThat(response.unreleasedEntityLocks,
          is(Integer.min(unreleasedEntityLocks, EntityLockCleanerService.ENTITY_LOCK_DEFAULT_PAGE_LIMIT)));
      assertThat(response.deletedEntityLocks,
          is(Integer.min(unreleasedEntityLocks, EntityLockCleanerService.ENTITY_LOCK_DEFAULT_PAGE_LIMIT)));
      freeTestEnvironment(machine);
    }

    private void freeTestEnvironment(TestEnvironment machine) throws Throwable {
      for (String selfLink : testSelfLinks) {
        machine.deleteService(selfLink);
      }
    }

    @DataProvider(name = "Success")
    public Object[][] getSuccessData() {
      return new Object[][]{
          {0, 0, 1},
          {2, 0, 1},
          {2, 0, TestEnvironment.DEFAULT_MULTI_HOST_COUNT},
          {5, 5, 1},
          {7, 5, 1},
          {7, 5, 1},
          {7, 5, TestEnvironment.DEFAULT_MULTI_HOST_COUNT},
          // Test cases with entity locks greater than the default page limit.
          {EntityLockCleanerService.ENTITY_LOCK_DEFAULT_PAGE_LIMIT + 1, EntityLockCleanerService
              .ENTITY_LOCK_DEFAULT_PAGE_LIMIT, 1},
          {EntityLockCleanerService.ENTITY_LOCK_DEFAULT_PAGE_LIMIT + 1, EntityLockCleanerService
              .ENTITY_LOCK_DEFAULT_PAGE_LIMIT + 1, 1},
      };
    }

    private void seedTestEnvironment(TestEnvironment env,
                                     int totalEntityLocks,
                                     int unreleasedEntityLocks) throws Throwable {
      for (int i = 0; i < totalEntityLocks; i++) {
        // create task
        TaskService.State newTask = new TaskService.State();
        newTask.entityId = "entity-id" + i;
        newTask.state = (i % 2 == 0) ? STARTED : QUEUED;

        if (i < unreleasedEntityLocks) {
          newTask.state = (i % 2 == 0) ? COMPLETED : ERROR;
        }

        Operation taskOperation = env.sendPostAndWait(TaskServiceFactory.SELF_LINK, newTask);
        TaskService.State createdTask = taskOperation.getBody(TaskService.State.class);
        testSelfLinks.add(createdTask.documentSelfLink);

        // create associated entity lock
        EntityLockService.State entityLock = new EntityLockService.State();
        entityLock.entityId = "entity-id" + i;
        entityLock.taskId = ServiceUtils.getIDFromDocumentSelfLink(createdTask.documentSelfLink);
        Operation entityLockOperation = env.sendPostAndWait(EntityLockServiceFactory.SELF_LINK, entityLock);
        EntityLockService.State createdEntityLock = entityLockOperation.getBody(EntityLockService.State.class);
        testSelfLinks.add(createdEntityLock.documentSelfLink);
      }
    }
  }
}
