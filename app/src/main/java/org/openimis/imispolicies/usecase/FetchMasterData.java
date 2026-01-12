package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.apache.commons.io.IOUtils;
import org.openimis.imispolicies.ClientAndroidInterface;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.exception.UserNotAuthenticatedException;
import org.openimis.imispolicies.network.request.GetMasterDataExportRequest;
import org.openimis.imispolicies.network.util.OkHttpUtils;
import org.openimis.imispolicies.tools.Log;

import java.io.ByteArrayInputStream;
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

    public FetchMasterData() {
        this(new GetMasterDataExportRequest());
    }

    public FetchMasterData(@NonNull GetMasterDataExportRequest getMasterDataExportRequest) {
        this.getMasterDataExportRequest = getMasterDataExportRequest;
    }

    @NonNull
    @WorkerThread
    public String execute() throws Exception {
        String BASE_URL = "http://192.168.70.213:3000/api/tools/extracts/download_master_data";
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
}
