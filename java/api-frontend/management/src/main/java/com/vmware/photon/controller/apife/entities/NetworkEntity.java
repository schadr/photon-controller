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

package com.vmware.photon.controller.apife.entities;

import com.vmware.photon.controller.api.Network;
import com.vmware.photon.controller.api.NetworkState;
import com.vmware.photon.controller.api.base.Named;
import com.vmware.photon.controller.api.common.entities.base.BaseEntity;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Network entity.
 */
@Entity(name = "Network")
@NamedQueries({
    @NamedQuery(
        name = "Network.listAll",
        query = "SELECT network FROM Network network"
    ),
    @NamedQuery(
        name = "Network.findByName",
        query = "SELECT network FROM Network network WHERE network.name = :name"
    )
})
public class NetworkEntity extends BaseEntity implements Named {

  @NotBlank
  private String name;

  private String description;

  @Enumerated(EnumType.STRING)
  private NetworkState state;

  @NotBlank
  private String portGroups;

  public NetworkEntity() {
  }

  public NetworkEntity(String name, String portGroups) {
    this.name = name;
    this.portGroups = portGroups;
  }

  @Override
  public String getKind() {
    return Network.KIND;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public NetworkState getState() {
    return state;
  }

  public void setState(NetworkState state) {
    if (this.getState() != null && state != null) {
      EntityStateValidator.validateStateChange(this.getState(), state, NetworkState.PRECONDITION_STATES);
    }

    this.state = state;
  }

  public String getPortGroups() {
    return portGroups;
  }

  public void setPortGroups(String portGroups) {
    this.portGroups = portGroups;
  }
}
