package com.ddkric.play2plat.api

import com.ddkric.play2plat.UserRegisterResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET("companies")
    fun getCompanies(): Call<List<Company>>

    @GET("sequences")
    fun getSequences(): Call<List<Sequence>>

    @POST("games")
    fun createGame(@Body game: Game): Call<Game>

    @PATCH("games/{id}")
    fun editGame(@Body game: Game, @Path("id") id: Int): Call<Game>

    @DELETE("game-genre/game/{id}")
    fun deleteGameGenres(@Path("id") id: Int): Call<Void>

    /*
    @POST("upload")
    fun uploadImage(@Body imageName: String)



    @POST("upload")
    fun uploadImage(@Body imageName: String): Call<ResponseBody>



    @POST("upload")
    fun uploadImage(@Body imageData: Map<String, String>): Call<ResponseBody>
    */

    @Multipart
    @POST("upload")
    fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("games")
    fun getAllGames(): Call<List<Game>>
    @GET("games/{id}")
    fun getGameById(@Path("id") id: Int): Call<GameInfo>

    @POST("users")
    fun createUser(@Body user: UserRegister): Call<UserRegisterResponse>

    @POST("users/login")
    fun loginUser(@Header("x-api-key") apiKey: String, @Body userLogin: UserLogin): Call<UserLoginResponse>

    // Endpoint de refresh do token
    @POST("users/refresh-token")
    fun refreshToken(@Body refreshTokenBody: RefreshTokenBody): Call<RefreshTokenResponse>
    @POST("some/protected/endpoint")
    fun someProtectedEndpoint(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body requestBody: UserLogin
    ): Call<UserLoginResponse>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: Int): Call<User>

    @GET("genres")
    fun getGenres(): Call<List<Genre>>

    @POST("game-genre")
    fun addGenresToGame(@Body game_genre: GameGenre): Call<GameGenre>

    @POST("upload")
    fun uploadImage(@Body imageName: String)

    // Na sua interface ApiService
    @GET("platforms")
    fun getPlatforms(): Call<List<Platforms>>

    @POST("platform-game")
    fun addPlatformsToGame(@Body game_platform: GamePlatform): Call<GamePlatform>

    @POST("user-platform")
    fun addPlatformsToUser(@Body user_platform: UserPlatform): Call<UserPlatform>

    @PATCH("users/{id}")
    fun updateUser(@Path("id") userId: Int, @Body user: User): Call<User>



    /*
    @GET("companies")
    fun getCompanies(): Call<List<Company>>

    @GET("sequences")
    fun getSequences(): Call<List<Sequence>>

    @GET("genres")
    fun getGenres(): Call<List<Genre>>

    @POST("game-genre")
    fun addGenresToGame(@Body game_genre: GameGenre): Call<GameGenre>

     */

    // User Game Comments

    @POST("user-game-comment")
    fun addComment(@Body comment: Comment): Call<Comment>

    // User Game Favorite
    @GET("user-game-favorite/user/{userId}")
    fun getUserGameFavorites(@Path("userId") userId: Int): Call<List<UserGameFavorite>>

    @POST("user-game-favorite")
    fun addUserGameFavorite(@Body userGameFavorite: UserGameFavorite): Call<UserGameFavorite>

    @DELETE("user-game-favorite/game/{gameId}/user/{userId}")
    fun deleteUserGameFavorite(@Path("gameId") gameId: Int, @Path("userId") userId: Int): Call<Void>

    // User Game

    @GET("user-game/user/{userId}")
    fun getUserGame(@Path("userId") userId: Int): Call<List<UserGame>>

    @POST("user-game")
    fun addUserGame(@Body userGame: UserGame): Call<UserGame>

    @DELETE("user-game/user/{userId}/game/{gameId}")
    fun deleteUserGame(@Path("userId") userId: Int, @Path("gameId") gameId: Int): Call<Void>

    @PATCH("user-game/user/{userId}/game/{gameId}")
    fun updateUserGame(@Path("userId") userId: Int, @Path("gameId") gameId: Int, @Body userGame: UserGame): Call<UserGame>

    // Avaliation
    @GET("avaliation/user/{userId}")
    fun getAvaliation(@Path("userId") userId: Int): Call<List<Avaliation>>

    @POST("avaliation")
    fun addAvaliation(@Body avaliation: Avaliation): Call<Avaliation>

    @DELETE("avaliation/user/{userId}/game/{gameId}")
    fun deleteAvaliation(@Path("userId") userId: Int, @Path("gameId") gameId: Int): Call<Void>

    @PATCH("avaliation/user/{userId}/game/{gameId}")
    fun updateAvaliation(@Path("userId") userId: Int, @Path("gameId") gameId: Int, @Body avaliation: Avaliation): Call<Avaliation>


    // Collections
    @GET("user-game/user/{userId}/state/{state}")
    fun getStateCollection(@Path("userId") userId: Int, @Path("state") state: String): Call<List<Collections>>


    @GET("user-game-favorite/user/{userId}")
    fun getFavoritesByUserId(@Path("userId") userId: Int): Call<List<ListFavoriteGames>>

    @DELETE("user-platform/user/{userId}/platform/{platformId}")
    fun deletePlatformFromUser(@Path("userId") userId: Int, @Path("platformId") platformId: Int): Call<Void>

    @DELETE("platform-game/platform/{platformId}/game/{gameId}")
    fun deletePlatformFromGame(@Path("gameId") gameId: Int, @Path("platformId") platformId: Int): Call<Void>

    @GET("games/searchByName/{name}")
    fun getSearchedGamesByName(@Path("name") name: String): Call<List<Collections>>

    @GET("gamess/descending")
    fun getRecentGames(): Call<List<Collections>>

    @DELETE("users/soft-delete/{userId}")
    fun deleteAccount(@Path("userId") userId: Int): Call<Void>

    @GET("companies/random-name")
    fun getRandomCompany(): Call<Paramater>

    @GET("sequences/random-name")
    fun getRandomSequence(): Call<Paramater>

    @GET("genres/random-name")
    fun getRandomGenre(): Call<Paramater>

    @GET("platforms/random-name")
    fun getRandomPlatform(): Call<Paramater>

    @GET("game-genre/name/{genreName}")
    fun getGamesByGenre(@Path("genreName") genreName: String): Call<List<Collections>>

    @GET("platform-games/name/{platformName}")
    fun getGamesByPlatform(@Path("platformName") platformName: String): Call<List<Collections>>

    @GET("games/sequence/{sequenceName}")
    fun getGamesBySequence(@Path("sequenceName") sequenceName: String): Call<List<Collections>>

    @GET("games/company/{companyName}")
    fun getGamesByCompany(@Path("companyName") companyName: String): Call<List<Collections>>

    @GET("games/same-sequence/{gameId}")
    fun getGamesSameSequence(@Path("gameId") gameId: Int): Call<List<Collections>>

    @GET("games/same-company/{gameId}")
    fun getGamesSameCompany(@Path("gameId") gameId: Int): Call<List<Collections>>

    @GET("user-game-comments-by-user-game/user/{userId}/game/{gameId}")
    fun getPosts(@Path("userId") userId: Int, @Path("gameId") gameId: Int): Call<List<GameCommentsResponse>>

    @GET("user-game-comment-preview/game/{gameId}")
    fun getPostsPreview(@Path("gameId") id: Int): Call<List<GameCommentsResponse>>

    @GET("user-game-comments-responses/post/{postId}")
    fun getAnswers(@Path("postId") userId: Int): Call<List<GameCommentsResponse>>

    @GET("avaliation/average/{gameId}")
    fun getAverageAvaliations(@Path("gameId") gameId: Int): Call<AverageStars>

    @POST("users/verify-password/{userId}")
    fun verifyPassword(@Path("userId") userId: Int, @Body password: Password): Call<Void>

    @POST("games/filter")
    fun getFilteredGames(@Body filter: Filters): Call<List<GameFiltered>>

    @GET("user-game-location-comments/{gameId}")
    fun getPostsByGame(@Path("gameId") gameId: Int): Call<List<GameCommentsResponse>>

    @GET("user-game/user/{userId}/game/{gameId}/state")
    fun getUserGameState(
        @Path("userId") userId: Int,
        @Path("gameId") gameId: Int
    ): Call<UserGameStateResponse>

    @GET("genres/random-names")
    fun getRandomNames(): Call<RandomGenresResponse>

    @DELETE("user-game-comments/{id}")
    fun deleteComment(@Path("id") id: Int): Call<Void>

    @PATCH("user-game-comments/{id}")
    fun updateComment(@Path("id") id: Int, @Body comment: PatchComment): Call<PatchComment>

    @GET("user-game-comment/{id}")
    fun getCommentById(@Path("id") id: Int): Call<Comment>

    @DELETE("games/{id}")
    fun deleteGame(@Path("id") id: Int): Call<Void>
}