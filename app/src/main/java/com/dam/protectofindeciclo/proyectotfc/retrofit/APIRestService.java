package com.dam.protectofindeciclo.proyectotfc.retrofit;

import com.dam.protectofindeciclo.proyectotfc.model.JuegoDetalles;
import com.dam.protectofindeciclo.proyectotfc.model.JuegosEstado;
import com.dam.protectofindeciclo.proyectotfc.model.ResultsBusqueda;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIRestService {
    public static final String BASE_URL = "https://www.giantbomb.com/api/";
    public static final String KEY = "540b7d99b6387edb7ea6d491c2a5948fe6bb1cc7";
    public static final String FORMAT = "json";
    public static final String RESOURCES = "game";
    public static final String FIELD_LIST = "description,genres,image,name,platforms";
    public static final String FILTER = "id:";
    @Headers("content-type: application/json")
    @GET("search/")
    Call<ResultsBusqueda> getJuegos(@Query("api_key") String key, @Query("format")String format,
                                    @Query("resources")String resources, @Query("query")String query);
    @GET("game/{guid}/")
    Call<JuegoDetalles> getInfo(@Path("guid")String guid, @Query("api_key") String key,
                                @Query("field_list")String field_list, @Query("format")String format);

    @GET("games")
    Call<JuegosEstado> getJuegosEstado(@Query("api_key") String key, @Query("format")String format,
                                       @Query("filter")String filter);
}