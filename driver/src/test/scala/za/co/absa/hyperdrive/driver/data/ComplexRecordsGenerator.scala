/*
 * Copyright 2018-2019 ABSA Group Limited
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

package za.co.absa.hyperdrive.driver.data

import java.lang._
import java.nio.ByteBuffer

import org.apache.avro.generic.GenericRecord
import org.apache.spark.sql.{Encoder, Row}
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import za.co.absa.abris.avro.format.SparkAvroConversions
import za.co.absa.abris.avro.parsing.AvroToSparkParser
import za.co.absa.abris.avro.parsing.utils.AvroSchemaUtils

import scala.collection.JavaConverters.{asScalaBufferConverter, seqAsJavaListConverter}
import scala.collection.{Seq, immutable, mutable}
import scala.util.Random

/**
 * This class provides methods to generate example/test data.
 * Not part of the library core.
 */
object ComplexRecordsGenerator {

  case class Bean(bytes: Array[scala.Byte], string: String, int: Int, long: Long, double: Double,
                  float: Float, boolean: Boolean, array: mutable.ListBuffer[Any], fixed: Array[scala.Byte])

  private val plainSchema = TestSchemas.NATIVE_COMPLETE_SCHEMA
  private val avroParser = new AvroToSparkParser()
  private val random = new Random()

  def usedAvroSchema: String = plainSchema

  /**
    * Generates a list of random Avro records.
    */
  def generateRecords(howMany: Int): List[GenericRecord] = {
    val result = new Array[GenericRecord](howMany)
    for (i <- 0 until howMany) {
      result(i) = AvroDataUtils.mapToGenericRecord(getDataMap, plainSchema)
    }
    result.toList
  }

  /**
    * Generates a list of random Spark Rows.
    */
  def generateUnparsedRows(howMany: Int): List[Row] = {
    val result = new Array[Row](howMany)
    for (i <- 0 until howMany) {
      result(i) = Row.fromSeq(getDataSeq)
    }
    result.toList
  }

  /**
    * Gets a Spark Row Encoder for the schema of the records generated by this class.
    * @return
    */
  def getEncoder: Encoder[Row] = {
    val avroSchema = AvroSchemaUtils.parse(ComplexRecordsGenerator.usedAvroSchema)
    val sparkSchema = SparkAvroConversions.toSqlType(avroSchema)
    RowEncoder.apply(sparkSchema)
  }

  private def getDataMap: immutable.Map[String, Object] = {
    immutable.Map[String, Object](
      "bytes" -> ByteBuffer.wrap(randomString(20).getBytes),
      "string" -> randomString(30),
      "int" -> new Integer(random.nextInt()),
      "long" -> new Long(random.nextLong()),
      "double" -> new Double(random.nextDouble()),
      "float" -> new Float(random.nextFloat()),
      "boolean" -> new Boolean(random.nextBoolean()),
      "array" -> randomListOfStrings(10, 15),
      "fixed" -> new FixedString(randomString(40)))
  }

  private def getDataSeq: Seq[Object] = {
    Seq(
      ByteBuffer.wrap(randomString(20).getBytes).array(),
      randomString(30),
      new Integer(random.nextInt()),
      new Long(random.nextLong()),
      new Double(random.nextDouble()),
      new Float(random.nextFloat()),
      new Boolean(random.nextBoolean()),
      randomSeqOfStrings(10, 15),
      new FixedString(randomString(40)).bytes())
  }

  def lazilyGenerateRows(howMany: Int): List[Row] = {
    lazilyConvertToRows(generateRecords(howMany))
  }

  def eagerlyGenerateRows(howMany: Int): List[Row] = {
    eagerlyConvertToRows(generateRecords(howMany))
  }

  def lazilyConvertToBeans(records: List[GenericRecord]): List[Bean] = {
    records.toStream.map(record => recordToBean(record)).toList
  }

  def eagerlyConvertToRows(records: List[GenericRecord]): List[Row] = {
    records.map(record => avroParser.parse(record))
  }

  def lazilyConvertToRows(records: List[GenericRecord]): List[Row] = {
    records.toStream.map(record => avroParser.parse(record)).toList
  }

  private def randomListOfLongs(listSize: Int): java.   util.ArrayList[Long] = {
    val array = new Array[Long](listSize)
    for (i <- 0 until listSize) {
      array(i) = random.nextLong()
    }
    new java.util.ArrayList(array.toList.asJava)
  }
  
  private def randomSeqOfLongs(listSize: Int) = {
    randomListOfLongs(listSize).asScala
  }
  
  private def randomListOfStrings(listSize: Int, stringLength: Int) = {
    val array = new Array[String](listSize)
    for (i <- 0 until listSize) {
      array(i) = randomString(stringLength)
    }
    new java.util.ArrayList(array.toList.asJava)
  }

  private def randomSeqOfStrings(listSize: Int, stringLength: Int) = {
    randomListOfStrings(listSize, stringLength).asScala
  }  
  
  private def randomString(length: Int): String = {
    val randomStream: Stream[Char] = Random.alphanumeric
    randomStream.take(length).mkString
  }

  private def recordToBean(record: GenericRecord): Bean = {    
    Bean(
      record.get("bytes").toString.getBytes(),
      record.get("string").asInstanceOf[String],
      record.get("int").asInstanceOf[Int],
      record.get("long").asInstanceOf[Long],
      record.get("double").asInstanceOf[Double],
      record.get("float").asInstanceOf[Float],
      record.get("boolean").asInstanceOf[Boolean],
      record.get("array").asInstanceOf[mutable.ListBuffer[Any]],
      record.get("fixed").toString.getBytes)
  }
}