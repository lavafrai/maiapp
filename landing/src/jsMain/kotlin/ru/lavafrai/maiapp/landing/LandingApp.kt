package ru.lavafrai.maiapp.landing

import js.objects.jso
import react.FC
import react.Props
import react.create
import react.router.RouteObject
import react.router.dom.RouterProvider
import react.router.dom.createBrowserRouter
import ru.lavafrai.maiapp.landing.pages.DownloadPage
import ru.lavafrai.maiapp.landing.pages.HomePage


val routes = arrayOf<RouteObject>(
    jso {
        path = "/"
        element = HomePage.create()
    },
    jso {
        path = "/download"
        element = DownloadPage.create()
    }
)


val LandingApp = FC<Props> {
    RouterProvider {
        this.router = createBrowserRouter(routes)
    }
}
