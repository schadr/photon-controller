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
import com.vmware.dcp.common.ServiceDocument;
import com.vmware.dcp.common.StatefulService;
import com.vmware.dcp.services.common.QueryTask;
import com.vmware.photon.controller.api.UsageTag;
import com.vmware.photon.controller.common.dcp.InitializationUtils;
import com.vmware.photon.controller.common.dcp.OperationUtils;
import com.vmware.photon.controller.common.dcp.PatchUtils;
import com.vmware.photon.controller.common.dcp.ServiceUtils;
import com.vmware.photon.controller.common.dcp.ValidationUtils;
import com.vmware.photon.controller.common.dcp.validation.Immutable;
import com.vmware.photon.controller.common.dcp.validation.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

/**
 * This class implements a DCP micro-service which provides a plain data object
 * representing a port group.
 */
public class PortGroupService extends StatefulService {

  public static final String USAGE_TAGS_KEY =
      QueryTask.QuerySpecification.buildCollectionItemName(PortGroupService.State.FIELD_NAME_USAGE_TAGS);

  public PortGroupService() {
    super(State.class);
    super.toggleOption(ServiceOption.PERSISTENCE, true);
    super.toggleOption(ServiceOption.REPLICATION, true);
    super.toggleOption(ServiceOption.OWNER_SELECTION, true);
    super.toggleOption(ServiceOption.INSTRUMENTATION, true);
  }

  @Override
  public void handleStart(Operation startOperation) {
    ServiceUtils.logInfo(this, "Starting service %s", getSelfLink());
    try {
      State startState = startOperation.getBody(State.class);
      InitializationUtils.initialize(startState);
      validateState(startState);
      startOperation.complete();
    } catch (Throwable t) {
      ServiceUtils.logSevere(this, t);
      if (!OperationUtils.isCompleted(startOperation)) {
        startOperation.fail(t);
      }
    }
  }

  @Override
  public void handlePatch(Operation patchOperation) {
    ServiceUtils.logInfo(this, "Patching service %s", getSelfLink());

    try {
      State startState = getState(patchOperation);

      State patchState = patchOperation.getBody(State.class);
      validatePatchState(startState, patchState);

      PatchUtils.patchState(startState, patchState);
      validateState(startState);

      patchOperation.complete();
    } catch (Throwable t) {
      ServiceUtils.logSevere(this, t);
      if (!OperationUtils.isCompleted(patchOperation)) {
        patchOperation.fail(t);
      }
    }
  }

  @Override
  public ServiceDocument getDocumentTemplate() {
    return ServiceUtils.getDocumentTemplateWithIndexedFields(super.getDocumentTemplate(), State.FIELD_NAME_USAGE_TAGS);
  }

  private void validateState(State currentState) {
    ValidationUtils.validateState(currentState);
  }

  private void validatePatchState(State startState, State patchState) {
    checkNotNull(patchState, "patch can not be null");
    ValidationUtils.validatePatch(startState, patchState);
  }

  /**
   * This class defines the document state associated with a single
   * {@link PortGroupService} instance.
   */
  public static class State extends ServiceDocument {

    public static final String FIELD_NAME_USAGE_TAGS = "usageTags";

    /**
     * This value represents the name of the port group.
     */
    @NotNull
    @Immutable
    public String name;

    /**
     * Parent network id of the port group.
     */
    public String network;

    /**
     * This value represents the usage tags of the port group.
     */
    public List<UsageTag> usageTags;
  }
}
