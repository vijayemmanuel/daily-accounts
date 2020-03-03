package scalajsApp.components

import scala.scalajs.js
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.mui.styles.Styles
import scalajsApp.external.icons.Menu
import scalajsApp.config.Config
import org.rebeam.mui.{AppBar, FormLabel, IconButton, MuiThemeProvider, Tab, Tabs, Toolbar, Typography}
import scalajsApp.pages
import scalajsApp.pages.{CurrentMonthPanel, ExpenditurePanel}
import scalajsApp.router.AppRouter
import scalajsApp.router.AppRouter.Page

import js.JSConverters._
import scala.scalajs.js.Dictionary

object TabModel {
  case class DisplayTab (name: String, page: Page, id: String)

  val TabToday = DisplayTab("Today",AppRouter.Expenditure,"two")
  val TabCurrentMonth = DisplayTab("This Month",AppRouter.CurrentMonthExpenses,"three")
  val TabLastMonth = DisplayTab("Last Month",AppRouter.LastMonthExpenses,"one")

  val tabCollection = List(TabToday,TabCurrentMonth,TabLastMonth)

}


object Layout {

  case class Props(ctl: RouterCtl[Page], r : Resolution[Page])

  case class State (selectedTabIdx : String)

  class Backend($: BackendScope[Props, State]){
    val host: String = Config.AppConfig.apiHost

    def onTabChange(e: ReactEvent) = {

      val selectedTab  = TabModel.tabCollection.filter(t => t.name == e.target.textContent).head

      //Chain the callbacks
      // First to update the URL
      $.props.flatMap(p => p.ctl.set(selectedTab.page)) >>
      //Second to update the selected tab state
      $.setState(State(selectedTabIdx = selectedTab.id))
    }

    def mounted: Callback = {
     Callback.log("Mounted")
    }

    def render(props: Props,state: State): VdomElement = {

      val theme = Styles.createMuiTheme(theme = js.Dynamic.literal(("useNextVariants"->true)))

      <.div(
        <.div(
          ^.cls := "container",
          MuiThemeProvider(theme = theme)(
            AppBar(position = AppBar.Position.Static)(
              Toolbar()(
                IconButton(color = IconButton.Color.Inherit)(
                  Menu()
                ),
                Typography(variant = Typography.Variant.H6, color = Typography.Color.Inherit)(
                  "My Account"
                )
              ),
              Tabs(theme = theme,
                value = Some(js.Any.fromString(state.selectedTabIdx)).orUndefined,
                centered = true,
                onChange = onTabChange _)(
                Tab(label = VdomNode(TabModel.TabLastMonth.name),value = Some(js.Any.fromString(TabModel.TabLastMonth.id)).orUndefined),
                Tab(label = VdomNode(TabModel.TabToday.name),value = Some(js.Any.fromString(TabModel.TabToday.id)).orUndefined),
                Tab(label = VdomNode(TabModel.TabCurrentMonth.name),value = Some(js.Any.fromString(TabModel.TabCurrentMonth.id)).orUndefined),
              )
            ),

            // Render the Component resolved by the Router
            props.r.render()

          )
        )
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("Layout")
    .initialState(State(selectedTabIdx = "two"))
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .build

  def apply(props: Props) = Component(props).vdomElement
}
