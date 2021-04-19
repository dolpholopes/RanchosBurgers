package com.rlsistemas.ranchosburgers.interface_mp;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ComunicacaoServidorMP {
    @Headers({
            "Content-Type:application/json"
    })
    @POST()
    Call<JsonObject> sendPayment(
            @Url String url,
            @Body JsonObject dados );
}
