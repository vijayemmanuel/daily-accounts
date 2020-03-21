package app

import org.scanamo.{query, _}
import org.scanamo.syntax._
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import org.apache.logging.log4j.{LogManager, Logger}
import org.scanamo.syntax._
import org.scanamo.auto._
import org.scanamo.query.{KeyEquals, Query, UniqueKey}
import org.scanamo._
import org.scanamo.syntax._
import org.scanamo.auto._
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._

import scala.language.existentials

case class DailyExpenses( YearMonth: Long, Day: Long, Food: Long, Transport: Long, Utility: Long)

class DynamoService {

  val logger: Logger = LogManager.getLogger(getClass)

  val awsCreds = new BasicAWSCredentials("XXXXXX","XXXXXXXXX")

  val client = AmazonDynamoDBClient
    .builder()
    //.withRegion(Regions.AP_SOUTH_1)
    //.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    .build()

  val table = Table[DailyExpenses]("DailyExpenses2")


  def getExpense(date: String) = {

    if ( date.length == 8 ) { // First four characters represents year, next two month and finally remaining two day
      val ops = for {
        survivors <- table.query("YearMonth" -> date.substring(0, 6).toLong and "Day" -> date.substring(6, 8).toLong)
        //survivors <- table.scan()
      } yield (survivors)
      Scanamo(client).exec(ops)
    }
    else if ( date.length == 6 ) { // First four characters represents year, next two month
      val ops = for {
        survivors <- table.query("YearMonth" -> date.substring(0, 6).toLong)
        //survivors <- table.scan()
      } yield (survivors)
      Scanamo(client).exec(ops)
    }
    else if ( date.length == 4 ) {
      val ops = for {
        survivors <- table.scan()
      } yield (survivors)
      Scanamo(client).exec(ops)
    }
    else {
        val ops = for {
          survivors <- table.query ("YearMonth" -> 0)
        } yield (survivors)
        Scanamo(client).exec(ops)
      }

  }

  def putExpense(item: Expense) = {

    item.Date.substring(0,5)
    val ops = for {
      _ <- table.put(DailyExpenses(
        item.Date.substring(0,6).toLong,
        item.Date.substring(6,8).toLong,
        item.Food.toLong,
        item.Transport.toLong,
        item.Utility.toLong))
      survivors <- table.query("YearMonth" -> item.Date.substring(0,6).toLong and "Day" -> item.Date.substring(6,8).toLong)
    } yield (survivors)


    Scanamo(client).exec(ops)
  }
}
