# Groww Stocks App

## Project Overview

This is an Android application for a stocks broking platform, developed as a coding assignment for the App Intern role at Groww. The app allows users to explore top gaining and losing stocks, manage watchlists, and view detailed stock information with price graphs.

## Features

* **Explore Screen**:
    * Displays "Top Gainers" and "Top Losers" sections in a grid layout.
    * Each stock is represented by a card with essential information.
    * Includes a "View All" option to navigate to a complete list of stocks for each category.
    * Features a dynamic theme toggle for switching between light and dark modes.
* **Watchlist**:
    * Shows a list of user-created watchlists.
    * Provides an empty state message if no watchlists are present.
    * Allows users to create new watchlists and delete existing ones.
* **Stock Details Screen**:
    * Presents basic information about a selected stock/ETF, including a line graph of its price history.
    * Users can add or remove stocks from their watchlist directly from this screen.
    * The watchlist icon dynamically changes to reflect whether the stock has been added.
* **Add to Watchlist Popup**:
    * Enables users to add a stock to a new or existing watchlist.
    * Watchlist data is saved and displayed across the app.
* **View All Screen**:
    * Displays a complete list of stocks from the "Top Gainers" and "Top Losers" sections.
* **Search Functionality**:
    * Allows users to search for stocks and ETFs.
* **News Feed**:
    * Displays the latest news related to stocks.

## Technologies Used

* **Kotlin**: The application is built entirely in Kotlin.
* **Jetpack Compose**: The UI is built using Jetpack Compose, a modern toolkit for building native Android UI.
* **Hilt**: Hilt is used for dependency injection to manage dependencies in a scalable and maintainable way.
* **Retrofit**: Retrofit is used for making network requests to the Alpha Vantage API.
* **Room**: Room is used for local data persistence, specifically for caching API responses and managing watchlist data.
* **Coroutines**: Coroutines are used for managing background threads and handling asynchronous operations.
* **Jetpack ViewModel**: ViewModel is used to store and manage UI-related data in a lifecycle-conscious way.
* **Jetpack LiveData**: LiveData is used to observe data changes and update the UI accordingly.
* **MPAndroidChart**: A third-party library is used for displaying line graphs of stock prices.

## Architecture

The app follows a well-defined folder structure and architecture:

* **`data`**:
    * **`local`**: Contains the Room database, DAOs, and entities for local data storage.
    * **`remote`**: Includes the Retrofit API service for network requests.
    * **`repository`**: Implements the repository pattern to abstract data sources.
* **`di`**: Contains Hilt modules for providing dependencies like the database, network service, and repositories.
* **`domain`**:
    * **`usecase`**: Contains use cases that encapsulate specific business logic.
* **`ui`**:
    * Contains the composable functions for each screen of the app, along with their ViewModels.
    * **`theme`**: Includes color schemes, typography, and shapes for the app's theme.
* **`navigation`**: Manages the navigation between different screens using Jetpack Navigation Compose.

## Setup Instructions

1.  **Get an API Key**:
    * You'll need an API key from [Alpha Vantage](https://www.alphavantage.co) to access the endpoints.
    * Generate a free API key from their website.
2.  **Clone the Repository**:
    ```bash
    git clone <your-repository-link>
    ```
3.  **Add API Key**:
    * In the `local.properties` file, add your API key:
        ```properties
        API_KEY="YOUR_API_KEY"
        ```
    * The project is set up to read this key in the `build.gradle` file and make it available through `BuildConfig`.
4.  **Build and Run**:
    * Open the project in Android Studio.
    * Build and run the app on an emulator or a physical device.

## Caching and Optimizations

* **API Response Caching**:
    * The app caches API responses using the Room database with an expiration time to reduce network requests and improve performance.
* **Image Lazy Loading**:
    * Images are loaded asynchronously using Coil, which supports lazy loading and caching.
* **Dynamic Theming**:
    * The app supports both light and dark themes, which can be switched dynamically from the Explore screen.
