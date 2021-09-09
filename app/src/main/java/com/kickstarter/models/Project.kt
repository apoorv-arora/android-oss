package com.kickstarter.models

import android.net.Uri
import android.os.Parcelable
import com.kickstarter.libs.Permission
import com.kickstarter.libs.qualifiers.AutoGson
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.mock.factories.UserFactory
import kotlinx.parcelize.Parcelize
import com.kickstarter.models.*
import org.joda.time.DateTime

@AutoGson
@Parcelize
class Project(
    private val availableCardTypes: List<String>,
    private val backersCount: Int,
    private val blurb: String,
    private val backing: Backing?,
    private val category: Category?,
    private val commentsCount: Int,
    private val country: String, // e.g.: US
    private val createdAt: DateTime?,
    private val creator: User,
    private val currency: String, // e.g.: USD
    private val currencySymbol: String, // e.g.: $
    private val currentCurrency: String, // e.g.: User's Preferred currency USD
    private val currencyTrailingCode: Boolean,
    private val displayPrelaunch: Boolean,
    private val featuredAt: DateTime?,
    private val friends: List<User>,
    private val fxRate: Float,
    private val deadline: DateTime?,
    private val goal: Double,
    private val id: Long, // in the Kickstarter app, this is project.pid not project.id
    val isBacking: Boolean,
    val isStarred: Boolean,
    private val lastUpdatePublishedAt: DateTime?,
    private val launchedAt: DateTime?,
    private val location: Location?,
    private val name: String,
    private val permissions: List<Permission>,
    private val pledged: Double,
    private val photo: Photo?,
    private val prelaunchActivated: Boolean,
    private val tags: List<String>,
    private val rewards: List<Reward>,
    private val slug: String?,
    private val staffPick: Boolean,
    private val state: String,
    private val stateChangedAt: DateTime?,
    private val staticUsdRate: Float,
    private val usdExchangeRate: Float,
    private val unreadMessagesCount: Int,
    private val unseenActivityCount: Int,
    private val updatesCount: Int,
    private val updatedAt: DateTime?,
    private val urls: Urls,
    private val video: Video?
) : Parcelable, Relay {

    // - Getters
    fun availableCardTypes() = this.availableCardTypes
    fun backersCount() = this.backersCount
    fun blurb() = this.blurb
    fun backing() = this.backing
    fun category() = this.category
    fun commentsCount() = this.commentsCount
    fun country() = this.country
    fun createdAt() = this.createdAt
    fun creator() = this.creator
    fun currency() = this.currency
    fun currencySymbol() = this.currencySymbol
    fun currentCurrency() = this.currentCurrency
    fun currencyTrailingCode() = this.currencyTrailingCode
    fun displayPrelaunch() = this.displayPrelaunch
    fun featuredAt() = this.featuredAt
    fun friends() = this.friends
    fun fxRate() = this.fxRate
    fun deadline() = this.deadline
    fun goal() = this.goal
    fun lastUpdatePublishedAt() = this.lastUpdatePublishedAt
    fun launchedAt() = this.launchedAt
    fun location() = this.location
    fun name() = this.name
    fun permissions() = this.permissions
    fun pledged() = this.pledged
    fun photo() = this.photo
    fun prelaunchActivated() = this.prelaunchActivated
    fun tags() = this.tags
    fun rewards() = this.rewards
    fun slug() = this.slug
    fun staffPick() = this.staffPick
    fun state() = this.state
    fun stateChangedAt() = this.stateChangedAt
    fun staticUsdRate() = this.staticUsdRate
    fun usdExchangeRate() = this.usdExchangeRate
    fun unreadMessagesCount() = this.unreadMessagesCount
    fun unseenActivityCount() = this.unseenActivityCount
    fun updatesCount() = this.updatesCount
    fun updatedAt() = this.updatedAt
    fun urls() = this.urls
    fun video() = this.video
    override fun id(): Long = this.id

    val isLive = STATE_LIVE == this.state

    @Parcelize
    data class Builder(
        private var availableCardTypes: List<String> = emptyList(),
        private var backersCount: Int = 0,
        private var blurb: String = "",
        private var backing: Backing? = null,
        private var category: Category? = null,
        private var commentsCount: Int = -1,
        private var country: String = "",
        private var createdAt: DateTime? = null,
        private var creator: User? = null,
        private var currency: String = "",
        private var currencySymbol: String = "",
        private var currentCurrency: String = "",
        private var currencyTrailingCode: Boolean = false,
        private var displayPrelaunch: Boolean = false,
        private var featuredAt: DateTime? = null,
        private var friends: List<User> = emptyList(),
        private var fxRate: Float = 0f,
        private var deadline: DateTime? = null,
        private var goal: Double = 0.0,
        private var id: Long = 0,
        private var isBacking: Boolean = false,
        private var isStarred: Boolean = false,
        private var lastUpdatePublishedAt: DateTime? = null,
        private var launchedAt: DateTime? = null,
        private var location: Location? = null,
        private var name: String = "",
        private var permissions: List<Permission> = emptyList(),
        private var pledged: Double = 0.0,
        private var photo: Photo? = null,
        private var prelaunchActivated: Boolean = false,
        private var tags: List<String> = emptyList(),
        private var rewards: List<Reward> = emptyList(),
        private var slug: String? = null,
        private var staffPick: Boolean = false,
        private var state: String = "",
        private var stateChangedAt: DateTime? = null,
        private var staticUsdRate: Float = 0f,
        private var usdExchangeRate: Float = 0f,
        private var unreadMessagesCount: Int = -1,
        private var unseenActivityCount: Int = -1,
        private var updatesCount: Int = -1,
        private var updatedAt: DateTime? = null,
        private var urls: Urls? = null,
        private var video: Video? = null
    ) : Parcelable {
        fun availableCardTypes(avCards: List<String>?) = apply { avCards?.let { this.availableCardTypes = it } }
        fun backersCount(count: Int?) = apply { count?.let { this.backersCount = it } }
        fun blurb(bl: String?) = apply { bl?.let { this.blurb = it } }
        fun backing(b: Backing?) = apply { this.backing = b }
        fun category(cat: Category?) = apply { this.category = cat }
        fun commentsCount(cc: Int?) = apply { cc?.let { this.commentsCount = it } }
        fun country(c: String?) = apply { c?.let { this.country = it } }
        fun createdAt(dt: DateTime?) = apply { this.createdAt = dt }
        fun creator(c: User?) = apply { this.creator = c }
        fun currency(currency: String?) = apply { currency?.let { this.currency = it } }
        fun currencySymbol(symbol: String?) = apply { symbol?.let { this.currencySymbol = it } }
        fun currentCurrency(cCurrency: String?) = apply { cCurrency?.let { this.currentCurrency = it } }
        fun currencyTrailingCode(code: Boolean?) = apply { code?.let { this.currencyTrailingCode = it } }
        fun displayPrelaunch(preLaunch: Boolean?) = apply { preLaunch?.let { this.displayPrelaunch = it } }
        fun featuredAt(dt: DateTime?) = apply { this.featuredAt = dt }
        fun friends(friends: List<User>?) = apply { friends?.let { this.friends = it } }
        fun fxRate(fxRate: Float?) = apply { fxRate?.let { this.fxRate = it } }
        fun deadline(deadline: DateTime?) = apply { this.deadline = deadline }
        fun goal(goal: Double?) = apply { goal?.let { this.goal = it } }
        fun id(l: Long) = apply { this.id = l }
        fun isBacking(isBacking: Boolean?) = apply { isBacking?.let { this.isBacking = it } }
        fun isStarred(isStarred: Boolean?) = apply { isStarred?.let { this.isStarred = it } }
        fun lastUpdatePublishedAt(lastPublished: DateTime?) = apply { this.lastUpdatePublishedAt = lastPublished }
        fun launchedAt(launchedAt: DateTime?) = apply { this.launchedAt = launchedAt }
        fun location(location: Location?) = apply { this.location = location }
        fun name(name: String?) = apply { name?.let { this.name = it } }
        fun permissions(perms: List<Permission>?) = apply { perms?.let { this.permissions = it } }
        fun pledged(pledged: Double?) = apply { pledged?.let { this.pledged = it } }
        fun photo(photo: Photo?) = apply { this.photo = photo }
        fun prelaunchActivated(isPreLaunchAct: Boolean?) = apply { isPreLaunchAct?.let { this.prelaunchActivated = it } }
        fun tags(tags: List<String>?) = apply { tags?.let { this.tags = it } }
        fun rewards(rewards: List<Reward>?) = apply { rewards?.let { this.rewards = it } }
        fun slug(slug: String?) = apply { this.slug = slug }
        fun staffPick(staffPick: Boolean?) = apply { staffPick?.let { this.staffPick = it } }
        fun state(state: String?) = apply { state?.let { this.state = it } }
        fun stateChangedAt(stateChanged: DateTime?) = apply { this.stateChangedAt = stateChanged }
        fun staticUsdRate(sur: Float?) = apply { sur?.let { this.staticUsdRate = it } }
        fun usdExchangeRate(ser: Float?) = apply { ser?.let { this.usdExchangeRate = it } }
        fun unreadMessagesCount(messagesCount: Int?) = apply { messagesCount?.let { this.unreadMessagesCount = it } }
        fun unseenActivityCount(unseenAct: Int?) = apply { unseenAct?.let { this.unseenActivityCount = it } }
        fun updatesCount(updatesCount: Int?) = apply { updatesCount?.let { this.updatesCount = it } }
        fun updatedAt(updatedAt: DateTime?) = apply { this.updatedAt = updatedAt }
        fun urls(urls: Urls?) = apply { this.urls = urls }
        fun video(video: Video?) = apply { this.video = video }
        fun build() = Project(
            availableCardTypes,
            backersCount,
            blurb,
            backing,
            category,
            commentsCount,
            country,
            createdAt,
            creator ?: UserFactory.creator(),
            currency,
            currencySymbol,
            currentCurrency,
            currencyTrailingCode,
            displayPrelaunch,
            featuredAt,
            friends,
            fxRate,
            deadline,
            goal,
            id,
            isBacking,
            isStarred,
            lastUpdatePublishedAt,
            launchedAt,
            location,
            name,
            permissions,
            pledged,
            photo,
            prelaunchActivated,
            tags,
            rewards,
            slug,
            staffPick,
            state,
            stateChangedAt,
            staticUsdRate,
            usdExchangeRate,
            unreadMessagesCount,
            unseenActivityCount,
            updatesCount,
            updatedAt,
            urls ?: Urls.builder().web(Urls.Web.builder().project("").build()).build(),
            video
        )
    }

    companion object {
        fun builder() = Builder()
        const val STATE_STARTED = "started"
        const val STATE_SUBMITTED = "submitted"
        const val STATE_LIVE = "live"
        const val STATE_SUCCESSFUL = "successful"
        const val STATE_FAILED = "failed"
        const val STATE_CANCELED = "canceled"
        const val STATE_SUSPENDED = "suspended"
        const val STATE_PURGED = "purged"
    }

    fun toBuilder() = Builder(
        this.availableCardTypes,
        this.backersCount,
        this.blurb,
        this.backing,
        this.category,
        this.commentsCount,
        this.country,
        this.createdAt,
        this.creator,
        this.currency,
        this.currencySymbol,
        this.currentCurrency,
        this.currencyTrailingCode,
        this.displayPrelaunch,
        this.featuredAt,
        this.friends,
        this.fxRate,
        this.deadline,
        this.goal,
        this.id,
        this.isBacking,
        this.isStarred,
        this.lastUpdatePublishedAt,
        this.launchedAt,
        this.location,
        this.name,
        this.permissions,
        this.pledged,
        this.photo,
        this.prelaunchActivated,
        this.tags,
        this.rewards,
        this.slug,
        this.staffPick,
        this.state,
        this.stateChangedAt,
        this.staticUsdRate,
        this.usdExchangeRate,
        this.unreadMessagesCount,
        this.unseenActivityCount,
        this.updatesCount,
        this.updatedAt,
        this.urls,
        this.video
    )

    fun creatorBioUrl() = urls?.web()?.creatorBio()

    fun descriptionUrl() = this.urls?.web()?.description()

    fun updatesUrl() = this.urls?.web()?.updates() ?: ""

    fun webProjectUrl() = this.urls?.web()?.project() ?: ""

    fun hasComments() = this.commentsCount > 0

    fun hasRewards() = this.rewards.isNotEmpty()

    fun hasVideo() = this.video == null

    /** Returns whether the project is in a canceled state.  */
    fun isCanceled(): Boolean {
        return STATE_CANCELED == this.state
    }

    /** Returns whether the project is in a failed state.  */
    fun isFailed() = STATE_FAILED == this.state

    fun isFeaturedToday(): Boolean {
        return if (this.featuredAt == null) {
            false
        } else DateTimeUtils.isDateToday(this.featuredAt)
    }

    fun isFriendBacking() = this.friends.isNotEmpty()

    fun isFunded(): Boolean {
        return this.isLive && percentageFunded() >= 100
    }

    /** Returns whether the project is in a purged state.  */
    fun isPurged(): Boolean {
        return STATE_PURGED == this.state
    }

    /** Returns whether the project is in a live state.  */
    fun isStarted(): Boolean {
        return STATE_STARTED == this.state
    }

    /** Returns whether the project is in a submitted state.  */
    fun isSubmitted(): Boolean {
        return STATE_SUBMITTED == this.state
    }

    /** Returns whether the project is in a suspended state.  */
    fun isSuspended(): Boolean {
        return STATE_SUSPENDED == this.state
    }

    /** Returns whether the project is in a successful state.  */
    fun isSuccessful(): Boolean {
        return STATE_SUCCESSFUL == this.state
    }

    val isApproachingDeadline = this.deadline?.let {
        return@let if (it.isBeforeNow) {
            false
        } else it.isBefore(DateTime().plusDays(2))
    } ?: false

    fun percentageFunded(): Float {
        return if (this.goal > 0.0f) {
            this.pledged as Float / this.goal as Float * 100.0f
        } else 0.0f
    }

    fun param() = slug ?: id().toString()

    fun secureWebProjectUrl() = Uri.parse(webProjectUrl())
        .buildUpon()
        .scheme("https")
        .build()
        .toString()

    fun newPledgeUrl() = Uri.parse(secureWebProjectUrl())
        .buildUpon()
        .appendEncodedPath("pledge/new")
        .toString()

    fun editPledgeUrl() = Uri.parse(secureWebProjectUrl())
        .buildUpon()
        .appendEncodedPath("pledge/edit")
        .toString()

    override fun toString() = "Project{id=$this.id, name=$this.name}"

    override fun equals(o: Any?): Boolean {
        if (o != null && o is Project) {
            return id() == o.id()
        }
        return false
    }

    override fun hashCode() = id.toInt()
}
