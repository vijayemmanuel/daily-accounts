package scalajsApp.pages

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.mui.Grid.Lg
import org.rebeam.mui.{Button, Card, DatePicker, FormControl, Grid, InputLabel, MenuItem, OutlinedInput, Select, Snackbar, TextField, Typography}
import org.scalajs.dom
import scalajsApp.components.{ExpenseDaySelect, ExpenseField, ExpenseSnackBar}
import scalajsApp.config.Config
import scalajsApp.diode.{AddFoodExpense, AddTransportExpense, AddUtilityExpense, AppCircuit, AppState, ClearLoadingState, SetLoadingState}
import scalajsApp.models.{Expense, ExpenseRequest, ExpenseResponse, NotifType}
import scalajsApp.router.AppRouter
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._
import scalajsApp.models.NotifType.NotifType

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.Try


object CurrentMonthPanel {

  case class State (var foodExp : Int,
                    var transportExp : Int,
                    var utilityExp: Int,
                    var foodSum : Int,
                    var transportSum : Int,
                    var utilitySum: Int,
                    var dayId: Int,
                    var saveNotifStatus: Boolean,
                    var saveNotifType: NotifType
                   )

  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[AppRouter.Page]
                  )

  class Backend($: BackendScope[Props, State]) {

    val host = Config.AppConfig.apiHost

    val day = new js.Date().getDate()
    val month = new js.Date().getMonth() + 1 // Note the JS month starts from 0
    val year = new js.Date().getFullYear()

    val monthId = year.toString +
      (if (month.toString.length == 1) "0" + month.toString else month.toString)

    def updateState(date : Int) = {

      AppCircuit.dispatch(SetLoadingState())
      // Launch the API
      def getData(): Future[List[Expense]] = {
        println(s"Requesting expenses for $monthId")
        // Note that we have added additional header to enable CORS policy in the request
        dom.ext.Ajax.get(url = s"$host/dev/expense?date=$monthId").map(xhr => {
          val option = decode[ExpenseResponse](xhr.responseText)
          option match {
            case Left(failure) => List(Expense("0", "0", "0", "0"))
            case Right(data) => data.message
          }
        })
      }

      def modPartialState(data: Future[List[Expense]]) = {

        Callback.future(
          data.map { value =>

            val foodOnly = value.map(p => p.Food.toInt)
            var foodSum = foodOnly.fold(0)((a: Int, b: Int) => a + b)

            val transportOnly = value.map(p => p.Transport.toInt)
            var transportSum = transportOnly.fold(0)((a: Int, b: Int) => a + b)

            val utilityOnly = value.map(p => p.Utility.toInt)
            var utilitySum = utilityOnly.fold(0)((a: Int, b: Int) => a + b)

            val target = value.filter(p => p.Date.toInt == date)
            AppCircuit.dispatch(ClearLoadingState())
            //Callback.log("Target  Date " + date + " Values :" + target.Food + " ,"+ target.Transport + " ," + target.Utility) >>
            //  Callback.log("State : Target  Date " + $.state.map(s => s.dayId).runNow() + " Values :" + $.state.map(s => s.foodExp).runNow()+ " ,"+ $.state.map(s => s.transportExp).runNow() + " ," + $.state.map(s => s.utilityExp).runNow()) >>
            $.modState(s => s.copy(foodSum = foodSum,
              transportSum = transportSum,
              utilitySum = utilitySum,
              foodExp = target.map(t => t.Food.toInt).headOption.getOrElse(0),
              transportExp = target.map(t => t.Transport.toInt).headOption.getOrElse(0),
              utilityExp = target.map(t => t.Utility.toInt).headOption.getOrElse(0),
              saveNotifStatus = false))
          }
        )
      }

      modPartialState(getData())
    }

    def onExpenseDayChange(selectedDay: Int): Callback = {
      // Need to pass selectedDay to updateState, as it is not updated within modState
      $.modState(s => s.copy(dayId = selectedDay)) >> updateState(selectedDay)
    }

    def onExpenseValueChange(value: Int, label: String) =
      CallbackTo {
        label match {
          case "Food Amount" => {
            // Dispatch to diode
            //TODO change monthid to datid
            //AppCircuit.dispatch(AddFoodExpense(date = monthId.toInt, food = value))
            // Update the local state so as to propagate to props in children
            $.modState(s => s.copy(foodExp = value)).runNow()
          }
          case "Transport Amount" => {
            // Dispatch to diode
            //TODO change monthid to datid
            //AppCircuit.dispatch(AddTransportExpense(date = monthId.toInt, transport = value))
            // Update the local state so as to propagate to props in children
            $.modState(s => s.copy(transportExp = value)).runNow()
          }
          case "Utility Amount" => {
            // Dispatch to diode
            //TODO change monthid to datid
            //AppCircuit.dispatch(AddUtilityExpense(date = monthId.toInt, utility = value))
            // Update the local state so as to propagate to props in children
            $.modState(s => s.copy(utilityExp = value)).runNow()
          }
        }
      }

    def onSave(e: ReactMouseEvent) = {

      AppCircuit.dispatch(SetLoadingState())

      def saveData() = {
        dom.ext.Ajax.put(url = s"$host/dev/expense",
          data = ExpenseRequest(
            Expense(
              $.state.map(s => s.dayId).runNow().toString,
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

      def modPartialState(data: Future[Expense]) = {
        Callback.future(data.map(value => value match {
          // Handle possible network issues.
          case Expense("-2", "-2", "-2", "-2") =>
            AppCircuit.dispatch(ClearLoadingState())
            $.modState(s => s.copy(saveNotifStatus = true, saveNotifType = NotifType.Error))

          case _ =>
            AppCircuit.dispatch(ClearLoadingState())
            // Return -1 if there was wrong data (non Int) stored in database.
            $.modState(s => s.copy(
              foodExp = Try(value.Food.toInt).toOption.getOrElse(-1),
              transportExp = Try(value.Transport.toInt).toOption.getOrElse(-1),
              utilityExp = Try(value.Utility.toInt).toOption.getOrElse(-1),
              saveNotifStatus = true, saveNotifType = NotifType.Success))
        }) // Recover with error if exception is re thrown.
          .recover { case e: Exception => AppCircuit.dispatch(ClearLoadingState())
            Callback.log(s"ERROR $e") >>
            $.modState(s => s.copy(saveNotifStatus = true, saveNotifType = NotifType.Severe))
          })

        //updateState($.state.map(s => s.dayId).runNow()).delayMs(1000).toCallback
      }

      // IMPORTANT Do not chain any new callbacks in event functions. It behaves really wierd!!
      // Best thing to do is to hook this new callback to other event
      modPartialState(saveData()) //>> updateState($.state.map(s => s.dayId).runNow())

    }

    def mounted: Callback = {
      Callback.log("Mounted Current Month!")
      updateState($.state.map(s => s.dayId).runNow())

    }

    def render(props: Props, state: State): VdomElement = {

      <.div (
        Grid(container = true, direction = Grid.Direction.Column,
          justify = Grid.Justify.Center,
          alignItems = Grid.AlignItems.Center)(
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.Center,
            alignItems = Grid.AlignItems.Center)(
              Typography(align = Typography.Align.Center,color = Typography.Color.Primary)("Select Day : "),
              ExpenseDaySelect (ExpenseDaySelect.Props(day,onExpenseDayChange))
            ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceEvenly,
            //alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4, xs = Grid.Xs._12, md = Grid.Md._6, spacing = Grid.Spacing._24,sm = Grid.Sm._8)(
            ExpenseField(ExpenseField.Props("Food Amount",state.foodExp,onExpenseValueChange, false)),
            ExpenseField(ExpenseField.Props("Cumulative",state.foodSum,onExpenseValueChange, true)),
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceEvenly,
            //alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4, xs = Grid.Xs._12, md = Grid.Md._6, spacing = Grid.Spacing._24,sm = Grid.Sm._8)(
          ExpenseField(ExpenseField.Props("Transport Amount",state.transportExp,onExpenseValueChange, false)),
          ExpenseField(ExpenseField.Props("Cumulative",state.transportSum,onExpenseValueChange, true)),
          ),
          <.br(),
          <.br(),
          Grid(container = true, direction = Grid.Direction.Row,
            justify = Grid.Justify.SpaceEvenly,
            //alignItems = Grid.AlignItems.Center,
            item = true, lg = Grid.Lg._4, xs = Grid.Xs._12, md = Grid.Md._6, spacing = Grid.Spacing._24,sm = Grid.Sm._8)(
          ExpenseField(ExpenseField.Props("Utility Amount",state.utilityExp,onExpenseValueChange, false)),
          ExpenseField(ExpenseField.Props("Cumulative",state.utilitySum,onExpenseValueChange, true)),
          ),
          <.br(),
          <.br(),
          Button(variant =  Button.Variant.Contained,color = Button.Color.Primary,onClick = onSave _,disabled = props.proxy().isLoading)(
            VdomNode("Save")),
            ExpenseSnackBar(ExpenseSnackBar.Props(state.saveNotifStatus, state.saveNotifType))
        )
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("CurrentMonthPage")
    .initialState {
      val day = new js.Date().getDate()
      val month = new js.Date().getMonth() + 1 // Note the JS month starts from 0
      val year = new js.Date().getFullYear()

      val dayId = year.toString +
        (if (month.toString.length == 1) "0" + month.toString else month.toString) +
        (if (day.toString.length == 1) "0" + day.toString else day.toString)

      // TODO : Get the udpates staus for APp Circuit
      State(0, 0, 0, 0, 0, 0, dayId.toInt, false, NotifType.Error)
    }
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props)
}
