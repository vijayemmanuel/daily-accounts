package app

import io.circe.generic.auto._
import io.github.mkotsur.aws.handler.Lambda._
import io.github.mkotsur.aws.handler.Lambda
import com.amazonaws.services.lambda.runtime.Context
import io.github.mkotsur.aws.proxy
import io.github.mkotsur.aws.proxy.ProxyResponse
import org.joda.time.DateTime
import org.apache.logging.log4j.{LogManager, Logger}
import ApiModels._
import org.scanamo.error.DynamoReadError

import scala.language.reflectiveCalls

class ScalaHandler extends Lambda[Req, Resp] {

  //val logger: Logger = LogManager.getLogger(getClass)

  override def handle(req: Req, context: Context): Either[Throwable, Resp] = {
    Right(Resp(List(Expense(DateTime.now().toString(),"","",""))))
  }
}

class GetExpenseScalaHandler extends Proxy[Req, Resp] {

  val logger: Logger = LogManager.getLogger(getClass)

  override def handle(input: proxy.ProxyRequest[Req], c: Context): Either[Throwable, ProxyResponse[Resp]] = {

    // Print info from the context object
    logger.info("Function name: " + c.getFunctionName)
    logger.info("Function query parameters: " + input.queryStringParameters)
    logger.info("Function query to lambda: " + input.queryStringParameters.get("date"))

    val service = new DynamoService()

    logger.info("Calling Dynamo Service")
    val queryResult  = service.getExpense(input.queryStringParameters.get("date")).map (exp =>
    exp match {
      case Right(v:DailyExpenses) => Expense(
        v.YearMonth.toString() +  (if (v.Day / 10 >= 1) v.Day.toString else "0"+ v.Day.toString),
        v.Food.toString(),
        v.Transport.toString(),
        v.Utility.toString())
      case Left (e:DynamoReadError) => {
        logger.error(e.toString)
        Expense(e.toString,"","","")
      }
    })
    logger.info(queryResult.asInstanceOf[List[Expense]])
    val responseBodyOption = Some(Resp(queryResult.asInstanceOf[List[Expense]]))
    val headers = Map("Access-Control-Allow-Origin" -> "*")
    Right(ProxyResponse(200,Some(headers),responseBodyOption))

  }
}

class PutExpenseScalaHandler extends Proxy[Req, Resp] {

  val logger: Logger = LogManager.getLogger(getClass)

  override def handle(input: proxy.ProxyRequest[Req], c: Context): Either[Throwable, ProxyResponse[Resp]] = {

    // Print info from the context object
    logger.info("Function name: " + c.getFunctionName)
    logger.info("Function body: " + input.body)

    val service = new DynamoService()

    logger.info("Calling Dynamo Service")
    val date = service.putExpense(input.body.get.in).head
    val queryResult : List[Expense] = date match {
      case Right(v:DailyExpenses) => List(Expense(
        v.YearMonth.toString() +  (if (v.Day / 10 >= 1) v.Day.toString else "0"+ v.Day.toString),
        v.Food.toString(),
        v.Transport.toString(),
        v.Utility.toString()))
      case Left (e:DynamoReadError) => {
        logger.error(e.toString)
        List(Expense("","","",""))

      }
    }

    val responseBodyOption = input.body.map(req => Resp(queryResult.asInstanceOf[List[Expense]]))
    val headers = Map("Access-Control-Allow-Origin" -> "*")
    Right(ProxyResponse(200,Some(headers),responseBodyOption))

  }
}


