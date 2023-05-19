from fastapi import FastAPI, Request, File, UploadFile
import uvicorn
import pickle

app = FastAPI()

"""
@app.get("/hello")
def hello_world():
    return {"message": "Hello World!"}
"""

@app.post("/load_pickle")
def load_pickle(request: Request):
    data = request.json()
    file_name = data["pickleFiles/model1_pickle"]
    with open(file_name, "rb") as f:
        data = pickle.load(f)
    return data

if __name__ == "__main__":
    uvicorn.run(app, host="192.168.1.107", port = 8000)