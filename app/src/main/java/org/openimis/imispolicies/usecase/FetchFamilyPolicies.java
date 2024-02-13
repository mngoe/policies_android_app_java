package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetFamilyPoliciesQuery;
import org.openimis.imispolicies.GetPolicyQuery;
import org.openimis.imispolicies.domain.entity.Policy;
import org.openimis.imispolicies.network.request.GetFamilyPoliciesGraphQLRequest;
import org.openimis.imispolicies.network.request.GetPolicyGraphQLRequest;
import org.openimis.imispolicies.network.util.Mapper;

import java.util.List;
import java.util.Objects;

public class FetchFamilyPolicies {

    @NonNull
    private final GetFamilyPoliciesGraphQLRequest getFamilyPoliciesGraphQLRequest;

    public FetchFamilyPolicies() {
        this(new GetFamilyPoliciesGraphQLRequest());
    }

    public FetchFamilyPolicies(@NonNull GetFamilyPoliciesGraphQLRequest getFamilyPoliciesGraphQLRequest) {
        this.getFamilyPoliciesGraphQLRequest = getFamilyPoliciesGraphQLRequest;
    }

    @WorkerThread
    @NonNull
    public List<Policy> execute(@NonNull String familyUuid) throws Exception{
        GetFamilyPoliciesQuery.PoliciesByFamily response = getFamilyPoliciesGraphQLRequest.get(familyUuid);
        return Mapper.map(
                response.edges(),
                dto -> {
                    GetFamilyPoliciesQuery.Node node = Objects.requireNonNull(dto.node());
                    return new Policy(
                            node.productCode(),
                            node.productName(),
                            null,
                            node.expiryDate(),
                            node.status()==2 ? Policy.Status.ACTIVE:Policy.Status.IDLE,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                }
                );
    }
}
