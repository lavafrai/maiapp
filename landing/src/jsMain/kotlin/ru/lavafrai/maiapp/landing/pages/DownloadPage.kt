package ru.lavafrai.maiapp.landing.pages

import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.p


val DownloadPage = FC<Props> {
    div {
        h1 { +"Скачать приложение" }
        p {
            +"Вы можете скачать последнюю версию здесь: "
            a {
                href = "/downloads/maiapp-latest.zip"
                +"Скачать ZIP"
            }
        }
    }
}
