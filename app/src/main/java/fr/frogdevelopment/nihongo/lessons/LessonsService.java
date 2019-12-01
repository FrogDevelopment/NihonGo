package fr.frogdevelopment.nihongo.lessons;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LessonsService {

    @GET("lessons/{language}/available")
    Call<List<Lesson>> fetchAvailableLessons(@Path("language") String language);

    @GET("lessons/{language}/download/{lesson}")
    Call<List<Details>> fetchLessons(@Path("language") String language, @Path("lesson") String lesson);
}
