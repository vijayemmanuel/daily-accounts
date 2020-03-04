package scalajsApp.router

import diode.react.ModelProxy
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import scalajsApp.components.Layout
import scalajsApp.diode.AppCircuit
import scalajsApp.pages.{CurrentMonthPanel, ExpenditurePanel}


object AppRouter {
  sealed trait Page
  case object LastMonthExpenses extends Page
  case object Expenditure extends Page
  case object CurrentMonthExpenses extends Page


  val connection = AppCircuit.connect(m => m.state)

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._
    (trimSlashes
      | staticRoute(root, Expenditure) ~> renderR(renderExpenditurePage)
      | staticRoute("#currentmonth", CurrentMonthExpenses) ~> renderR(renderCurrentMonthPage)
    )
      .notFound(redirectToPage(Expenditure)(Redirect.Replace))
      .renderWith(layout)
  }

  def renderExpenditurePage(ctl: RouterCtl[Page]) = {
    connection(proxy => ExpenditurePanel(ExpenditurePanel.Props(proxy, ctl)))
  }

  def renderCurrentMonthPage(ctl: RouterCtl[Page]) = {
    connection(proxy => CurrentMonthPanel(CurrentMonthPanel.Props(proxy, ctl)))
  }

  def layout ( c: RouterCtl[Page], r: Resolution[Page]) = connection (proxy => Layout(Layout.Props(proxy, c,r)))

  val baseUrl = BaseUrl.fromWindowOrigin_/

  val router = Router(baseUrl, routerConfig.logToConsole)
}
