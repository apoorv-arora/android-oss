package com.kickstarter.models

import android.net.Uri
import com.kickstarter.libs.qualifiers.AutoGson
import auto.parcel.AutoParcel
import android.os.Parcelable
import com.kickstarter.libs.Permission
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.models.AutoParcel_Project
import org.joda.time.DateTime
import javax.annotation.Nullable

@AutoGson
@AutoParcel
abstract class Project : Parcelable, Relay {
    abstract fun availableCardTypes(): List<String>
    abstract fun backersCount(): Int
    abstract fun blurb(): String?
    abstract fun backing(): Backing?
    abstract fun category(): Category?
    abstract fun commentsCount(): Int?
    abstract fun country(): String // e.g.: US
    abstract fun createdAt(): DateTime?
    abstract fun creator(): User
    abstract fun currency(): String // e.g.: USD
    abstract fun currencySymbol(): String // e.g.: $
    abstract fun currentCurrency(): String // e.g.: User's Preferred currency USD
    abstract fun currencyTrailingCode(): Boolean
    abstract fun displayPrelaunch(): Boolean?
    abstract fun featuredAt(): DateTime?
    abstract fun fxRate(): Float?
    abstract fun deadline(): DateTime?
    abstract fun goal(): Double
    abstract override fun id(): Long // in the Kickstarter app, this is project.pid not project.id
    abstract val isBacking: Boolean
    abstract val isStarred: Boolean
    abstract fun lastUpdatePublishedAt(): DateTime?
    abstract fun launchedAt(): DateTime?
    abstract fun location(): Location?
    abstract fun name(): String
    abstract fun pledged(): Double
    abstract fun photo(): Photo?
    abstract fun prelaunchActivated(): Boolean?
    abstract fun tags(): List<String>
    abstract fun rewards(): List<Reward>
    abstract fun slug(): String?
    abstract fun staffPick(): Boolean?
    abstract fun state(): String
    abstract fun stateChangedAt(): DateTime?
    abstract fun staticUsdRate(): Float
    abstract fun usdExchangeRate(): Float
    abstract fun unreadMessagesCount(): Int?
    abstract fun unseenActivityCount(): Int?
    abstract fun updatesCount(): Int?
    abstract fun updatedAt(): DateTime?
    abstract fun urls(): Urls
    abstract fun video(): Video?

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun availableCardTypes(cardTypes: List<String>): Builder
        abstract fun backersCount(backersCount: Int): Builder
        abstract fun blurb(blurb: String?): Builder
        abstract fun backing(backing: Backing?): Builder
        abstract fun category(category: Category?): Builder
        abstract fun commentsCount(commentsCount: Int?): Builder
        abstract fun country(country: String): Builder
        abstract fun createdAt(createdAt: DateTime?): Builder
        abstract fun creator(creator: User): Builder
        abstract fun currency(currency: String): Builder
        abstract fun currencySymbol(currencySymbol: String): Builder
        abstract fun currentCurrency(currentCurrency: String): Builder
        abstract fun currencyTrailingCode(currencyTrailingCode: Boolean): Builder
        abstract fun displayPrelaunch(displayPrelaunch: Boolean?): Builder
        abstract fun deadline(deadline: DateTime?): Builder
        abstract fun featuredAt(featuredAt: DateTime?): Builder
        abstract fun fxRate(fxRate: Float?): Builder
        abstract fun goal(goal: Double): Builder
        abstract fun id(id: Long): Builder
        abstract fun isBacking(isBacking: Boolean): Builder
        abstract fun isStarred(isStarred: Boolean): Builder
        abstract fun lastUpdatePublishedAt(lastUpdatePublishedAt: DateTime?): Builder
        abstract fun launchedAt(launchedAt: DateTime?): Builder
        abstract fun location(location: Location?): Builder
        abstract fun name(name: String?): Builder
        abstract fun pledged(pledged: Double): Builder
        abstract fun photo(photo: Photo?): Builder
        abstract fun prelaunchActivated(prelaunchActivated: Boolean?): Builder
        abstract fun tags(tags: List<String>): Builder
        abstract fun rewards(reward: List<Reward>): Builder
        abstract fun slug(slug: String?): Builder
        abstract fun staffPick(staffPick: Boolean?): Builder
        abstract fun staticUsdRate(staticUsdRate: Float): Builder
        abstract fun usdExchangeRate(usdExchange: Float): Builder
        abstract fun state(state: String?): Builder
        abstract fun stateChangedAt(stateChangedAt: DateTime?): Builder
        abstract fun unreadMessagesCount(unreadMessageCount: Int?): Builder
        abstract fun unseenActivityCount(unseenActivityCount: Int?): Builder
        abstract fun updatedAt(updatedAt: DateTime?): Builder
        abstract fun updatesCount(updatesCount: Int?): Builder
        abstract fun urls(urls: Urls?): Builder
        abstract fun video(video: Video?): Builder
        abstract fun build(): Project
    }

    abstract fun toBuilder(): Builder?

    fun creatorBioUrl(): String {
        return urls().web().creatorBio()
    }

    fun descriptionUrl(): String {
        return urls().web().description()
    }

    fun updatesUrl(): String {
        return urls().web().updates()!!
    }

    fun webProjectUrl(): String {
        return urls().web().project()
    }

    fun hasComments(): Boolean {
        return IntegerUtils.isNonZero(commentsCount())
    }

    fun hasRewards(): Boolean {
        return rewards() != null
    }

    fun hasVideo(): Boolean {
        return video() != null
    }

    /** Returns whether the project is in a canceled state.  */
    val isCanceled: Boolean
        get() = STATE_CANCELED == state()

    /** Returns whether the project is in a failed state.  */
    val isFailed: Boolean
        get() = STATE_FAILED == state()
    val isFeaturedToday: Boolean
        get() = if (featuredAt() == null) {
            false
        } else DateTimeUtils.isDateToday(featuredAt()!!)

    /** Returns whether the project is in a live state.  */
    val isLive: Boolean
        get() = STATE_LIVE == state()
    val isFriendBacking: Boolean = false
        //get() = friends() != null && friends()!!.size > 0
    val isFunded: Boolean
        get() = isLive && percentageFunded() >= 100

    /** Returns whether the project is in a purged state.  */
    val isPurged: Boolean
        get() = STATE_PURGED == state()

    /** Returns whether the project is in a live state.  */
    val isStarted: Boolean
        get() = STATE_STARTED == state()

    /** Returns whether the project is in a submitted state.  */
    val isSubmitted: Boolean
        get() = STATE_SUBMITTED == state()

    /** Returns whether the project is in a suspended state.  */
    val isSuspended: Boolean
        get() = STATE_SUSPENDED == state()

    /** Returns whether the project is in a successful state.  */
    val isSuccessful: Boolean
        get() = STATE_SUCCESSFUL == state()
    val isApproachingDeadline: Boolean
        get() = if (deadline()!!.isBeforeNow) {
            false
        } else deadline()!!.isBefore(DateTime().plusDays(2))

    fun percentageFunded(): Float {
        return if (goal() > 0.0f) {
            pledged().toFloat() / goal().toFloat() * 100.0f
        } else 0.0f
    }

    fun param(): String {
        val slug = slug()
        return slug ?: id().toString()
    }

    fun secureWebProjectUrl(): String {
        // TODO: Just use http with local env
        return Uri.parse(webProjectUrl()).buildUpon().scheme("https").build().toString()
    }

    fun newPledgeUrl(): String {
        return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/new")
            .toString()
    }

    fun editPledgeUrl(): String {
        return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/edit")
            .toString()
    }

    fun rewardSelectedUrl(reward: Reward): String {
        return Uri.parse(newPledgeUrl())
            .buildUpon().scheme("https")
            .appendQueryParameter("backing[backer_reward_id]", reward.id().toString())
            .appendQueryParameter("clicked_reward", "true")
            .build()
            .toString()
    }

    override fun toString(): String {
        return ("Project{"
                + "id=" + id() + ", "
                + "name=" + name() + ", "
                + "}")
    }

    override fun equals(o: Any?): Boolean {
        if (o != null && o is Project) {
            return id() == o.id()
        }
        return false
    }

    override fun hashCode(): Int {
        return id().toInt()
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoParcel_Project.Builder()
                .isBacking(false)
                .isStarred(false)
                .rewards(emptyList())
        }

        const val STATE_STARTED = "started"
        const val STATE_SUBMITTED = "submitted"
        const val STATE_LIVE = "live"
        const val STATE_SUCCESSFUL = "successful"
        const val STATE_FAILED = "failed"
        const val STATE_CANCELED = "canceled"
        const val STATE_SUSPENDED = "suspended"
        const val STATE_PURGED = "purged"
    }
}