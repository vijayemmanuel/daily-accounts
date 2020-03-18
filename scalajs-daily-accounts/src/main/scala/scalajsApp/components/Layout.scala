package scalajsApp.components

import diode.react.ModelProxy

import scala.scalajs.js
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.styles.Styles
import scalajsApp.external.icons.MenuIcon
import scalajsApp.config.Config
import org.rebeam.mui.{AppBar, Dialog, DialogContent, FormLabel, IconButton, Menu, MenuItem, MuiThemeProvider, Tab, Tabs, Toolbar, Typography}
import scalajsApp.diode.AppState
import scalajsApp.pages
import scalajsApp.pages.{CurrentMonthPanel, ExpenditurePanel}
import scalajsApp.router.AppRouter
import scalajsApp.router.AppRouter.Page
import scalajsApp.diode.AppCircuit.connect

import js.JSConverters._
import scala.scalajs.js.Dictionary

object LayoutModel {
  case class DisplayTab (name: String, page: Page, id: String)

  val TabToday = DisplayTab("Today",AppRouter.Expenditure,"two")
  val TabCurrentMonth = DisplayTab("This Month",AppRouter.CurrentMonthExpenses,"three")
  val TabLastMonth = DisplayTab("Last Month",AppRouter.LastMonthExpenses,"one")

  val tabCollection = List(TabToday,TabCurrentMonth,TabLastMonth)

  case class MenuItemName(name: String, page: Option[Page])
  val YearlyMenu = MenuItemName("Yearly Expense", Some(AppRouter.YearlyExpenses))
  val AboutMenu = MenuItemName("About", None)

  val menuCollection = List(YearlyMenu,AboutMenu)


}


object Layout {

  val connection = connect(_.state)

  case class Props(
                    proxy: ModelProxy[AppState],
                    ctl: RouterCtl[Page],
                    r : Resolution[Page]
                  )

  case class State (selectedTabIdx : String,
                    menuHtmlElement : js.Any,
                    showMenu : Boolean,
                    showAbout: Boolean
                   )

  class Backend($: BackendScope[Props, State]){
    val host: String = Config.AppConfig.apiHost

    def onTabChange(e: ReactEvent) = {

      val selectedTab  = LayoutModel.tabCollection.filter(t => t.name == e.target.textContent).head

      //Chain the callbacks
      // First to update the URL
      $.props.flatMap(p => p.ctl.set(selectedTab.page)) >>
      //Second to update the selected tab state
      $.modState(s => s.copy(selectedTabIdx = selectedTab.id))
    }

    def mounted: Callback = {
     Callback.log("Mounted")
    }

    val theme = Styles.createMuiTheme(theme = js.Dynamic.literal())

    def onMenuIconClick(e: ReactMouseEvent) = {
      val tag = e.target
      $.modState(s => s.copy(menuHtmlElement = tag,showMenu = true))
    }

    def handleClose  = {
      $.modState(s => s.copy(menuHtmlElement = null,showMenu = false))
    }

    def onMenuClick(e: ReactMouseEvent) = {
      val menu = e.target.textContent
      menu match {
        case LayoutModel.YearlyMenu.name => {
          $.props.flatMap(p => p.ctl.set(LayoutModel.YearlyMenu.page.get)) >>
          $.modState(s => s.copy(showMenu = false))
        }
        case LayoutModel.AboutMenu.name => {
          $.modState(s => s.copy(showMenu = false,showAbout = true))
        }
      }

    }

    def render(props: Props,state: State): VdomElement = {
      <.div(
        <.div(
          ^.cls := "container",
          MuiThemeProvider(theme = theme)(
            AppBar(position = AppBar.Position.Static)(
              connection(proxy => ExpenseLoadingIndicator(ExpenseLoadingIndicator.Props(proxy))),

                Toolbar()(
                IconButton(color = IconButton.Color.Inherit,onClick = onMenuIconClick _)(
                  MenuIcon()
                ),
                  Menu(open = state.showMenu, anchorEl = state.menuHtmlElement, onClose = handleClose)(
                  MenuItem(onClick = onMenuClick _)(VdomNode(LayoutModel.YearlyMenu.name)),
                  MenuItem(onClick = onMenuClick _)(VdomNode(LayoutModel.AboutMenu.name))
                ),
                Typography(variant = Typography.Variant.H6, color = Typography.Color.Inherit)(
                  "My Daily Expense Tracker"
                )
              ),

              Tabs(theme = theme,
                value = Some(js.Any.fromString(state.selectedTabIdx)).orUndefined,
                centered = true,
                onChange = onTabChange _)(
                Tab(label = VdomNode(LayoutModel.TabLastMonth.name),value = Some(js.Any.fromString(LayoutModel.TabLastMonth.id)).orUndefined),
                Tab(label = VdomNode(LayoutModel.TabToday.name),value = Some(js.Any.fromString(LayoutModel.TabToday.id)).orUndefined),
                Tab(label = VdomNode(LayoutModel.TabCurrentMonth.name),value = Some(js.Any.fromString(LayoutModel.TabCurrentMonth.id)).orUndefined),
              )
            ),

            // Render the Component resolved by the Router
            props.r.render(),
            ExpenseAbout(ExpenseAbout.Props(
              state.showAbout,(e: Boolean) => {
              $.modState(s=> s.copy(showAbout = e)).runNow()
            }
            )
            )
          )
        )
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("Layout")
    .initialState(State(selectedTabIdx = "two",null, false,false))
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props).vdomElement
}
