![mai app logo](https://maiapp.lavafrai.ru/maiapp.webp)
# Приложение МАИ
[![GitHub release (latest SemVer)][release-badge]][release-url] ![License info badge][license-badge] [![GitHub badge][uptime-badge]][uptime-url]
 > MAI app - приложение с информацией и расписанием для студентов и преподавателей Московского Авиационного Института.

## Функции
 - Отображение расписания и его локальное сохранение
 - Удобный виджет, показывающий ваше расписание (Android)
 - Возможность загрузить несколько расписаний и переключаться между ними
 - Информация о студгородке и карта кампуса
 - Расписание обновляется автоматически при каждом запуске приложения
 - Отзывы на преподавателей, при сотрудничестве с [МАИ.Экслер.ру](https://mai-exler.ru/)
 - Вы можете пометить работы, чтобы не забыть о предстоящих контрольных и проверочных
 - Даже преподаватели могут пользоваться приложением, оно сделано для всех маёвцев!


## Установка
Собранные версии приложения можно найти в нескольких местах, в зависимости от платформы:
 - [Telegram](https://t.me/maiapp3) - актуальные сборки для Android и свежие новости проекта
 - [RuStore](https://www.rustore.ru/catalog/app/ru.lavafrai.maiapp) - автоматическое обновление, если вы пользуетесь RuStore
 - [GitHub Releases][release-url] - архив с актуальными версиями приложения 
 - [Apple App Store](https://apps.apple.com/app/mai-app/id6444168850) - для iOS, но версии появляются с задержкой, так как Apple требует ручной проверки каждой сборки


## Сборка
Если вы хотите собрать приложение самостоятельно, сделать это можно по следующей инструкции:
1. Склонируйте репозиторий: `git clone https://github.com/lavafrai/maiapp`.
2. Скопируйте файл `composeApp/secrets.properties.example` в `composeApp/secrets.properties` и заполните его своими данными.
3. Для сборки под Android, установите переменные среды для сборки:
   - `ANDROID_HOME` - путь к Android SDK
   - `JAVA_HOME` - путь к JDK
   - `ANDROID_KEYSTORE` - путь к вашему keystore-файлу
   - `ANDROID_KEYSTORE_KEY` - имя ключа в keystore
   - `ANDROID_KEYSTORE_PASSWORD` - пароль к keystore
   - `ANDROID_KEYSTORE_KEY_PASSWORD` - пароль к ключу в keystore
4. Запустите сборку с помощью Gradle:
   - Для Android: `./gradlew :composeApp:assemble`
   - Для Windows: `./gradlew :composeApp:packageMsi`
   - Для Debian: `./gradlew :composeApp:packageDeb`
   - Сборка для iOS осуществляется через Xcode.


## Ссылки
 - [Приложение МАИ в Telegram][release-url] - канал с актуальными версиями и новостями
 - [Сайт проекта](https://maiapp.lavafrai.ru/) - подробная информация о проекте
 - [lavafrai.ru](https://lavafrai.ru/) - сайт разработчика приложения


## Поддержка
Если вы хотите поддержать разработку приложения, вы можете сделать это с помощью пожертвования любым из этих способов:
 - [CloudTips](https://pay.cloudtips.ru/p/e930707c) - через CloudTips, с любой карты или через СБП
 - Ton: `UQA2AoComBmDuQ8Q27UaQ9knsjKbwP2onqvcObKitBK40jw9` 
 - USDT (TRC20): `TBWkaiT4MmrTmGQrCm1QR1qEifmjERLava`

[release-url]: https://github.com/lavafrai/maiapp/releases
[release-badge]: https://img.shields.io/github/v/release/lavafrai/maiapp?sort=semver&logo=github&logoColor=959DA5&labelColor=444D56
[license-badge]: https://img.shields.io/github/license/lavafrai/maiapp
[uptime-badge]: https://status.lavafrai.ru/api/badge/10/uptime/24
[uptime-url]: https://status.lavafrai.ru
