package com.pk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class SfGateScraper {

    private static final String SFGATE_URL="http://www.sfgate.com/cgi-bin/movies/listings/theatershowtimes?county=San+Francisco";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson GSON = new Gson();

    @SneakyThrows
    public List<Movie> getMovies() {
        List<Movie> movies = Lists.newArrayList();

        Set<String> seen = Sets.newHashSet();

        Document document = Jsoup.connect(SFGATE_URL).get();
        for (Element movie_listing_list : document.getElementsByClass("theater")) {
            String title = movie_listing_list.text();
            if (seen.contains(title)) continue;
            seen.add(title);

            int year = Calendar.getInstance().get(Calendar.YEAR);
            String omdbUrl = String.format("http://www.omdbapi.com/?t=%s&y=" + year, title);
            Request req = new Request.Builder()
                    .url(omdbUrl)
                    .build();
            Response response = client.newCall(req).execute();
            if (!response.isSuccessful()) throw new Error();

            try (ResponseBody body = response.body()) {
                String json = body.string();
                JsonObject j = GSON.fromJson(json, JsonObject.class);
                JsonElement error = j.get("Error");
                if (error != null) continue;

                String language = j.get("Language").getAsJsonPrimitive().getAsString();
                if (language.contains("Hindi")) {
                    Movie movie = new Movie();
                    movie.setTitle(title);
                    movie.setPlot(j.get("Plot").getAsJsonPrimitive().getAsString());
                    movie.setPoster(j.get("Poster").getAsJsonPrimitive().getAsString());
                    movie.setImdbId(j.get("imdbID").getAsJsonPrimitive().getAsString());
                    movies.add(movie);
                }
            }

        }
        return movies;
    }

    @SneakyThrows
    public static void main(String[] args) {
        List<Movie> movies = new SfGateScraper().getMovies();
        System.out.println("movies = " + movies);
    }

    @Data
    public static class Movie {
        String title;
        String poster;
        String plot;
        String imdbId;
    }
}
