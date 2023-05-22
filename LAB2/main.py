from fastapi import FastAPI, Request, File, UploadFile
from pydantic import BaseModel
from joblib import load, dump
import numpy as np
import uvicorn
import pickle
import sklearn


app = FastAPI()
model1 = load("LAB2\joblibFiles\model1.joblib")

class InputData(BaseModel):
    # Define the necessary fields for your input data
    speed: str
    action: int
    # Add more fields as required

#with open("LAB2\pickleFiles\model1_pickle.pickle", "rb") as f:
#    model = pickle.load(f)


@app.post("/predict")
def predict(data: InputData):
    # Extract the required fields from the input data
    speed = data.field1
    action = data.field2

    # Perform any necessary preprocessing on the input data

    # Make the prediction using the loaded model
    prediction = model1.predict([[speed, action]])  # Adjust based on your model's input format

    # Process and return the prediction result
    return {"prediction": prediction}

if __name__ == "__main__":
    uvicorn.run(app, host="10.22.113.42", port = 8000)