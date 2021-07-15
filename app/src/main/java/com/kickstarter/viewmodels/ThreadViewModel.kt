package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.ApolloPaginate
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.extensions.toCommentCardList
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThreadActivity
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ThreadViewModel {

    interface Inputs {
        fun nextPage()
        fun reloadRepliesPage()
        fun insertNewReplyToList(comment: String, createdAt: DateTime)
        fun onShowGuideLinesLinkClicked()
    }

    interface Outputs {
        /** The anchored root comment */
        fun getRootComment(): Observable<Comment>

        /** get comment replies **/
        fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>>

        /** Will tell to the compose view if should open the keyboard */
        fun shouldFocusOnCompose(): Observable<Boolean>
        fun scrollToBottom(): Observable<Void>

        fun currentUserAvatar(): Observable<String?>
        fun replyComposerStatus(): Observable<CommentComposerStatus>
        fun showReplyComposer(): Observable<Boolean>

        fun isFetchingReplies(): Observable<Boolean>
        fun loadMoreReplies(): Observable<Void>

        /** Display the pagination Error Cell **/
        fun shouldShowPaginationErrorUI(): Observable<Boolean>
        /** Display the Initial Error Cell **/
        fun initialLoadCommentsError(): Observable<Boolean>

        fun refresh(): Observable<Void>

        fun showCommentGuideLinesLink(): Observable<Void>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ThreadActivity>(environment), Inputs, Outputs {
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val nextPage = PublishSubject.create<Void>()
        private val onLoadingReplies = PublishSubject.create<Void>()
        private val insertNewReplyToList = PublishSubject.create<Pair<String, DateTime>>()
        private val onShowGuideLinesLinkClicked = PublishSubject.create<Void>()

        private val rootComment = BehaviorSubject.create<Comment>()
        private val focusOnCompose = BehaviorSubject.create<Boolean>()
        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val replyComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showReplyComposer = BehaviorSubject.create<Boolean>()
        private val scrollToBottom = BehaviorSubject.create<Void>()
        private val isFetchingReplies = BehaviorSubject.create<Boolean>()
        private val hasPreviousElements = BehaviorSubject.create<Boolean>()
        private val refresh = PublishSubject.create<Void>()
        private val loadMoreReplies = PublishSubject.create<Void>()
        private val displayPaginationError = BehaviorSubject.create<Boolean>()
        private val initialLoadCommentsError = BehaviorSubject.create<Boolean>()
        private val showGuideLinesLink = BehaviorSubject.create<Void>()

        private val onCommentReplies =
            BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        private var project: Project? = null

        val inputs = this
        val outputs = this

        // - Error observables to handle the 3 different use cases
        private val internalError = BehaviorSubject.create<Throwable>()
        private val initialError = BehaviorSubject.create<Throwable>()
        private val paginationError = BehaviorSubject.create<Throwable>()

        init {

            val commentData = getCommentCardDataFromIntent()
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            intent()
                .map { it.getBooleanExtra(IntentKey.REPLY_EXPAND, false) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.focusOnCompose)

            val comment =
                getCommentCardDataFromIntent().map { it.comment }.map { requireNotNull(it) }

            val project = commentData
                .map { it.project }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            project.take(1)
                .subscribe {
                    this.project = it
                }

            loadCommentListFromProjectOrUpdate(comment)

            this.insertNewReplyToList
                .distinctUntilChanged()
                .withLatestFrom(this.currentUser.loggedInUser()) { comment, user ->
                    Pair(comment, user)
                }
                .withLatestFrom(commentData) { reply, parent ->
                    Pair(reply, parent)
                }
                .map {
                    Pair(
                        it.second,
                        buildReplyBody(Pair(Pair(it.second, it.first.second), it.first.first))
                    )
                }
                .map {
                    CommentCardData.builder()
                        .comment(it.second)
                        .project(it.first.project)
                        .commentableId(it.first.commentableId)
                        .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
                        .build()
                }
                .withLatestFrom(this.onCommentReplies) { reply, pair ->
                    Pair(
                        pair.first.toMutableList().apply {
                            add(reply)
                        }.toList(),
                        pair.second
                    )
                }.compose(bindToLifecycle())
                .subscribe {
                    onCommentReplies.onNext(it)
                    scrollToBottom.onNext(null)
                }

            commentData
                .compose(bindToLifecycle())
                .subscribe {
                    this.rootComment.onNext(it.comment)
                }

            val loggedInUser = this.currentUser.loggedInUser()
                .filter { u -> u != null }
                .map { requireNotNull(it) }

            loggedInUser
                .compose(bindToLifecycle())
                .subscribe {
                    currentUserAvatar.onNext(it.avatar().small())
                }

            loggedInUser
                .compose(bindToLifecycle())
                .subscribe {
                    showReplyComposer.onNext(true)
                }

            project
                .compose(Transformers.combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    val composerStatus = getCommentComposerStatus(Pair(it.first, it.second))
                    showReplyComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    replyComposerStatus.onNext(composerStatus)
                }

            this.onLoadingReplies
                .map {
                    this.onCommentReplies.value
                }.compose(bindToLifecycle())
                .subscribe {
                    if (it?.first?.isNotEmpty() == true) {
                        this.loadMoreReplies.onNext(null)
                    } else {
                        this.refresh.onNext(null)
                    }
                }

            this.initialError
                .compose(bindToLifecycle())
                .subscribe {
                    this.initialLoadCommentsError.onNext(true)
                }

            this.paginationError
                .compose(bindToLifecycle())
                .subscribe {
                    this.displayPaginationError.onNext(true)
                }

            this.onShowGuideLinesLinkClicked
                .compose(bindToLifecycle())
                .subscribe {
                    showGuideLinesLink.onNext(null)
                }
        }

        private fun loadCommentListFromProjectOrUpdate(comment: Observable<Comment>) {
            val startOverWith =
                Observable.merge(
                    comment,
                    comment.compose(
                        Transformers.takeWhen(
                            refresh
                        )
                    )
                )

            val apolloPaginate =
                ApolloPaginate.builder<CommentCardData, CommentEnvelope, Comment?>()
                    .nextPage(nextPage)
                    .distinctUntilChanged(true)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData {
                        hasPreviousElements.onNext(it.pageInfoEnvelope()?.hasPreviousPage ?: false)
                        this.project?.let { project -> mapListToData(it, project) }
                    }
                    .loadWithParams {
                        loadWithProjectReplies(
                            Observable.just(it.first), it.second
                        )
                    }
                    .isReversed(true)
                    .clearWhenStartingOver(true)
                    .build()

            apolloPaginate
                .isFetching()
                .compose(bindToLifecycle<Boolean>())
                .subscribe(this.isFetchingReplies)

            apolloPaginate
                .paginatedData()
                ?.compose(Transformers.combineLatestPair(this.hasPreviousElements))
                ?.distinctUntilChanged()
                ?.share()
                ?.subscribe {
                    this.onCommentReplies.onNext(it)
                }

            this.internalError
                .compose(bindToLifecycle())
                .subscribe {
                    this.initialError.onNext(it)
                }

            this.internalError
                .compose(Transformers.combineLatestPair(onCommentReplies))
                .filter {
                    it.second.first.isNotEmpty()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.paginationError.onNext(it.first)
                }
        }

        private fun mapListToData(it: CommentEnvelope, project: Project) =
            it.comments?.toCommentCardList(project)

        private fun loadWithProjectReplies(
            comment: Observable<Comment>,
            cursor: String?
        ): Observable<CommentEnvelope> {
            return comment.switchMap {
                return@switchMap this.apolloClient.getRepliesForComment(it, cursor)
            }.doOnError {
                this.internalError.onNext(it)
            }
                .onErrorResumeNext(Observable.empty())
        }

        private fun buildReplyBody(it: Pair<Pair<CommentCardData, User>, Pair<String, DateTime>>): Comment {
            return Comment.builder()
                .body(it.second.first)
                .parentId(it.first.first.comment?.id() ?: -1)
                .authorBadges(listOf())
                .createdAt(it.second.second)
                .cursor("")
                .deleted(false)
                .id(-1)
                .repliesCount(0)
                .author(it.first.second)
                .build()
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>) =
            when {
                projectAndUser.second == null -> CommentComposerStatus.GONE
                projectAndUser.first.isBacking || ProjectUtils.userIsCreator(
                    projectAndUser.first,
                    projectAndUser.second
                ) -> CommentComposerStatus.ENABLED
                else -> CommentComposerStatus.DISABLED
            }

        private fun getCommentCardDataFromIntent() = intent()
            .map { it.getParcelableExtra(IntentKey.COMMENT_CARD_DATA) as CommentCardData? }
            .ofType(CommentCardData::class.java)

        override fun nextPage() = nextPage.onNext(null)
        override fun reloadRepliesPage() = onLoadingReplies.onNext(null)

        override fun getRootComment(): Observable<Comment> = this.rootComment
        override fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>> =
            this.onCommentReplies

        override fun insertNewReplyToList(comment: String, createdAt: DateTime) =
            this.insertNewReplyToList.onNext(
                Pair(comment, createdAt)
            )

        override fun onShowGuideLinesLinkClicked() = onShowGuideLinesLinkClicked.onNext(null)

        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
        override fun scrollToBottom(): Observable<Void> = this.scrollToBottom
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun replyComposerStatus(): Observable<CommentComposerStatus> = replyComposerStatus
        override fun showReplyComposer(): Observable<Boolean> = showReplyComposer
        override fun isFetchingReplies(): Observable<Boolean> = this.isFetchingReplies
        override fun loadMoreReplies(): Observable<Void> = this.loadMoreReplies
        override fun showCommentGuideLinesLink(): Observable<Void> = showGuideLinesLink
        override fun shouldShowPaginationErrorUI(): Observable<Boolean> = this.displayPaginationError
        override fun initialLoadCommentsError(): Observable<Boolean> = this.initialLoadCommentsError
        override fun refresh(): Observable<Void> = this.refresh
    }
}
