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

package com.vmware.photon.controller.apife.backends;

import com.vmware.photon.controller.api.Image;
import com.vmware.photon.controller.api.ImageCreateSpec;
import com.vmware.photon.controller.api.ImageReplicationType;
import com.vmware.photon.controller.api.ImageState;
import com.vmware.photon.controller.api.Task;
import com.vmware.photon.controller.api.common.exceptions.external.ExternalException;
import com.vmware.photon.controller.apife.entities.ImageEntity;
import com.vmware.photon.controller.apife.entities.TaskEntity;

import com.google.common.base.Optional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * Perform image related operations.
 */
public interface ImageBackend {

  ImageEntity deriveImage(ImageCreateSpec imageCreateSpec, ImageEntity originalImage);

  TaskEntity prepareImageUpload(InputStream inputStream, String imageFileName,
                                ImageReplicationType replicationType) throws ExternalException;

  TaskEntity prepareImageDelete(String id)
      throws ExternalException;

  void tombstone(ImageEntity image) throws ExternalException;

  void updateState(ImageEntity imageEntity, ImageState state) throws ExternalException;

  void updateSize(ImageEntity imageEntity, Long size) throws ExternalException;

  List<ImageEntity> getAll() throws ExternalException;

  Image toApiRepresentation(String id) throws ExternalException;

  List<Image> getListApiRepresentation() throws ExternalException;

  void updateSettings(ImageEntity imageEntity, Map<String, String> imageSettings) throws ExternalException;

  List<Task> getTasks(String id, Optional<String> state) throws ExternalException;

  ImageEntity findById(String id) throws ExternalException;
}
