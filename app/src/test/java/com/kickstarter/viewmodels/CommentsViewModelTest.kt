package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject

class CommentsViewModelTest : KSRobolectricTestCase() {
    private val closeCommentPage = TestSubscriber<Void>()
    private val commentsList = TestSubscriber<List<CommentCardData>?>()
    private val commentComposerStatus = TestSubscriber<CommentComposerStatus>()
    private val showCommentComposer = TestSubscriber<Boolean>()
    private val showEmptyState = TestSubscriber<Boolean>()
    private val isLoadingMoreItems = TestSubscriber<Boolean>()
    private val isRefreshing = TestSubscriber<Boolean>()
    private val enablePagination = TestSubscriber<Boolean>()
    private val scrollToTop = BehaviorSubject.create<Boolean>()
    private val pullToRefreshError = TestSubscriber.create<Throwable>()
    private val initialLoadError = TestSubscriber.create<Throwable>()
    private val paginationError = TestSubscriber.create<Throwable>()
    private val shouldShowPaginatedCell = TestSubscriber.create<Boolean>()
    private val openCommentGuideLines = TestSubscriber<Void>()

    @Test
    fun testCommentsViewModel_whenUserLoggedInAndBacking_shouldShowEnabledComposer() {
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        // The comment composer should be shown to backer and enabled to write comments
        commentComposerStatus.assertValue(CommentComposerStatus.ENABLED)
        showCommentComposer.assertValues(true, true)
    }

    @Test
    fun testCommentsViewModel_whenUserIsLoggedOut_composerShouldBeGone() {
        val vm = CommentsViewModel.ViewModel(environment().toBuilder().build())

        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // The comment composer should be hidden and disabled to write comments as no user logged-in
        showCommentComposer.assertValue(false)
        commentComposerStatus.assertValue(CommentComposerStatus.GONE)
    }

    @Test
    fun testCommentsViewModel_whenUserIsLoggedInNotBacking_shouldShowDisabledComposer() {
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // The comment composer should show but in disabled state
        commentComposerStatus.assertValue(CommentComposerStatus.DISABLED)
        showCommentComposer.assertValues(true, true)
    }

    @Test
    fun testCommentsViewModel_whenUserIsCreator_shouldShowEnabledComposer() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(currentUser)
            .isBacking(false)
            .build()
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // The comment composer enabled to write comments for creator
        showCommentComposer.assertValues(true, true)
        commentComposerStatus.assertValues(CommentComposerStatus.ENABLED)
    }

    @Test
    fun testCommentsViewModel_enableCommentComposer_notBackingNotCreator() {
        val creator = UserFactory.creator().toBuilder().id(222).build()
        val currentUser = UserFactory.user().toBuilder().id(111).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .isBacking(false)
            .build()
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Comment composer should be disabled and shown the disabled msg if not backing and not creator.
        showCommentComposer.assertValues(true, true)
        commentComposerStatus.assertValue(CommentComposerStatus.DISABLED)
    }
    @Test
    fun testCommentsViewModel_setCurrentUserAvatar() {
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(111).avatar(
            userAvatar
        ).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .isBacking(false)
            .build()

        val vm = CommentsViewModel.ViewModel(
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
    fun testCommentsViewModel_EmptyState() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.empty()
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        val commentsList = TestSubscriber<List<CommentCardData>>()
        vm.outputs.commentsList().subscribe(commentsList)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_InitialLoad_Error() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.empty()
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        val commentsList = TestSubscriber<List<CommentCardData>?>()
        vm.outputs.commentsList().subscribe(commentsList)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_ProjectCommentsEmit() {
        val commentsList = BehaviorSubject.create<List<CommentCardData>?>()
        val vm = CommentsViewModel.ViewModel(environment())
        vm.outputs.commentsList().subscribe(commentsList)
        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // Comments should emit.
        val commentCardDataList = commentsList.value
        assertEquals(1, commentCardDataList?.size)
        assertEquals(CommentFactory.comment(), commentCardDataList?.get(0)?.comment)
    }

    /*
     * test when no comment available
     */
    @Test
    fun testCommentsViewModel_EmptyCommentState() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        vm.outputs.setEmptyState().subscribe(showEmptyState)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        showEmptyState.assertValue(true)
    }

    /*
     * test when comment(s) available
     */
    @Test
    fun testCommentsViewModel_CommentsAvailableState() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(CommentEnvelopeFactory.commentsEnvelope())
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        vm.outputs.setEmptyState().subscribe(showEmptyState)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        showEmptyState.assertValue(false)
    }

    @Test
    fun testCommentsViewModel_ProjectRefresh() {
        val vm = CommentsViewModel.ViewModel(environment())
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        vm.outputs.isRefreshing().subscribe(isRefreshing)

        // Start the view model with a project.
        vm.inputs.refresh()
        vm.outputs.commentsList().subscribe(commentsList)

        // Comments should emit.
        isRefreshing.assertValues(false, true, false)
        commentsList.assertValueCount(1)
    }

    @Test
    fun testCommentsViewModel_ProjectRefresh_AndInitialLoad_withError() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }).build()

        val vm = CommentsViewModel.ViewModel(env)
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        vm.outputs.isRefreshing().subscribe(isRefreshing)
        vm.outputs.pullToRefreshError().subscribe(pullToRefreshError)
        vm.outputs.initialLoadCommentsError().subscribe(initialLoadError)
        vm.outputs.paginateCommentsError().subscribe(paginationError)
        vm.outputs.shouldShowPaginationErrorUI().subscribe(shouldShowPaginatedCell)

        // Start the view model with a project.
        vm.inputs.refresh()
        vm.outputs.commentsList().subscribe(commentsList)

        // Comments should emit.
        isRefreshing.assertValues(true, false, false)
        commentsList.assertValueCount(0)
        initialLoadError.assertValueCount(1)
        paginationError.assertNoValues()
        shouldShowPaginatedCell.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_PaginationWith_Errors() {
        var firstCall = true
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return if (firstCall)
                    Observable.just(CommentEnvelopeFactory.commentsEnvelope())
                else
                    Observable.error(ApiExceptionFactory.badRequestException())
            }
        }).build()

        val vm = CommentsViewModel.ViewModel(env)
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        vm.outputs.enablePagination().subscribe(enablePagination)
        vm.outputs.isLoadingMoreItems().subscribe(isLoadingMoreItems)
        vm.outputs.commentsList().subscribe(commentsList)
        vm.outputs.paginateCommentsError().subscribe(paginationError)
        vm.outputs.shouldShowPaginationErrorUI().subscribe(shouldShowPaginatedCell)

        enablePagination.assertValues(true)

        firstCall = false
        // get the next page which is end of page
        vm.inputs.nextPage()

        enablePagination.assertValues(true)
        commentsList.assertValueCount(1)
        paginationError.assertValueCount(1)
        shouldShowPaginatedCell.assertValue(true)
    }

    /*
  * test Pagination
  */
    @Test
    fun testCommentsViewModel_ProjectLoadingMore() {
        var firstCall = true
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return if (firstCall)
                    Observable.just(CommentEnvelopeFactory.commentsEnvelope())
                else
                    Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
            }
        }).build()

        val vm = CommentsViewModel.ViewModel(env)
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        vm.outputs.enablePagination().subscribe(enablePagination)
        vm.outputs.isLoadingMoreItems().subscribe(isLoadingMoreItems)

        enablePagination.assertValues(true)

        firstCall = false
        // get the next page which is end of page
        vm.inputs.nextPage()
        vm.outputs.commentsList().subscribe(commentsList)

        isLoadingMoreItems.assertValues(false, true, false)
        enablePagination.assertValues(true, false)
        commentsList.assertValueCount(1)
    }

    /*
     * test when comment(s) available
     */
    @Test
    fun testCommentsViewModel_PostComment_CommentAddedToView() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val createdAt = DateTime.now()

        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)
        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        val commentsList = BehaviorSubject.create<List<CommentCardData>>()
        vm.outputs.commentsList().subscribe(commentsList)
        vm.outputs.scrollToTop().subscribe(scrollToTop)

        // post a comment
        vm.inputs.insertNewCommentToList(commentCardData.comment?.body()!!, createdAt)
        assertEquals(2, commentsList.value?.size)
        assertEquals(CommentFactory.comment(), commentsList.value?.last()?.comment)
    }

    @Test
    fun testCommentsViewModel_openCommentGuidelinesLink() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        vm.outputs.showCommentGuideLinesLink().subscribe(openCommentGuideLines)

        // post a comment
        vm.inputs.onShowGuideLinesLinkClicked()
        openCommentGuideLines.assertValueCount(1)
    }

    @Test
    fun backButtonPressed_whenEmits_shouldEmitToCloseActivityStream() {
        val vm = CommentsViewModel.ViewModel(environment())
        vm.outputs.closeCommentsPage().subscribe(closeCommentPage)

        vm.inputs.backPressed()
        closeCommentPage.assertValueCount(1)
    }
}