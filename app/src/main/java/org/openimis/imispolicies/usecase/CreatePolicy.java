package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CreatePolicyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePremiumGraphQLRequest;

import java.net.HttpURLConnection;
import java.util.List;

public class CreatePolicy {

    @NonNull
    private final CreatePolicyGraphQLRequest createPolicyGraphQLRequest;
    @NonNull
    private final CreatePremiumGraphQLRequest createPremiumGraphQLRequest;

    public CreatePolicy() {
        this(new CreatePolicyGraphQLRequest(), new CreatePremiumGraphQLRequest());
    }

    public CreatePolicy(
            @NonNull CreatePolicyGraphQLRequest createPolicyGraphQLRequest,
            @NonNull CreatePremiumGraphQLRequest createPremiumGraphQLRequest
    ) {
        this.createPolicyGraphQLRequest = createPolicyGraphQLRequest;
        this.createPremiumGraphQLRequest = createPremiumGraphQLRequest;
    }

    @WorkerThread
    public void execute(List<Family.Policy> policies, String familyUUID) throws Exception {
        for (Family.Policy policy : policies) {
            createPolicyGraphQLRequest.create(policy);
            String policyUuid = "";

            try{
                policyUuid = new FetchPolicy().fetchPolicyUuid(familyUUID);
            } catch (HttpException e) {
                if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                    throw e;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                createPremiumGraphQLRequest.create(premium, policyUuid);
            }
        }
    }
}
