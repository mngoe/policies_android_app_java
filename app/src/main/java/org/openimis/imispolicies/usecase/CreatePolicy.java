package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CheckMutationGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePolicyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePremiumGraphQLRequest;

import java.net.HttpURLConnection;
import java.util.List;

public class CreatePolicy {

    @NonNull
    private final CreatePolicyGraphQLRequest createPolicyGraphQLRequest;
    @NonNull
    private final CreatePremiumGraphQLRequest createPremiumGraphQLRequest;
    @NonNull
    private final CheckMutation checkMutation;

    public CreatePolicy() {
        this(new CheckMutation(), new CreatePolicyGraphQLRequest(), new CreatePremiumGraphQLRequest());
    }

    public CreatePolicy(
            @NonNull CheckMutation checkMutation,
            @NonNull CreatePolicyGraphQLRequest createPolicyGraphQLRequest,
            @NonNull CreatePremiumGraphQLRequest createPremiumGraphQLRequest
    ) {
        this.createPolicyGraphQLRequest = createPolicyGraphQLRequest;
        this.createPremiumGraphQLRequest = createPremiumGraphQLRequest;
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public void execute(@NonNull String chfId, @NonNull Family.Policy policy, String familyUUID) throws Exception {

        checkMutation.execute(createPolicyGraphQLRequest.create(policy), "Error while creating policy for beneficiary '" + chfId + "'");

        if(!policy.getPremiums().isEmpty()){
            try{
                String policyUuid = new FetchPolicy().fetchPolicyUuid(familyUUID);
                for (Family.Policy.Premium premium : policy.getPremiums()) {
                    checkMutation.execute(createPremiumGraphQLRequest.create(premium, policyUuid),"Error while creating contribution for beneficiary '" + chfId + "'");
                }
            }catch (HttpException e){
                if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                    throw e;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
