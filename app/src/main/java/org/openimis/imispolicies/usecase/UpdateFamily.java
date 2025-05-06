package org.openimis.imispolicies.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CreateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateInsureeGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateInsureeGraphQLRequest;

import java.net.HttpURLConnection;

public class UpdateFamily {

    @NonNull
    private final FetchFamily fetchFamily;
    @NonNull
    private final CreateFamilyGraphQLRequest createFamilyGraphQLRequest;
    @NonNull
    private final UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest;
    @NonNull
    private final CreateInsureeGraphQLRequest createInsureeGraphQLRequest;
    @NonNull
    private final UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest;
    @NonNull
    private final CheckMutation checkMutation;

    public UpdateFamily() {
        this(
                new FetchFamily(),
                new CreateFamilyGraphQLRequest(),
                new UpdateFamilyGraphQLRequest(),
                new CreateInsureeGraphQLRequest(),
                new UpdateInsureeGraphQLRequest(),
                new CheckMutation()
        );
    }

    public UpdateFamily(
            @NonNull FetchFamily fetchFamily,
            @NonNull CreateFamilyGraphQLRequest createFamilyGraphQLRequest,
            @NonNull UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest,
            @NonNull CreateInsureeGraphQLRequest createInsureeGraphQLRequest,
            @NonNull UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest,
            @NonNull CheckMutation checkMutation
    ) {
        this.fetchFamily = fetchFamily;
        this.createFamilyGraphQLRequest = createFamilyGraphQLRequest;
        this.updateFamilyGraphQLRequest = updateFamilyGraphQLRequest;
        this.createInsureeGraphQLRequest = createInsureeGraphQLRequest;
        this.updateInsureeGraphQLRequest = updateInsureeGraphQLRequest;
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public void execute(@NonNull Family family,@NonNull String insureeCHFID ) throws Exception {
        try {
            Family existingFamily = fetchFamily.execute(insureeCHFID);

            checkMutation.execute(updateFamilyGraphQLRequest.update(family),"Error while updating beneficiary '" + insureeCHFID + "'");
            outer:
            for (Family.Member existingMember : existingFamily.getMembers()) {
                for (Family.Member member: family.getMembers()) {
                    if (member.getChfId().equals(existingMember.getChfId())) {
                        continue outer;
                    }
                }
                removeMemberFromFamily(existingMember);
            }
        } catch (HttpException e) {
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            }else {
                checkMutation.execute(createFamilyGraphQLRequest.create(family),"Error while create beneficiary '" + insureeCHFID + "'");
            }
        }
        for (Family.Member member : family.getMembers()) {
            insertOrUpdateInsuree(member, insureeCHFID );
        }
    }

    @WorkerThread
    private void insertOrUpdateInsuree(@NonNull Family.Member member, @Nullable String insureeCHFID ) throws Exception {
        try {
            Family existingFamily = fetchFamily.execute(insureeCHFID);
            try {
                checkMutation.execute(createInsureeGraphQLRequest.create(member,existingFamily.getId()),"Error while creating insuree '" + insureeCHFID + "'");
            } catch (Exception e) {
                checkMutation.execute(updateInsureeGraphQLRequest.update(member, existingFamily.getId()), "Error while updating insuree '" + insureeCHFID + "'");
            }
        } catch (HttpException e) {
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            }
        }
    }

    @WorkerThread
    private void removeMemberFromFamily(@NonNull Family.Member member) throws Exception {
        updateInsureeGraphQLRequest.update(member, null);
    }
}
