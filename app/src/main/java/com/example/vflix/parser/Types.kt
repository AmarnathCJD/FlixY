package com.example.vflix.parser

import com.google.gson.annotations.SerializedName

data class TmdbData(
    val adult: Boolean,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    val genres: List<Genre>,
    val id: Int,
    @SerializedName("imdb_id")
    val imdbId: String,
    @SerializedName("original_language")
    val originalLanguage: String,
    @SerializedName("original_title")
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionCompany>,
    @SerializedName("production_countries")
    val productionCountries: List<ProductionCountry>,
    @SerializedName("release_date")
    val releaseDate: String,
    val runtime: Int,
    @SerializedName("spoken_languages")
    val spokenLanguages: List<SpokenLanguage>,
    val title: String,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("credits")
    val credits: Credits,
    @SerializedName("first_air_date")
    var firstAirDate: String,
    @SerializedName("last_air_date")
    var lastAirDate: String,
    val name: String,
    @SerializedName("last_episode_to_air")
    val lastEpisodeToAir: LastEpisodeToAir,
)

data class Genre(
    val id: Int,
    val name: String
)

data class ProductionCompany(
    val id: Int,
    @SerializedName("logo_path")
    val logoPath: String?,
    val name: String,
    @SerializedName("origin_country")
    val originCountry: String
)

data class ProductionCountry(
    @SerializedName("iso_3166_1")
    val iso31661: String,
    val name: String
)

data class SpokenLanguage(
    @SerializedName("english_name")
    val englishName: String,
    @SerializedName("iso_639_1")
    val iso6391: String,
    val name: String
)

data class Credits(
    val cast: List<Cast>,
    //val crew: List<Crew>
)

data class Cast(
    val name: String,
)

data class LastEpisodeToAir(
    val runtime: Int,
)

data class TmdbEpisode(
    val runtime: Int,
    val overview: String,
)