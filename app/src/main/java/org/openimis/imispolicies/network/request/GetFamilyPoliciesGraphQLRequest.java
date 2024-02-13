package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetFamilyPoliciesQuery;

public class GetFamilyPoliciesGraphQLRequest extends BaseGraphQLRequest {
    @WorkerThread
    @NonNull
    public GetFamilyPoliciesQuery.PoliciesByFamily get(@NonNull String familyUuid) throws Exception {
        return makeSynchronous(new GetFamilyPoliciesQuery(
                familyUuid
        )).getData().policiesByFamily();
    }
}
