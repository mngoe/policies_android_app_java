package org.openimis.imispolicies.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;

public class Insuree implements Parcelable {

    @NonNull
    private final String chfId;
    @NonNull
    private final String name;
    @NonNull
    private final Date dateOfBirth;
    @Nullable
    private final String gender;
    @Nullable
    private final String photoPath;
    @Nullable
    private final Integer disablingDisease;
    @Nullable
    private final Integer disability;
    @Nullable
    private final Integer coverageInsurance;
    @Nullable
    private final Integer houseType;
    @Nullable
    private final Integer residencePlace;
    @Nullable
    private final byte[] photo;
    @NonNull
    private final List<Policy> policies;

    public Insuree(
            @NonNull String chfId,
            @NonNull String name,
            @NonNull Date dateOfBirth,
            @Nullable String gender,
            @Nullable String photoPath,
            @Nullable byte[] photo,
            @NonNull List<Policy> policies,
            @Nullable Integer disablingDisease,
            @Nullable Integer disability,
            @Nullable Integer coverageInsurance,
            @Nullable Integer houseType,
            @Nullable Integer residencePlace
    ) {
        this.chfId = chfId.trim();
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.photoPath = photoPath;
        this.photo = photo;
        this.policies = policies;
        this.disablingDisease = disablingDisease;
        this.disability = disability;
        this.coverageInsurance = coverageInsurance;
        this.houseType = houseType;
        this.residencePlace = residencePlace;
    }

    public Insuree(
            @NonNull String chfId,
            @NonNull String name,
            @NonNull Date dateOfBirth,
            @Nullable String gender,
            @Nullable String photoPath,
            @Nullable byte[] photo,
            @NonNull List<Policy> policies
    ) {
        this(chfId, name, dateOfBirth, gender, photoPath, photo, policies, 0, 0, 0, 0, 0);
    }

    protected Insuree(Parcel in) {
        chfId = in.readString();
        name = in.readString();
        dateOfBirth = new Date(in.readLong());
        gender = in.readString();
        photoPath = in.readString();
        disablingDisease = in.readInt();
        disability = in.readInt();
        coverageInsurance = in.readInt();
        houseType = in.readInt();
        residencePlace = in.readInt();
        photo = in.createByteArray();
        policies = in.createTypedArrayList(Policy.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chfId);
        dest.writeString(name);
        dest.writeLong(dateOfBirth.getTime());
        dest.writeString(gender);
        dest.writeString(photoPath);
        dest.writeInt(disablingDisease);
        dest.writeInt(disability);
        dest.writeInt(coverageInsurance);
        dest.writeInt(houseType);
        dest.writeInt(residencePlace);
        dest.writeByteArray(photo);
        dest.writeTypedList(policies);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getChfId() {
        return chfId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @Nullable
    public String getGender() {
        return gender;
    }

    @Nullable
    public String getPhotoPath() {
        return photoPath;
    }

    @Nullable
    public Integer getDisablingDisease() {
        return disablingDisease;
    }

    @Nullable
    public Integer getDisability() {
        return disability;
    }

    @Nullable
    public Integer getCoverageInsurance() {
        return coverageInsurance;
    }

    @Nullable
    public Integer getHouseType() {
        return houseType;
    }

    @Nullable
    public Integer getResidencePlace() {
        return residencePlace;
    }

    @Nullable
    public byte[] getPhoto() {
        return photo;
    }

    @NonNull
    public List<Policy> getPolicies() {
        return policies;
    }

    public static final Creator<Insuree> CREATOR = new Creator<>() {
        @Override
        public Insuree createFromParcel(Parcel in) {
            return new Insuree(in);
        }

        @Override
        public Insuree[] newArray(int size) {
            return new Insuree[size];
        }
    };
}
