# Weather API App

This is a Python FastAPI application that fetches and displays temperature data from three different weather APIs (
OpenWeather, WeatherAPI, and Tomorrow.io) for a given city.

## Installation

Use the package manager pip to install the required packages.

```bash
pip install -r requirements.txt
```

## Usage

To use this application, you need to set up environment variables for the API keys for the three weather APIs. Rename
the **.env.sample** file to **.env** and add your API keys.

```bash
mv .env.sample .env
```

Then start the application by running:

```bash
uvicorn main:app --reload
```

You can then access the application by visiting http://localhost:8000 in your web browser.

## API Endpoints

The following endpoints are available:

### GET /

This is the homepage of the application, where you can enter a city name.

### POST /results

This endpoint is used to fetch temperature data from the three weather APIs for the specified city and display it in a
results page. Then values are compared between each other.

Credits
This application was created by Szymon Budziak.