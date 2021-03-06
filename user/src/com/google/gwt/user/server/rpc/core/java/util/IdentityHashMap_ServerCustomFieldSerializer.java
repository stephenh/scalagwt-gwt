/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.user.server.rpc.core.java.util;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.java.util.Map_CustomFieldSerializerBase;
import com.google.gwt.user.server.rpc.ServerCustomFieldSerializer;
import com.google.gwt.user.server.rpc.impl.DequeMap;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;

/**
 * Custom field serializer for {@link java.util.HashMap}.
 */
@SuppressWarnings("rawtypes")
public final class IdentityHashMap_ServerCustomFieldSerializer extends
    ServerCustomFieldSerializer<IdentityHashMap> {

  public static void deserialize(ServerSerializationStreamReader streamReader,
      IdentityHashMap instance, Class<?> instanceClass, DequeMap<Type, Type> resolvedTypes)
      throws SerializationException {
    Map_ServerCustomFieldSerializerBase.deserialize(streamReader, instance, instanceClass,
        resolvedTypes);
  }

  @Override
  public void deserializeInstance(SerializationStreamReader streamReader,
      IdentityHashMap instance) throws SerializationException {
    Map_CustomFieldSerializerBase.deserialize(streamReader, instance);
  }

  @Override
  public void deserializeInstance(ServerSerializationStreamReader streamReader,
      IdentityHashMap instance, Class<?> instanceClass, DequeMap<Type, Type> resolvedTypes)
      throws SerializationException {
    deserialize(streamReader, instance, instanceClass, resolvedTypes);
  }

  @Override
  public void serializeInstance(SerializationStreamWriter streamWriter,
      IdentityHashMap instance) throws SerializationException {
    Map_CustomFieldSerializerBase.serialize(streamWriter, instance);
  }
}
