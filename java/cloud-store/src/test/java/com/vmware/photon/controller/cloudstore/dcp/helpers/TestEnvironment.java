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

package com.vmware.photon.controller.cloudstore.dcp.helpers;

import com.vmware.photon.controller.cloudstore.dcp.CloudStoreDcpHost;
import com.vmware.photon.controller.common.dcp.helpers.dcp.MultiHostEnvironment;
import com.vmware.photon.controller.common.manifest.BuildInfo;

import org.apache.commons.io.FileUtils;
import static org.testng.Assert.assertTrue;

import java.io.File;

/**
 * TestMachine class hosting a DCP host.
 */
public class TestEnvironment extends MultiHostEnvironment<CloudStoreDcpHost> {
  private TestEnvironment(int hostCount) throws Throwable {
    assertTrue(hostCount > 0);
    hosts = new CloudStoreDcpHost[hostCount];
    for (int i = 0; i < hosts.length; i++) {

      String sandbox = generateStorageSandboxPath();
      FileUtils.forceMkdir(new File(sandbox));

      BuildInfo buildInfo = BuildInfo.get(TestCloudStoreModule.class);

      hosts[i] = new CloudStoreDcpHost(BIND_ADDRESS, 0, sandbox, buildInfo);
    }
  }

  /**
   * Create instance of TestEnvironment with specified count of hosts and start all hosts.
   *
   * @return
   * @throws Throwable
   */
  public static TestEnvironment create(int hostCount) throws Throwable {
    TestEnvironment testEnvironment = new TestEnvironment(hostCount);
    testEnvironment.start();
    return testEnvironment;
  }
}
