from fastapi import FastAPI, Request, File, UploadFile
from pydantic import BaseModel
from joblib import load, dump
import numpy as np
import uvicorn
import pickle
import sklearn
from sklearn import tree

app = FastAPI()
model1 = load("joblibFiles/model1.joblib")
model2 = load("joblibFiles/model2.joblib")
model3 = load("joblibFiles/model3.joblib")
model4 = load("joblibFiles/model4.joblib")

class InputData(BaseModel):
    # Define the necessary fields for your input data
    speed: int
    action: int

@app.get("/")
def read_root():
    return {"message": "Welcome to the API!"}

@app.get("/favicon.ico")
def get_favicon():
    return ""

@app.post("/predict1")
def predict1(data: InputData):
    # Extract the required fields from the input data
    speed = data.speed
    action = data.action

    # Perform any necessary preprocessing on the input data
    data = np.array([[speed, action]])

    # Make the prediction using the loaded model
    prediction = model1.predict(data)  # Adjust based on your model's input format

    # Process and return the prediction result
    return {"prediction": prediction[0]}

@app.post("/predict2")
def predict2(data: InputData):

    speed = data.speed
    action = data.action

    data = np.array([[speed, action]])
    prediction = model2.predict(data)

    return {"prediction": prediction[0]}

@app.post("/predict3")
def predict3(data: InputData):

    speed = data.speed
    action = data.action

    data = np.array([[speed, action]])
    prediction = model3.predict(data)

    return {"prediction": prediction[0]}

@app.post("/predict4")
def predict4(data: InputData):
    
        speed = data.speed
        action = data.action
    
        data = np.array([[speed, action]])
        prediction = model4.predict(data)
    
        return {"prediction": prediction[0]}


if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port = 8000)