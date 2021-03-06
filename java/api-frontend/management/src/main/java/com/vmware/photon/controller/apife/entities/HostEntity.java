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

import com.vmware.photon.controller.api.Host;
import com.vmware.photon.controller.api.HostState;
import com.vmware.photon.controller.api.common.entities.base.BaseEntity;
import com.vmware.photon.controller.api.constraints.DomainOrIP;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Host entity.
 */
@Entity(name = "Host")
@NamedQueries({
    @NamedQuery(
        name = "Host.listAll",
        query = "SELECT h FROM Host h"
    ),
    @NamedQuery(
        name = "Host.listAllByUsage",
        query = "SELECT h FROM Host h WHERE h.usageTags LIKE :usageTag"
    ),
})
public class HostEntity extends BaseEntity {
  public static final String KIND = Host.KIND;

  @Enumerated(EnumType.STRING)
  private HostState state;

  @DomainOrIP
  private String address;

  @NotBlank
  private String username;

  @NotBlank
  private String password;

  private String availabilityZone;

  @NotNull
  @ElementCollection(fetch = FetchType.EAGER)
  @Cascade(CascadeType.ALL)
  private Map<String, String> metadata = new HashMap<String, String>();

  @NotNull
  private String usageTags;

  private String esxVersion;

  @Override
  public String getKind() {
    return KIND;
  }

  public HostState getState() {
    return state;
  }

  public void setState(HostState state) {
    this.state = state;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAvailabilityZone() {
    return availabilityZone;
  }

  public void setAvailabilityZone(String availabilityZone) {
    this.availabilityZone = availabilityZone;
  }

  public String getUsageTags() {
    return usageTags;
  }

  public void setUsageTags(String usageTags) {
    this.usageTags = usageTags;
  }

  public String getEsxVersion() {
    return esxVersion;
  }

  public void setEsxVersion(String esxVersion) {
    this.esxVersion = esxVersion;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  @Override
  protected com.google.common.base.Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("address", address)
        .add("username", username)
        .add("password", password)
        .add("availabilityZone", availabilityZone)
        .add("esxVersion", esxVersion)
        .add("usageTags", usageTags)
        .add("metadata", metadata);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof HostEntity)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    HostEntity that = (HostEntity) o;

    return Objects.equals(username, that.username)
        && Objects.equals(password, that.password)
        && Objects.equals(availabilityZone, that.availabilityZone)
        && Objects.equals(esxVersion, that.esxVersion)
        && Objects.equals(address, that.address)
        && Objects.equals(usageTags, that.usageTags)
        && Objects.equals(metadata, that.metadata);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(
        super.hashCode(),
        address,
        username,
        password,
        availabilityZone,
        esxVersion,
        usageTags,
        metadata);
  }
}
