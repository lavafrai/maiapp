# TLS для личного кабинета МАИ

Дополнительный Russian Trusted Root CA разрешён только для `esia.mai.ru` и
`my.mai.ru`. Системные центры сертификации также остаются доверенными.
Проверка имени сервера, цепочки и срока действия сертификата обязательна.
Клиент `HttpClientProvider.myMai` запрещает HTTP; редиректы с HTTPS на HTTP
не выполняются. Учётные данные для проверки TLS не нужны.

## Реализация

- **iOS:** Darwin проверяет `SecTrust` с дополнительным корнем и SSL-политикой
  для имени запрашиваемого сервера. В `iosApp/iosApp/Info.plist` нужны точечные
  исключения ATS `NSExceptionAllowsInsecureHTTPLoads` для двух адресов. Без них
  URLSession возвращает `-1200` даже после успешной проверки `SecTrust`.
  Глобальное исключение ATS не используется; версия TLS и PFS не ослаблены.
  При публикации Apple требует обоснование этих исключений: серверы МАИ
  используют удостоверяющий центр, которого нет в системном хранилище iOS.
- **Android:** MyMai использует Ktor Android / HttpsURLConnection со штатным
  Network Security Config. Манифест библиотеки `shared` добавляет конфигурацию
  в итоговый манифест приложения. XML доверяет дополнительному корню только
  для двух точных адресов, не включая поддомены, и запрещает для них HTTP.
  Собственного Android TrustManager и reflection нет. Минимальная версия
  Android — API 24, где поддерживается Network Security Config.
- **JVM:** Ktor Android также работает на JVM через HttpsURLConnection.
  Дополнительная SSL-фабрика выбирается по URL каждого соединения только
  для двух адресов; HttpsURLConnection проверяет имя сервера. Это применяется
  заново при редиректах, поэтому дополнительное доверие не переносится на
  посторонний адрес.
- **Web:** корнями доверия управляет браузер; код приложения не может добавить
  корень в браузерное хранилище.

## Обновление сертификата и адресов

PEM хранится в `shared/src/commonMain/kotlin/RussianTrustedRootCA.kt`; его копия
для Android — `shared/src/androidMain/res/raw/russian_trusted_root_ca.pem`.
SHA-256 текущего корня:
`d26d2d0231b7c39f92cc738512ba54103519e4405d68b5bd703e9788ca8ecf31`.

При замене проверяйте происхождение нового корня и обновляйте обе копии.
Список адресов должен совпадать в `MyMaiTls.kt`, Android XML и iOS Info.plist.
Проверка согласованности без SDK и доступа к сети:

```sh
python3 scripts/check-mymai-tls-config.py
```

## Проверки

Общие тесты проверяют границы списка адресов, запрет HTTP и редиректов на HTTP:

```sh
./gradlew :shared:jvmTest :shared:lintDebug
```

Живые проверки требуют доступ к серверам МАИ, example.com и badssl.com.
Они проверяют успешный TLS к МАИ и системным CA, отказ при просроченном,
самоподписанном сертификате и несовпадении имени сервера.
Android дополнительно проверяет, что цепочка МАИ не становится доверенной
для другого поддомена МАИ.

```sh
# При открытом Android-эмуляторе; SDK указан через ANDROID_HOME или local.properties.
./gradlew :shared:connectedDebugAndroidTest

# При открытом ARM64 iOS-симуляторе на Apple Silicon.
scripts/check-ios-tls.sh

# JVM; --rerun-tasks нужен при изменении переменной окружения после обычного jvmTest.
MAI_TLS_LIVE_TESTS=1 ./gradlew :shared:jvmTest --rerun-tasks
```

Для выбора конкретного iOS-симулятора задайте `MAI_TLS_SIMULATOR` его UUID.
Скрипт использует тестовый app bundle с production Info.plist и удаляет его
после запуска. Обычный `iosSimulatorArm64Test` не заменяет эту проверку:
тестовый исполняемый файл вне app bundle не воспроизводит политику ATS приложения.
Поэтому живые iOS-проверки без `MAI_TLS_LIVE_TESTS=1` пропускаются.

Справка: [ATS exceptions](https://developer.apple.com/documentation/bundleresources/information-property-list/nsexceptionallowsinsecurehttploads),
[Android Network Security Config](https://developer.android.com/privacy-and-security/security-config).
