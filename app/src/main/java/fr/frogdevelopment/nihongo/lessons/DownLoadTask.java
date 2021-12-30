package fr.frogdevelopment.nihongo.lessons;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.frogdevelopment.nihongo.data.model.Details;

import static java.nio.charset.StandardCharsets.UTF_8;

class DownLoadTask extends AsyncTask<String, Integer, List<Details>> {

    interface DownloadListener {
        void onSuccess(List<Details> details);

        void onFailure();
    }

    private static final String           BASE_URL = "http://frog-development.com/nihongo/lessons/";
    private final        String           mSuffixTag;
    private final        DownloadListener mListener;

    public DownLoadTask(String mSuffixTag, DownloadListener listener) {
        this.mSuffixTag = mSuffixTag;
        this.mListener = listener;
    }

    @Override
    protected List<Details> doInBackground(String... params) {
        HttpURLConnection connection = null;
        String local = params[0];
        String lesson = params[1];
        String fileName = local + "-" + lesson + ".tar";
        Log.d("NIHONGO", "Downloading lesson " + fileName);
        try {
            URL url = new URL(BASE_URL + fileName);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // fixme display message error
                return Collections.emptyList();
            }

            return read(connection, lesson);
        } catch (IOException e) {
            Log.e("NGD", "Can not fetch data", e);
            // fixme display message error
            return Collections.emptyList();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<Details> read(HttpURLConnection connection, String lesson) throws IOException {
        final CSVFormat csvFormat = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        final List<Details> entities = new ArrayList<>();
        try (final BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
             final GzipCompressorInputStream inputStream = new GzipCompressorInputStream(is);
             final TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream)) {
            ArchiveEntry entry;
            while (null != (entry = tarIn.getNextEntry())) {
                if (entry.getSize() < 1) {
                    continue;
                }

                // parse lines
                final CSVParser parse = csvFormat.parse(new InputStreamReader(tarIn, UTF_8));

                for (CSVRecord record : parse.getRecords()) {
                    entities.add(toEntity(record, lesson));
                }
            }
        }

        return entities;
    }

    private Details toEntity(final CSVRecord record, String lesson) {
        Details details = new Details();
        details.kanji = record.get("kanji");
        details.kana = record.get("kana");
        details.sortLetter = record.get("sort_letter");
        details.input = record.get("input");
        details.details = record.get("details");
        details.example = record.get("example");
        details.tags = mSuffixTag + " " + lesson;

        return details;
    }

    @Override
    protected void onPostExecute(List<Details> result) {
        if (result.isEmpty()) {
            mListener.onFailure();
        } else {
            mListener.onSuccess(result);
        }
    }
}
