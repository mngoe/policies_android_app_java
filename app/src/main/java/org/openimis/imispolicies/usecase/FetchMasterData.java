package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openimis.imispolicies.BuildConfig;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.exception.UserNotAuthenticatedException;
import org.openimis.imispolicies.network.request.GetMasterDataExportRequest;
import org.openimis.imispolicies.network.util.OkHttpUtils;
import org.openimis.imispolicies.tools.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.sentry.Sentry;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FetchMasterData {

    private static final String MASTER_DATA_FILE_NAME = "masterdata.";
    @NonNull
    private final GetMasterDataExportRequest getMasterDataExportRequest;

    private static final String BASE_URL = BuildConfig.MASTER_DATA_URL + "api/tools/extracts/download_master_data";

    public FetchMasterData() {
        this(new GetMasterDataExportRequest());
    }

    public FetchMasterData(@NonNull GetMasterDataExportRequest getMasterDataExportRequest) {
        this.getMasterDataExportRequest = getMasterDataExportRequest;
    }

    @NonNull
    @WorkerThread
    public String execute() throws Exception {
        OkHttpClient okHttpClient = OkHttpUtils.getDefaultOkHttpClient();
        Request.Builder builder = new Request.Builder();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL)).newBuilder();
        builder.url(urlBuilder.build())
                .addHeader("Content-Type", "application/json");
        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            ResponseBody body = response.body();
            if (response.isSuccessful() && body != null) {
                ZipInputStream zipFile = new ZipInputStream(new ByteArrayInputStream(body.bytes()));
                ZipEntry entry;
                while ((entry = zipFile.getNextEntry()) != null) {
                    // Currently, the name of the file is "MasterData.txt" but the code is a little bit
                    // more permissive in case someone wants to "fix" that into 'masterdata.json'.
                    if (entry.getName().toLowerCase(Locale.ENGLISH).startsWith(MASTER_DATA_FILE_NAME)) {
                        return IOUtils.toString(zipFile, StandardCharsets.UTF_8);
                    }
                }
                throw new IllegalArgumentException("The file '" + MASTER_DATA_FILE_NAME + "' could not be found in the zip file.");
            } else {
                String responseBody = null;
                if (body != null) {
                    responseBody = body.string();
                }
                throw new HttpException(response.code(), response.message(), responseBody, null);
            }
        }catch (HttpException e) {
            // By default, there is no authentication or permissions needed to download the master
            // data but it's possible to put some restrictions in the configuration.
            // Therefore, it's possible the backend would return a 403 (though it should return a
            // 401) when trying to download the zip.
            Sentry.captureException(e);
            if (e.getCode() == 401 || e.getCode() == 403) {
                throw new UserNotAuthenticatedException("Backend return '" + e.getCode() + "' while trying to download master data.", e);
            } else throw e;
        }
    }

    public JSONObject streamOnline() throws Exception {
        try{
            URL url = new URL(BASE_URL);
            ZipInputStream zipInputStream = new ZipInputStream(url.openStream());
            ZipEntry zipEntry;
            JSONObject masterDataObj = new JSONObject();

            while ((zipEntry = zipInputStream.getNextEntry()) != null){
                if(zipEntry.getName().toLowerCase(Locale.ENGLISH).startsWith(MASTER_DATA_FILE_NAME)){
                    JsonReader reader = new JsonReader(
                            new InputStreamReader(zipInputStream, StandardCharsets.UTF_8)
                    );
                    reader.beginObject();

                    while (reader.hasNext()){
                        String name = reader.nextName();
                        if(!name.equals("cheques")){
                            JSONArray array = new JSONArray();
                            reader.beginArray();
                            while (reader.hasNext()) {
                                JsonObject gson = JsonParser.parseReader(reader).getAsJsonObject();
                                array.put(new JSONObject(gson.toString()));
                            }
                            reader.endArray();
                            masterDataObj.put(name,array);
                        } else {
                            reader.skipValue();
                        }
                    }
                }
            }
            return masterDataObj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
