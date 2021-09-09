package com.kickstarter.models;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.libs.utils.UrlUtils;

import auto.parcel.AutoParcel;

@AutoParcel
@AutoGson
public abstract class Urls implements Parcelable {
    public abstract Web web();
    public abstract @Nullable
    Api api();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Builder web(Web __);
        public abstract Builder api(Api __);
        public abstract Urls build();
    }

    public static Builder builder() {
        return new AutoParcel_Urls.Builder();
    }

    public abstract Builder toBuilder();

    @AutoParcel
    @AutoGson
    public abstract static class Web implements Parcelable {
        public abstract String project();
        public abstract @Nullable String projectShort();
        public abstract String rewards();
        public abstract @Nullable String updates();

        @AutoParcel.Builder
        public abstract static class Builder {
            public abstract Builder project(String __);
            public abstract Builder projectShort(String __);
            public abstract Builder rewards(String __);
            public abstract Builder updates(String __);
            public abstract Web build();
        }

        public static Builder builder() {
            return new AutoParcel_Urls_Web.Builder();
        }

        public abstract Builder toBuilder();

        public @NonNull
        String creatorBio() {
            return UrlUtils.INSTANCE.appendPath(project(), "creator_bio");
        }

        public @NonNull String description() {
            return UrlUtils.INSTANCE.appendPath(project(), "description");
        }
    }

    @AutoParcel
    @AutoGson
    public abstract static class Api implements Parcelable {
        public abstract @Nullable String project();
        public abstract @Nullable String comments();
        public abstract @Nullable String updates();

        @AutoParcel.Builder
        public abstract static class Builder {
            public abstract Builder project(String __);
            public abstract Builder comments(String __);
            public abstract Builder updates(String __);
            public abstract Api build();
        }

        public static Builder builder() {
            return new AutoParcel_Urls_Api.Builder();
        }

        public abstract Builder toBuilder();
    }
}
