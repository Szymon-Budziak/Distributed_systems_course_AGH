from fastapi import FastAPI, Request, HTTPException, Form
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
import json
import requests
import os
from dotenv import load_dotenv

load_dotenv()

app = FastAPI()
templates = Jinja2Templates(directory="templates")

API_KEY_1 = os.getenv("API_KEY_1")
API_KEY_2 = os.getenv("API_KEY_2")
API_KEY_3 = os.getenv("API_KEY_3")


@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    return templates.TemplateResponse("error.html", {"request": request, "error": exc.detail})


@app.exception_handler(Exception)
async def exception_handler(request, exc):
    return templates.TemplateResponse("error.html", {"request": request, "error": str(exc)})


@app.get("/", response_class=HTMLResponse)
async def index(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})


@app.post("/results", response_class=HTMLResponse)
async def results(request: Request, city: str = Form(...)):
    if not city:
        # Redirect to the same page with a flash message
        return templates.TemplateResponse("index.html", {"request": request, "error": "Please provide a city name"})

    # Request data from 1st weather API
    try:
        url = f"https://api.openweathermap.org/data/2.5/weather?q={city}&appid={API_KEY_1}"
        response_1 = requests.get(url)
        response_1.raise_for_status()
        data = json.loads(response_1.content)
        temperature_1 = round(data["main"]["temp"] - 273.15, 2)

    except requests.exceptions.RequestException as e:
        raise HTTPException(
            status_code=500, detail="Error while fetching data from OpenWeather") from e

    except (KeyError, ValueError) as e:
        raise HTTPException(
            status_code=500, detail="Invalid response from OpenWeather API") from e

    # Request data from 2nd weather API
    try:
        url = f"http://api.weatherapi.com/v1/current.json?key={API_KEY_2}&q={city}&aqi=no"
        response_2 = requests.get(url)
        response_2.raise_for_status()
        data = json.loads(response_2.content)
        temperature_2 = data["current"]["temp_c"]

    except requests.exceptions.RequestException as e:
        raise HTTPException(
            status_code=500, detail="Error while fetching data from WeatherAPI") from e

    except (KeyError, ValueError) as e:
        raise HTTPException(
            status_code=500, detail="Invalid response from WeatherAPI") from e

    # Request data from 3rd weather API
    try:
        url = f"https://api.tomorrow.io/v4/weather/realtime?location={city}&apikey={API_KEY_3}"
        response_3 = requests.get(url)
        response_3.raise_for_status()
        data = json.loads(response_3.content)
        temperature_3 = data["data"]["values"]["temperature"]

    except requests.exceptions.RequestException as e:
        raise HTTPException(
            status_code=500, detail="Error while fetching data from WeatherAPI") from e

    except (KeyError, ValueError) as e:
        raise HTTPException(
            status_code=500, detail="Invalid response from WeatherAPI") from e

    max_temperature = max(temperature_1, temperature_2, temperature_3)
    min_temperature = min(temperature_1, temperature_2, temperature_3)
    context = {
        "request": request,
        "city": city,
        "temperature_1": temperature_1,
        "temperature_2": temperature_2,
        "temperature_3": temperature_3,
        "avg_temperature": round((temperature_1 + temperature_2 + temperature_3) / 3, 2),
        "max_temperature": max_temperature,
        "min_temperature": min_temperature,
        "diff_temperature": round(abs(max_temperature - min_temperature), 2)
    }
    return templates.TemplateResponse("results.html", context=context)


if __name__ == "__main__":
    pass
