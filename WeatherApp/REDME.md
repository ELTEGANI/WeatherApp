## Setup (API Key)
1) Get an API key from [OpenWeather](https://openweathermap.org/)

2) Add it to your project `local.properties` (project root):
- `WEATHER_API_KEY=YOUR_KEY_HERE`

> The app reads this value and exposes it as `BuildConfig.WEATHER_API_KEY`.

## Run Tests
From the project root:

- Unit tests:
    - `./gradlew test`

## Code Coverage (JaCoCo)
From the project root:

Generate coverage report:
    - `./gradlew testDebugUnitTest jacocoTestReport`

Outputs:
- `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`

display the report:
- `open app/build/reports/jacoco/jacocoTestReport/html/index.html`

For CI/CD:
- not implement but i would trigger it with each merge into main branch.
- it should run the test and generate the coverage report.
- then dispatch the apk to internal testing channel in play store.

