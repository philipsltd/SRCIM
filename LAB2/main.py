from fastapi import FastAPI, Request, File, UploadFile
from pydantic import BaseModel
from joblib import load, dump
import numpy as np
import uvicorn
import pickle
import sklearn
from sklearn import tree


app = FastAPI()
#model1 = load(r"LAB2/joblibFiles/model1.joblib")
#model2 = load(r"LAB2/joblibFiles/model2.joblib")
#model3 = load(r"LAB2/joblibFiles/model3.joblib")
#model4 = load(r"LAB2/joblibFiles/model4.joblib")

class InputData(BaseModel):
    # Define the necessary fields for your input data
    speed: int
    action: int

model1 = pickle.load(open('model1_pickle.pkl', 'rb'))

@app.get("/")
def read_root():
    return {"message": "Welcome to the API!"}

@app.get("/favicon.ico")
def get_favicon():
    return ""

@app.post("/predict")
def predict(data: InputData):
    # Extract the required fields from the input data
    speed = data.speed
    action = data.action

    # Perform any necessary preprocessing on the input data
    data = [[speed, action]]

    # Make the prediction using the loaded model
    prediction = model1.predict([[50, 2]])  # Adjust based on your model's input format

    # Process and return the prediction result
    return {"prediction": prediction}

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port = 8000)