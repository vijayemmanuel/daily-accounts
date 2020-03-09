package scalajsApp.pages

import java.time.LocalDateTime

import diode.react.ModelProxy

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.mui.{Button, FormControl, Grid, InputLabel, OutlinedInput, Snackbar, SnackbarContent, TextField, Typography}
import org.scalajs.dom
import scalajsApp.components.{ExpenseField, ExpenseSnackBar}
import scalajsApp.models.{Expense, ExpenseRequest, ExpenseResponse, NotifType}
import scalajsApp.router.AppRouter
import scalajsApp.config.Config
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._
import scalajsApp.diode.{AddFoodExpense, AddTransportExpense, AddUtilityExpense, AppCircuit, AppState}
import scalajsApp.models.NotifType.NotifType

import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success, Try}


object ExpenditurePanel {

  case class State (var foodExp : Int,
                    var transportExp : Int,
                    var utilityExp: Int,
                    var saveNotifStatus: Boolean,
                    var saveNotifType: NotifType
                   )
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
      (if (month.toString.length == 1) "0" + month.toString else month.toString) +
      (if (day.toString.length == 1) "0" + day.toString else day.toString)


    def updateState = {

      def getData(): Future[Expense] = {
        println(s"Requesting expenses for $dayId")
        // Note that we have added additional header to enable CORS policy in the request
        dom.ext.Ajax.get(url = s"$host/dev/expense?date=$dayId").map(xhr => {
          val option = decode[ExpenseResponse](xhr.responseText)
          option match {
            case Left(failure) => Expense("-2", "-2", "-2", "-2")
            case Right(data) => data.message.head
          }
        })
      }

      getData().map { value =>
        $.modState(s => s.copy(foodExp = Try(value.Food.toInt).toOption.getOrElse(-1),
          transportExp = Try(value.Transport.toInt).toOption.getOrElse(-1),
          utilityExp = Try(value.Utility.toInt).toOption.getOrElse(-1))).runNow()
      }
    }

    def onSave(e: ReactMouseEvent) = {
      def saveData() = {
        dom.ext.Ajax.put(url = s"$host/dev/expense",
          data = ExpenseRequest(
            Expense(
              dayId,
              $.state.map(s => s.foodExp).runNow().toString,
              $.state.map(s => s.transportExp).runNow().toString,
              $.state.map(s => s.utilityExp).runNow().toString,
            )
          ).asJson.toString).map(xhr => {
          val option = decode[ExpenseResponse](xhr.responseText)
          option match {
            case Left(failure) => Expense("-2", "-2", "-2", "-2")
            case Right(data) => data.message.head
          }
        })
      }

      Callback.future(saveData().map( value =>  value match {
          // Handle possible network issues.
        case Expense("-2", "-2", "-2", "-2") =>
          $.modState (s => s.copy (saveNotifStatus = true, saveNotifType = NotifType.Error))

        case _ =>
          // Return -1 if there was wrong data (non Int) stored in database.
        $.modState (s => s.copy (foodExp = Try (value.Food.toInt).toOption.getOrElse (- 1),
        transportExp = Try (value.Transport.toInt).toOption.getOrElse (- 1),
        utilityExp = Try (value.Utility.toInt).toOption.getOrElse (- 1),
          saveNotifStatus = true, saveNotifType = NotifType.Success) )
      }) // Recover with error if exception is re thrown.
        .recover { case e: Exception =>  Callback.log(s"ERROR $e")  >>
          $.modState (s => s.copy (saveNotifStatus = true, saveNotifType = NotifType.Severe))})
    }

    def onNotifClose  = {
      $.modState(s=> s.copy(saveNotifStatus = false))
    }

    def onExpenseValueChange(value: Int, label: String) =
      CallbackTo {
        label match {
          case "Food Amount" => {
            // Dispatch to diode
            AppCircuit.dispatch(AddFoodExpense(date = dayId.toInt, food = value))
            // Update the local state so as to propagate to props in children
            $.modState(s => s.copy(foodExp = value)).runNow()
          }
          case "Transport Amount" => {
            // Dispatch to diode
            AppCircuit.dispatch(AddTransportExpense(date = dayId.toInt, transport = value))
            // Update the local state so as to propagate to props in children
            $.modState(s => s.copy(transportExp = value)).runNow()
          }
          case "Utility Amount" => {
            // Dispatch to diode
            AppCircuit.dispatch(AddUtilityExpense(date = dayId.toInt, utility = value))
            // Update the local state so as to propagate to props in children
            $.modState(s => s.copy(utilityExp = value)).runNow()
          }

        }
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
          ExpenseField(ExpenseField.Props("Food Amount", state.foodExp,onExpenseValueChange, false)),
          <.br(),
          <.br(),
          ExpenseField(ExpenseField.Props("Transport Amount",state.transportExp,onExpenseValueChange, false)),
          <.br(),
          <.br(),
          ExpenseField(ExpenseField.Props("Utility Amount",state.utilityExp,onExpenseValueChange, false)),
        <.br(),
          <.br(),
          Button(variant =  Button.Variant.Contained,color = Button.Color.Primary,onClick = onSave _)(VdomNode("Save")),
          ExpenseSnackBar(ExpenseSnackBar.Props(state.saveNotifStatus, state.saveNotifType))
        )
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("ExpenditurePage")
    .initialState(State(0,0,0,false,NotifType.Error))
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props)
}
