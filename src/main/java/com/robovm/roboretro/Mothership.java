package com.robovm.roboretro;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface Mothership {
    @GET("/{id}")
    Observable<Response> register( @Path( "id" ) String id );
}
