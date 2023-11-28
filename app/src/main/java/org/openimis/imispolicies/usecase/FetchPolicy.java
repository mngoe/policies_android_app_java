package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetFamilyQuery;
import org.openimis.imispolicies.GetPolicyQuery;
import org.openimis.imispolicies.domain.utils.IdUtils;
import org.openimis.imispolicies.network.request.GetFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.GetPolicyGraphQLRequest;

public class FetchPolicy {

    @NonNull
    private final GetPolicyGraphQLRequest getPolicyGraphQLRequest;

    public FetchPolicy() {
        this(new GetPolicyGraphQLRequest());
    }

    public FetchPolicy(@NonNull GetPolicyGraphQLRequest getPolicyGraphQLRequest) {
        this.getPolicyGraphQLRequest = getPolicyGraphQLRequest;
    }

    @WorkerThread
    @NonNull
    public int fetchPolicyId(@NonNull String familyUuid) throws Exception{
        GetPolicyQuery.Node node = getPolicyGraphQLRequest.get(familyUuid);
        return node.policyId();
    }

    @WorkerThread
    @NonNull
    public String fetchPolicyUuid(@NonNull String familyUuid) throws Exception{
        GetPolicyQuery.Node node = getPolicyGraphQLRequest.get(familyUuid);
        return node.policyUuid();
    }

}
