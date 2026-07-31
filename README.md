# WeatherApp

An Android weather application built with Kotlin, Jetpack Compose, Retrofit, and WeatherAPI.com.

## Requirements

- Android Studio Koala or newer
- JDK 11 or newer
- A WeatherAPI.com API key

## Configure the API key

The API key is intentionally not stored in the repository. Add it to your user-level Gradle properties file:

- Windows: `%USERPROFILE%\\.gradle\\gradle.properties`
- macOS/Linux: `~/.gradle/gradle.properties`

Add this line, replacing the placeholder with your own key:

```properties
WEATHER_API_KEY=your_weatherapi_key_here
```

Alternatively, pass `-PWEATHER_API_KEY=your_weatherapi_key_here` to a Gradle command.

> Important: API keys included in a client Android application can be extracted from the APK. Restrict the key in the WeatherAPI.com dashboard and monitor usage. For production applications, proxy requests through a backend you control.

## Build and run

1. Open the project in Android Studio.
2. Configure `WEATHER_API_KEY` as described above.
3. Sync Gradle and run the `app` configuration on an emulator or device.

From a terminal, the debug APK can be built with:

```text
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`.

## Project structure

- `app/src/main/java`: application, UI, view model, and Retrofit API code
- `app/src/main/res`: Android resources and launcher assets
- `gradle/libs.versions.toml`: dependency versions

## License

No license has been selected yet. Add a license before accepting external contributions or redistributing the project.