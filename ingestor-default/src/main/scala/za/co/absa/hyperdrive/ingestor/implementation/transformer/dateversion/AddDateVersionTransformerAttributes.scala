/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.hyperdrive.ingestor.implementation.transformer.dateversion

import za.co.absa.hyperdrive.ingestor.api.{HasComponentAttributes, PropertyMetadata}

trait AddDateVersionTransformerAttributes extends HasComponentAttributes {

  val KeyReportDate = "report.date"

  override def getName: String = "Add Date Version Transformer"

  override def getDescription: String = "This transformer adds a date and an incrementing version number as columns hyperdrive_date and hyperdrive_version." +
    " hyperdrive_date and hyperdrive_version need to be specified in that order as partition columns for the increment to work." +
    " This transformer depends on a writer which defines writer.parquet.destination.directory"

  override def getProperties: Map[String, PropertyMetadata] = Map(
    KeyReportDate -> PropertyMetadata("Date", Some("Date to add as a column, e.g. 2018-01-23. By default, the current date will be taken."), required = false)
  )
}
