/*
 *  Copyright 2019 ABSA Group Limited
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package za.co.absa.hyperdrive.ingestor.implementation.decoder.avro.confluent

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.DataStreamReader
import za.co.absa.abris.avro.read.confluent.SchemaManager
import za.co.absa.abris.avro.schemas.policy.SchemaRetentionPolicies.SchemaRetentionPolicy
import za.co.absa.hyperdrive.ingestor.api.decoder.StreamDecoder

private[decoder] class ConfluentAvroKafkaStreamDecoder(val topic: String, val schemaRegistrySettings: Map[String,String], val retentionPolicy: SchemaRetentionPolicy) extends StreamDecoder {

  if (StringUtils.isBlank(topic)) {
    throw new IllegalArgumentException("Blank topic.")
  }

  if (schemaRegistrySettings == null) {
    throw new IllegalArgumentException("Null Schema Registry settings received.")
  }

  if (schemaRegistrySettings.isEmpty) {
    throw new IllegalArgumentException("Empty Schema Registry settings received.")
  }

  if (retentionPolicy == null) {
    throw new IllegalArgumentException("Null SchemaRetentionPolicy instance received.")
  }

  private val logger = LogManager.getLogger

  def decode(streamReader: DataStreamReader): DataFrame = {
    if (streamReader == null) {
      throw new IllegalArgumentException("Null DataStreamReader instance received.")
    }

    val schemaRegistryFullSettings = schemaRegistrySettings + (SchemaManager.PARAM_SCHEMA_REGISTRY_TOPIC -> topic)
    logger.info(s"SchemaRegistry settings: $schemaRegistryFullSettings")

    import za.co.absa.abris.avro.AvroSerDe._
    streamReader.fromConfluentAvro(column = "value", None, Some(schemaRegistryFullSettings))(retentionPolicy)
  }
}