package org.openimis.imispolicies.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.domain.entity.Insuree;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CreateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateInsureeGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateInsureeGraphQLRequest;

import java.net.HttpURLConnection;
import java.util.List;

import io.sentry.Sentry;

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
    private final FetchInsureeInquire fetchInsureeInquire;
    @NonNull
    private final CheckMutation checkMutation;
    private static final int STATUS_ERROR = 1;

    public UpdateFamily() {
        this(
                new FetchFamily(),
                new CreateFamilyGraphQLRequest(),
                new UpdateFamilyGraphQLRequest(),
                new CreateInsureeGraphQLRequest(),
                new UpdateInsureeGraphQLRequest(),
                new CheckMutation(),
                new FetchInsureeInquire()
        );
    }

    public UpdateFamily(
            @NonNull FetchFamily fetchFamily,
            @NonNull CreateFamilyGraphQLRequest createFamilyGraphQLRequest,
            @NonNull UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest,
            @NonNull CreateInsureeGraphQLRequest createInsureeGraphQLRequest,
            @NonNull UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest,
            @NonNull CheckMutation checkMutation,
            @NonNull FetchInsureeInquire fetchInsureeInquire
    ) {
        this.fetchFamily = fetchFamily;
        this.createFamilyGraphQLRequest = createFamilyGraphQLRequest;
        this.updateFamilyGraphQLRequest = updateFamilyGraphQLRequest;
        this.createInsureeGraphQLRequest = createInsureeGraphQLRequest;
        this.updateInsureeGraphQLRequest = updateInsureeGraphQLRequest;
        this.checkMutation = checkMutation;
        this.fetchInsureeInquire = fetchInsureeInquire;
    }

    @WorkerThread
    public Integer execute(
            @NonNull Family family,
            @NonNull String insureeCHFID,
            @NonNull List<Family.Policy> policies
            ) throws Exception {
        Integer status;
        try {
            final Family existingFamily = fetchFamily.execute(insureeCHFID);

            status = new CreatePolicy().execute(policies, existingFamily.getId(), existingFamily.getUuid());
            if(status == STATUS_ERROR){
                return status;
            }

//            status = checkMutation.execute(updateFamilyGraphQLRequest.update(family),"Error while updating beneficiary '" + insureeCHFID + "'");
//            outer:
//            for (Family.Member existingMember : existingFamily.getMembers()) {
//                for (Family.Member member: family.getMembers()) {
//                    if (member.getChfId().equals(existingMember.getChfId())) {
//                        continue outer;
//                    }
//                }
//                removeMemberFromFamily(existingMember);
//            }
            return status;
        } catch (HttpException e) {
            Sentry.captureException(e);
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            } else {
                status = checkMutation.execute(createFamilyGraphQLRequest.create(family),"Error while create beneficiary '" + insureeCHFID + "'");

                if(status != STATUS_ERROR){
                    final Family existingFamily = fetchFamily.execute(insureeCHFID);
                    //upload success
                    for (Family.Member member : family.getMembers()) {
                        if(!member.isHead()){
                            insertOrUpdateInsuree(member, existingFamily.getId() );
                        }
                    }
                    status = new CreatePolicy().execute(policies, existingFamily.getId(), existingFamily.getUuid());
                    if(status == STATUS_ERROR){
                        return status;
                    }
                }
                return status;
            }
        }
    }

    @WorkerThread
    private void insertOrUpdateInsuree(@NonNull Family.Member member, @Nullable int familyId ) throws Exception {
        try {
            Insuree insuree = fetchInsureeInquire.execute(member.getChfId());
            checkMutation.execute(updateInsureeGraphQLRequest.update(insuree.getUuid(), member, familyId), "Error while updating insuree '" + member.getChfId() + "'");
        } catch (HttpException e) {
            if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                checkMutation.execute(createInsureeGraphQLRequest.create(member,familyId),"Error while creating insuree '" + member.getChfId() + "'");
            } else {
                Sentry.captureException(e);
                throw e;
            }
        }
    }

    @WorkerThread
    private void removeMemberFromFamily(@NonNull Family.Member member) throws Exception {
        updateInsureeGraphQLRequest.update(null, member, null);
    }
}
