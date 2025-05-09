package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;


import org.openimis.imispolicies.CreatePolicyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.CreatePolicyMutationInput;

import java.util.Objects;
import java.util.UUID;

public class CreatePolicyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String create(@NonNull Family.Policy policy) throws Exception {
        Response<CreatePolicyMutation.Data> response = makeSynchronous(new CreatePolicyMutation(
                CreatePolicyMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Create Policy")
                        .familyId(policy.getFamilyId())
                        .enrollDate(policy.getEnrollDate())
                        .startDate(policy.getStartDate())
                        .expiryDate(policy.getExpiryDate())
                        .value(policy.getValue())
                        .productId(policy.getProductId())
                        .officerId(policy.getOfficerId())
                        .policyNumber(policy.getPolicyNumber())
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .createPolicy(), "mobileEnrollment is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
