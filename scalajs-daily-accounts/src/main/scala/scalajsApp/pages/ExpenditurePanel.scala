package scalajsApp.pages

import java.time.LocalDateTime

import diode.react.ModelProxy

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.mui.{Button, FormControl, Grid, InputLabel, OutlinedInput, TextField, Typography}
import org.scalajs.dom
import scalajsApp.components.ExpenseField
import scalajsApp.models.{Expense, ExpenseRequest, ExpenseResponse}
import scalajsApp.router.AppRouter
import scalajsApp.config.Config
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._
import scalajsApp.diode.AppState

import scala.concurrent.Future
import scala.scalajs.js
import scala.util.Try


object ExpenditurePanel {

  case class State (foodExp : Int,transportExp : Int, utilityExp: Int)
  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[AppRouter.Page]
                  )

  class Backend($: BackendScope[Props, State]) {

    val host = Config.AppConfig.apiHost
    val date = new js.Date().toDateString()

    val day = new js.Date().getDate()
    val month = new js.Date().getMonth() + 1 // Note the JS month starts from 0
    val year = new js.Date().getFullYear()

    val dayId = year.toString +
      (if(month.toString.length ==1) "0"+ month.toString else month.toString) +
      (if(day.toString.length ==1) "0"+ day.toString else day.toString)


    def updateState = {

      def getData(): Future[ExpenseResponse] = {
        println(s"Requesting expenses for $dayId")
        // Note that we have added additional header to enable CORS policy in the request
        dom.ext.Ajax.get(url=s"$host/dev/expense?date=$dayId").map(xhr => {
          val option = decode[ExpenseResponse](xhr.responseText)
          option match {
            case Left(failure) => ExpenseResponse(Expense("-2","-2","-2","-2"))
            case Right(data) => data
          }
        })
      }

      getData().map { value =>
        $.modState(s => s.copy(foodExp = Try(value.message.Food.toInt).toOption.getOrElse(-1),
          transportExp = Try(value.message.Transport.toInt).toOption.getOrElse(-1),
          utilityExp = Try(value.message.Utility.toInt).toOption.getOrElse(-1))).runNow()
      }
    }

    def onSave(e: ReactMouseEvent)= {
      def saveData() = {
        dom.ext.Ajax.put(url = s"$host/dev/expense",
          data = ExpenseRequest(
            Expense(
              dayId,
              $.state.map(s=> s.foodExp.toString).runNow(),
              $.state.map(s=> s.transportExp.toString).runNow(),
              $.state.map(s=> s.utilityExp.toString).runNow()
            )
          ).asJson.toString).map(xhr => {
          val option = decode[ExpenseResponse](xhr.responseText)
          option match {
            case Left(failure) => ExpenseResponse(Expense("-2","-2","-2","-2"))
            case Right(data) => data
          }
        })

      }
      Callback(saveData().map { value =>
        $.modState(s => s.copy(foodExp = Try(value.message.Food.toInt).toOption.getOrElse(-1),
          transportExp = Try(value.message.Transport.toInt).toOption.getOrElse(-1),
          utilityExp = Try(value.message.Utility.toInt).toOption.getOrElse(-1))).runNow()
      })




    }

    def mounted: Callback = {
      // Call to update the state for current date
      updateState
      Callback.log("Mounted ExpenditurePanel")
    }

    def render(props: Props, state: State): VdomElement = {
      <.div (
        Grid(container = true, direction = Grid.Direction.Column,
        justify = Grid.Justify.Center, alignItems = Grid.AlignItems.Center)(
        <.br(), <.br(),
            Typography(align = Typography.Align.Center,color = Typography.Color.Primary,variant = Typography.Variant.H3)(date),
          <.br(),
          <.br(),
          ExpenseField(ExpenseField.Props(props.proxy, "Food Amount",state.foodExp)),
          <.br(),
          <.br(),
          ExpenseField(ExpenseField.Props(props.proxy, "Transport Amount",state.transportExp)),
          <.br(),
          <.br(),
          ExpenseField(ExpenseField.Props(props.proxy, "Utility Amount",state.utilityExp)),
        <.br(),
          <.br(),
          Button(variant =  Button.Variant.Contained,color = Button.Color.Primary,onClick = onSave _)(VdomNode("Save"))

        )
      )

    }
  }

  val Component = ScalaComponent.builder[Props]("ExpenditurePage")
    .initialState(State(0,0,0))
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props)
}
