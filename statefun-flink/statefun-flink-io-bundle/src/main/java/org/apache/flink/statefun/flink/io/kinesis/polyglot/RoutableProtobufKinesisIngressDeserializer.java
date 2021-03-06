/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.statefun.flink.io.kinesis.polyglot;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import java.util.Map;
import org.apache.flink.statefun.flink.io.generated.AutoRoutable;
import org.apache.flink.statefun.flink.io.generated.RoutingConfig;
import org.apache.flink.statefun.sdk.kinesis.ingress.IngressRecord;
import org.apache.flink.statefun.sdk.kinesis.ingress.KinesisIngressDeserializer;

public final class RoutableProtobufKinesisIngressDeserializer
    implements KinesisIngressDeserializer<Message> {

  private static final long serialVersionUID = 1L;

  private final Map<String, RoutingConfig> routingConfigs;

  RoutableProtobufKinesisIngressDeserializer(Map<String, RoutingConfig> routingConfigs) {
    if (routingConfigs == null || routingConfigs.isEmpty()) {
      throw new IllegalArgumentException(
          "Routing config for routable Kinesis ingress cannot be empty.");
    }
    this.routingConfigs = routingConfigs;
  }

  @Override
  public Message deserialize(IngressRecord ingressRecord) {
    final String stream = ingressRecord.getStream();

    final RoutingConfig routingConfig = routingConfigs.get(stream);
    if (routingConfig == null) {
      throw new IllegalStateException(
          "Consumed a record from stream [" + stream + "], but no routing config was specified.");
    }

    return AutoRoutable.newBuilder()
        .setConfig(routingConfig)
        .setId(ingressRecord.getPartitionKey())
        .setPayloadBytes(ByteString.copyFrom(ingressRecord.getData()))
        .build();
  }
}
