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

package com.vmware.photon.controller.api.common.providers;

import com.vmware.photon.controller.api.common.exceptions.external.InvalidEntityException;
import static com.vmware.photon.controller.api.common.Responses.invalidEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * The invalid entity exception mapper is designed to generate a common response format
 * when entity validation checks fail.
 */
@Provider
public class InvalidEntityExceptionMapper implements ExceptionMapper<InvalidEntityException> {

  private static final Logger logger = LoggerFactory.getLogger(InvalidEntityExceptionMapper.class);

  @Override
  public Response toResponse(InvalidEntityException e) {
    logger.debug("InvalidEntityException: {}", e.getErrors());
    return invalidEntity(e);
  }
}
