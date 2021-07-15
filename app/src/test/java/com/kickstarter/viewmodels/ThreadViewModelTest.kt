package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentCardDataFactory
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Comment
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject
import java.io.IOException

class ThreadViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ThreadViewModel.ViewModel
    private val getComment = TestSubscriber<Comment>()
    private val focusCompose = TestSubscriber<Boolean>()
    private val onReplies = TestSubscriber<Pair<List<CommentCardData>, Boolean>>()

    private val replyComposerStatus = TestSubscriber<CommentComposerStatus>()
    private val showReplyComposer = TestSubscriber<Boolean>()
    private val loadMoreReplies = TestSubscriber<Void>()
    private val openCommentGuideLines = TestSubscriber<Void>()
    private val refresh = TestSubscriber<Void>()

    private fun setUpEnvironment() {
        setUpEnvironment(environment())
    }

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ThreadViewModel.ViewModel(environment)
        this.vm.getRootComment().subscribe(getComment)
        this.vm.shouldFocusOnCompose().subscribe(focusCompose)
        this.vm.onCommentReplies().subscribe(onReplies)
        this.vm.loadMoreReplies().subscribe(loadMoreReplies)
        this.vm.refresh().subscribe(refresh)
    }

    @Test
    fun testGetRootComment() {
        setUpEnvironment()

        val commentCardData = CommentCardDataFactory.commentCardData()

        this.vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))
        getComment.assertValue(commentCardData.comment)

        this.vm.intent(Intent().putExtra("Some other Key", commentCardData))
        getComment.assertValue(commentCardData.comment)
    }

    @Test
    fun testShouldFocusCompose() {
        setUpEnvironment()

        this.vm.intent(Intent().putExtra(IntentKey.REPLY_EXPAND, false))
        focusCompose.assertValue(false)

        this.vm.intent(Intent().putExtra("Some other Key", false))
        focusCompose.assertValues(false)

        this.vm.intent(Intent().putExtra(IntentKey.REPLY_EXPAND, true))
        focusCompose.assertValues(false, true)
    }

    @Test
    fun testThreadViewModel_setCurrentUserAvatar() {
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(111).avatar(
            userAvatar
        ).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .isBacking(false)
            .build()

        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )
        val currentUserAvatar = TestSubscriber<String?>()
        vm.outputs.currentUserAvatar().subscribe(currentUserAvatar)
        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // set user avatar with small url
        currentUserAvatar.assertValue(userAvatar.small())
    }

    @Test
    fun testThreadViewModel_whenUserLoggedInAndBacking_shouldShowEnabledComposer() {
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardDataBacked()))

        // The comment composer should be shown to backer and enabled to write comments
        replyComposerStatus.assertValue(CommentComposerStatus.ENABLED)
        showReplyComposer.assertValues(true, true)
    }

    @Test
    fun testThreadViewModel_whenUserIsLoggedOut_composerShouldBeGone() {
        val vm = ThreadViewModel.ViewModel(environment().toBuilder().build())

        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))

        // The comment composer should be hidden and disabled to write comments as no user logged-in
        showReplyComposer.assertValue(false)
        replyComposerStatus.assertValue(CommentComposerStatus.GONE)
    }

    @Test
    fun testThreadViewModel_whenUserIsLoggedInNotBacking_shouldShowDisabledComposer() {
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))

        // The comment composer should show but in disabled state
        replyComposerStatus.assertValue(CommentComposerStatus.DISABLED)
        showReplyComposer.assertValues(true, true)
    }

    @Test
    fun testThreadViewModel_whenUserIsCreator_shouldShowEnabledComposer() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(currentUser)
            .isBacking(false)
            .build()
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardDataBacked()))

        // The comment composer enabled to write comments for creator
        showReplyComposer.assertValues(true, true)
        replyComposerStatus.assertValues(CommentComposerStatus.ENABLED)
    }

    @Test
    fun testThreadViewModel_enableCommentComposer_notBackingNotCreator() {
        val creator = UserFactory.creator().toBuilder().id(222).build()
        val currentUser = UserFactory.user().toBuilder().id(111).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .isBacking(false)
            .build()
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))

        // Comment composer should be disabled and shown the disabled msg if not backing and not creator.
        showReplyComposer.assertValues(true, true)
        replyComposerStatus.assertValue(CommentComposerStatus.DISABLED)
    }

    @Test
    fun testLoadCommentReplies_Successful() {
        val createdAt = DateTime.now()
        val env = environment().toBuilder()
            .apolloClient(object : MockApolloClient() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(CommentEnvelopeFactory.repliesCommentsEnvelope(createdAt = createdAt))
                }
            })
            .build()

        setUpEnvironment(env)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))

        this.onReplies.assertValueCount(2)
    }

    @Test
    fun testLoadCommentReplies_Error() {
        val createdAt = DateTime.now()
        val env = environment().toBuilder()
            .apolloClient(object : MockApolloClient() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.error(IOException())
                }
            })
            .build()

        setUpEnvironment(env)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))

        this.onReplies.assertValueCount(0)

        vm.reloadRepliesPage()
        this.refresh.assertValueCount(1)
    }

    @Test
    fun testLoadCommentReplies_pagination() {
        val createdAt = DateTime.now()
        val replies = CommentEnvelopeFactory.repliesCommentsEnvelopeHasPrevious(createdAt = createdAt)
        val env = environment().toBuilder()
            .apolloClient(object : MockApolloClient() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(replies)
                }
            })
            .build()

        setUpEnvironment(env)

        val onReplies = BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        this.vm.onCommentReplies().subscribe(onReplies)

        // Start the view model with a backed project and comment.
        vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))

        val onRepliesResult = onReplies.value

        assertEquals(replies.comments?.size, onRepliesResult?.first?.size)
        assertEquals(true, onRepliesResult?.second)

        vm.inputs.reloadRepliesPage()

        this.loadMoreReplies.assertValueCount(1)
    }

    @Test
    fun testThreadsViewModel_openCommentGuidelinesLink() {
        setUpEnvironment()

        this.vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, CommentCardDataFactory.commentCardData()))

        // Start the view model with an update.
        vm.outputs.showCommentGuideLinesLink().subscribe(openCommentGuideLines)

        // post a comment
        vm.inputs.onShowGuideLinesLinkClicked()
        openCommentGuideLines.assertValueCount(1)
    }
}
