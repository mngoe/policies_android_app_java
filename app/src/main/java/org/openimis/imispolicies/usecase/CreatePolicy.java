package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.request.CheckMutationGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePolicyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePremiumGraphQLRequest;

import java.util.List;

public class CreatePolicy {

    @NonNull
    private final CreatePolicyGraphQLRequest createPolicyGraphQLRequest;
    @NonNull
    private final CreatePremiumGraphQLRequest createPremiumGraphQLRequest;
    @NonNull
    private final CheckMutation checkMutation;
    private static final int STATUS_ERROR = 1;

    public CreatePolicy() {
        this(new CreatePolicyGraphQLRequest(), new CreatePremiumGraphQLRequest(), new CheckMutation());
    }

    public CreatePolicy(
            @NonNull CreatePolicyGraphQLRequest createPolicyGraphQLRequest,
            @NonNull CreatePremiumGraphQLRequest createPremiumGraphQLRequest,
            @NonNull CheckMutation checkMutation
    ) {
        this.createPolicyGraphQLRequest = createPolicyGraphQLRequest;
        this.createPremiumGraphQLRequest = createPremiumGraphQLRequest;
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public Integer execute(List<Family.Policy> policies, int familyId, String familyUuid) throws Exception {
        Integer status = STATUS_ERROR;
        for (Family.Policy policy : policies) {
            status = checkMutation.execute(createPolicyGraphQLRequest.create(policy, familyId),"Error while creating policy");
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                status = checkMutation.execute(createPremiumGraphQLRequest.create(premium),"Error while creating premium");
            }
        }
        return status;
    }
}
