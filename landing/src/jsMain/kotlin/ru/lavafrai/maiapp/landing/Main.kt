package ru.lavafrai.maiapp.landing

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import react.create
import react.dom.client.createRoot
import web.dom.Element


fun main() {
    val container = document.getElementById("root") as? Element ?: error("Root element not found")
    val root = createRoot(container)
    root.render(LandingApp.create())
}
