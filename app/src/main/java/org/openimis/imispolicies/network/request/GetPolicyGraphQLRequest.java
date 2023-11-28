package org.openimis.imispolicies.network.request;


import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imispolicies.GetPolicyQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;

public class GetPolicyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public GetPolicyQuery.Node get(@NonNull String familyUuid) throws Exception {
        List<GetPolicyQuery.Edge> edges = makeSynchronous(new GetPolicyQuery(
                familyUuid
        )).getData().policiesByFamily().edges();
        if (edges.isEmpty()) {
            throw new HttpException(
                    /* code = */ HttpURLConnection.HTTP_NOT_FOUND,
                    /* message = */ "No policy found with family uuid: '" + familyUuid + "'",
                    /* body = */ null,
                    /* cause = */ null
            );
        }
        return Objects.requireNonNull(edges.get(0).node());
    }

}
