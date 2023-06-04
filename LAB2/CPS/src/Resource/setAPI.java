package Resource;

import okhttp3.*;
import java.io.IOException;

public class setAPI {

    public static float returnPrediction(int speed, int station, int skill){

        float prediction = 0;

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType type = MediaType.parse("application/json");

        String msgContent = "{\"speed\": "+ speed +", \"station\": "+ station +", \"skill\": "+ skill +"}";
        RequestBody body = RequestBody.create(type, msgContent);

        Request request = new Request.Builder()
                .url("http://127.0.0.1:8000/predict")
                .method("POST", body)
                .addHeader("Content-Type","application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                int startIndex = responseBody.indexOf(":") + 1;
                int endIndex = responseBody.lastIndexOf("}");

                String floatString = responseBody.substring(startIndex, endIndex).trim();
                prediction = Float.parseFloat(floatString);

                return prediction;
            }

        }  catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
