package fr.frogdevelopment.nihongo.lessons;

import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LessonsService {

    @GET("last_ready")
    Call<Integer> fetchAvailableLessons(@Query("locale") String language);

    @GET("import")
    Call<List<Details>> fetchLessons(@Query("locale") String language, @Query("lesson") String lesson);
}
