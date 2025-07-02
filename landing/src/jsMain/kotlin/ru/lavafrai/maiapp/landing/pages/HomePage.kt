package ru.lavafrai.maiapp.landing.pages

import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.p


val HomePage = FC<Props> {
    div {
        h1 { +"Добро пожаловать!" }
        p { +"Это главная страница вашего сайта." }
    }
}
