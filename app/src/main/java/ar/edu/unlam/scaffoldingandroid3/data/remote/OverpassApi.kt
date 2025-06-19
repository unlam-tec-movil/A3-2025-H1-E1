package ar.edu.unlam.scaffoldingandroid3.data.remote

import ar.edu.unlam.scaffoldingandroid3.data.remote.dto.OverpassResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OverpassApi {

    @FormUrlEncoded
    @POST("interpreter")
    suspend fun getNearbyRoutes(@Field("data") query: String): OverpassResponse

} 