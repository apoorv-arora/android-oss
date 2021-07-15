package com.kickstarter.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.adapters.ExpandableHeaderAdapter
import com.kickstarter.ui.adapters.RewardCardAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.setTextAndSelection
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.itemdecorations.RewardCardItemDecoration
import com.kickstarter.viewmodels.PledgeFragmentViewModel
import com.stripe.android.ApiResultCallback
import com.stripe.android.SetupIntentResult
import kotlinx.android.synthetic.main.fragment_pledge.deadline_warning
import kotlinx.android.synthetic.main.fragment_pledge.divider_total
import kotlinx.android.synthetic.main.fragment_pledge.pledge_content
import kotlinx.android.synthetic.main.fragment_pledge.pledge_root
import kotlinx.android.synthetic.main.fragment_pledge.shipping_rules_row
import kotlinx.android.synthetic.main.fragment_pledge.shipping_rules_row_static
import kotlinx.android.synthetic.main.fragment_pledge_section_accountability.accountability
import kotlinx.android.synthetic.main.fragment_pledge_section_accountability.accountability_container
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.bonus_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.bonus_container
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.bonus_maximum
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.bonus_support_label
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.bonus_symbol_end
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.bonus_symbol_start
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.decrease_bonus
import kotlinx.android.synthetic.main.fragment_pledge_section_bonus_support.increase_bonus
import kotlinx.android.synthetic.main.fragment_pledge_section_editable_shipping.shipping_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_editable_shipping.shipping_amount_loading_view
import kotlinx.android.synthetic.main.fragment_pledge_section_editable_shipping.shipping_rules
import kotlinx.android.synthetic.main.fragment_pledge_section_editable_shipping.shipping_rules_label
import kotlinx.android.synthetic.main.fragment_pledge_section_editable_shipping.view.shipping_rules
import kotlinx.android.synthetic.main.fragment_pledge_section_footer.pledge_footer_continue_button
import kotlinx.android.synthetic.main.fragment_pledge_section_footer.pledge_footer_pledge_agreement
import kotlinx.android.synthetic.main.fragment_pledge_section_footer.pledge_footer_pledge_button
import kotlinx.android.synthetic.main.fragment_pledge_section_footer.pledge_footer_pledge_button_progress
import kotlinx.android.synthetic.main.fragment_pledge_section_header_reward_sumary.header_arrow_button
import kotlinx.android.synthetic.main.fragment_pledge_section_header_reward_sumary.header_summary_list
import kotlinx.android.synthetic.main.fragment_pledge_section_header_reward_sumary.pledge_header_container
import kotlinx.android.synthetic.main.fragment_pledge_section_header_reward_sumary.pledge_header_estimated_delivery_label
import kotlinx.android.synthetic.main.fragment_pledge_section_header_reward_sumary.pledge_header_summary_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_payment.cards_recycler
import kotlinx.android.synthetic.main.fragment_pledge_section_payment.payment_container
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.additional_pledge_amount_container
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.decrease_pledge
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.increase_pledge
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.pledge_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.pledge_amount_label
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.pledge_container
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.pledge_maximum
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.pledge_minimum
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.pledge_symbol_end
import kotlinx.android.synthetic.main.fragment_pledge_section_pledge_amount.pledge_symbol_start
import kotlinx.android.synthetic.main.fragment_pledge_section_reward_summary.pledge_header_container_no_reward
import kotlinx.android.synthetic.main.fragment_pledge_section_reward_summary.pledge_header_title_no_reward
import kotlinx.android.synthetic.main.fragment_pledge_section_shipping.shipping_amount_static
import kotlinx.android.synthetic.main.fragment_pledge_section_shipping.shipping_rules_static
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_bonus.bonus_summary
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_bonus.bonus_summary_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_pledge.pledge_summary
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_pledge.pledge_summary_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_shipping.shipping_label
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_shipping.shipping_summary
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_shipping.shipping_summary_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_total.total_amount
import kotlinx.android.synthetic.main.fragment_pledge_section_total.total_amount_conversion
import kotlinx.android.synthetic.main.fragment_pledge_section_total.total_amount_loading_view

@RequiresFragmentViewModel(PledgeFragmentViewModel.ViewModel::class)
class PledgeFragment : BaseFragment<PledgeFragmentViewModel.ViewModel>(), RewardCardAdapter.Delegate, ShippingRulesAdapter.Delegate {

    interface PledgeDelegate {
        fun pledgePaymentSuccessfullyUpdated()
        fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>)
        fun pledgeSuccessfullyUpdated()
    }

    private lateinit var adapter: ShippingRulesAdapter
    private var headerAdapter = ExpandableHeaderAdapter()
    private var isExpanded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_pledge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCardsAdapter()
        setUpShippingAdapter()
        setupRewardRecyclerView()

        pledge_amount.onChange { this.viewModel.inputs.pledgeInput(it) }

        bonus_amount.onChange { this.viewModel.inputs.bonusInput(it) }

        this.viewModel.outputs.additionalPledgeAmountIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(additional_pledge_amount_container, it) }

        this.viewModel.outputs.pledgeSectionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(pledge_container, it) }

        this.viewModel.outputs.decreasePledgeButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { decrease_pledge.isEnabled = it }

        this.viewModel.outputs.increasePledgeButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { increase_pledge.isEnabled = it }

        this.viewModel.outputs.headerSectionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setGone(pledge_header_container, it)
            }

        this.viewModel.outputs.decreaseBonusButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { decrease_bonus.isEnabled = it }

        this.viewModel.outputs.increaseBonusButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { increase_bonus.isEnabled = it }

        this.viewModel.outputs.estimatedDelivery()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                pledge_header_estimated_delivery_label.text = pledge_header_estimated_delivery_label.text.toString() + " " + it
            }

        this.viewModel.outputs.estimatedDeliveryInfoIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setGone(pledge_header_estimated_delivery_label)
            }

        this.viewModel.outputs.continueButtonIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(pledge_footer_continue_button, it) }

        this.viewModel.outputs.conversionTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe(ViewUtils.setGone(total_amount_conversion))

        this.viewModel.outputs.conversionText()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setConversionTextView(it) }

        this.viewModel.outputs.paymentContainerIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(payment_container, it) }

        this.viewModel.outputs.showSelectedCard()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { updatePledgeCardState(it) }

        this.viewModel.outputs.pledgeAmountHeader()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                pledge_header_summary_amount.text = it
            }

        this.viewModel.outputs.pledgeAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                pledge_amount.setTextAndSelection(it)
            }

        this.viewModel.outputs.bonusAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                bonus_amount.setTextAndSelection(it)
                bonus_summary_amount.text = it
            }

        this.viewModel.outputs.pledgeHint()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { pledge_amount.hint = it }

        this.viewModel.outputs.bonusHint()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { bonus_amount.hint = it }

        this.viewModel.outputs.pledgeMaximum()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                setPledgeMaximumText(it)
            }

        this.viewModel.outputs.pledgeMaximumIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setInvisible(pledge_maximum, it)
                ViewUtils.setInvisible(bonus_maximum, it)
            }

        this.viewModel.outputs.pledgeMinimum()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setPledgeMinimumText(it) }

        this.viewModel.outputs.projectCurrencySymbol()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                setCurrencySymbols(it)
            }

        this.viewModel.outputs.pledgeTextColor()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setTextColor(it, pledge_amount, pledge_symbol_start, pledge_symbol_end) }

        this.viewModel.outputs.cardsAndProject()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { (cards_recycler.adapter as RewardCardAdapter).takeCards(it.first, it.second) }

        this.viewModel.outputs.addedCard()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                val position = (cards_recycler.adapter as RewardCardAdapter).insertCard(it)
                this.viewModel.inputs.addedCardPosition(position)
            }

        this.viewModel.outputs.startLoginToutActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { startActivity(Intent(this.context, LoginToutActivity::class.java)) }

        this.viewModel.outputs.showNewCardFragment()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                fragmentManager
                    ?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                    ?.add(R.id.secondary_container, NewCardFragment.newInstance(true, it), NewCardFragment::class.java.simpleName)
                    ?.addToBackStack(NewCardFragment::class.java.simpleName)
                    ?.commit()
            }

        this.viewModel.outputs.selectedShippingRule()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                shipping_rules.setText(it.toString())
                shipping_rules_static.text = it.toString()
            }

        this.viewModel.outputs.shippingAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setGone(shipping_amount_loading_view, true)
                setPlusTextView(shipping_amount, it)
                setPlusTextView(shipping_amount_static, it)
            }

        this.viewModel.outputs.shippingSummaryAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setPlusTextView(shipping_summary_amount, it) }

        this.viewModel.outputs.shippingSummaryLocation()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { shipping_label.text = String.format("%s: %s", getString(R.string.Shipping), it) }

        this.viewModel.outputs.shippingRulesAndProject()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .filter { ObjectUtils.isNotNull(context) }
            .subscribe {
                displayShippingRules(it.first, it.second)
            }

        this.viewModel.outputs.shippingRulesSectionIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setGone(shipping_rules_row, it)
            }

        this.viewModel.outputs.shippingRuleStaticIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setGone(shipping_rules_row_static, it)
            }

        this.viewModel.outputs.shippingSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(shipping_summary, it) }

        this.viewModel.outputs.pledgeSummaryAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { pledge_summary_amount.text = it }

        this.viewModel.outputs.bonusSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(bonus_summary, it) }

        this.viewModel.outputs.bonusSummaryAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { bonus_summary_amount.text = it }

        this.viewModel.outputs.pledgeSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(pledge_summary, it) }

        this.viewModel.outputs.totalDividerIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(divider_total, it) }

        this.viewModel.outputs.totalAmount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                ViewUtils.setGone(total_amount_loading_view, true)
                total_amount.text = it
            }

        this.viewModel.outputs.totalAndDeadline()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setDeadlineWarning(it) }

        this.viewModel.outputs.totalAndDeadlineIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setInvisible(deadline_warning, false) }

        this.viewModel.outputs.showPledgeSuccess()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { (activity as PledgeDelegate?)?.pledgeSuccessfullyCreated(it) }

        this.viewModel.outputs.showSCAFlow()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { this.viewModel.environment.stripe().handleNextActionForSetupIntent(this, it) }

        this.viewModel.outputs.showPledgeError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { activity?.applicationContext?.let { showErrorToast(it, pledge_content, getString(R.string.general_error_something_wrong)) } }

        this.viewModel.outputs.startChromeTab()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                activity?.let { activity ->
                    ChromeTabsHelperActivity.openCustomTab(activity, UrlUtils.baseCustomTabsIntent(activity), Uri.parse(it), null)
                }
            }

        this.viewModel.outputs.baseUrlForTerms()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { setHtmlStrings(it) }

        this.viewModel.outputs.showUpdatePledgeError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { activity?.applicationContext?.let { showErrorToast(it, pledge_content, getString(R.string.general_error_something_wrong)) } }

        this.viewModel.outputs.showUpdatePledgeSuccess()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { (activity as PledgeDelegate?)?.pledgeSuccessfullyUpdated() }

        this.viewModel.outputs.showUpdatePaymentError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { activity?.applicationContext?.let { showErrorToast(it, pledge_content, getString(R.string.general_error_something_wrong)) } }

        this.viewModel.outputs.showUpdatePaymentSuccess()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { (activity as PledgeDelegate?)?.pledgePaymentSuccessfullyUpdated() }

        this.viewModel.outputs.pledgeButtonCTA()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { pledge_footer_pledge_button.setText(it) }

        this.viewModel.outputs.pledgeButtonIsEnabled()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { pledge_footer_pledge_button.isEnabled = it }

        this.viewModel.outputs.pledgeButtonIsGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { ViewUtils.setInvisible(pledge_footer_pledge_button, it) }

        this.viewModel.outputs.pledgeProgressIsGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { ViewUtils.setGone(pledge_footer_pledge_button_progress, it) }

        this.viewModel.outputs.continueButtonIsEnabled()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { pledge_footer_continue_button.isEnabled = it }

        this.viewModel.outputs.headerSelectedItems()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                populateHeaderItems(it)
            }

        this.viewModel.outputs.isPledgeMinimumSubtitleGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                ViewUtils.setGone(pledge_minimum, it)
            }

        this.viewModel.outputs.isBonusSupportSectionGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                ViewUtils.setGone(bonus_container, it)
                pledge_container.setPadding(0, resources.getDimension(R.dimen.grid_4).toInt(), 0, 0)
            }

        this.viewModel.outputs.isNoReward()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                ViewUtils.setGone(pledge_header_container, it)
                pledge_header_container_no_reward.visibility = View.VISIBLE
            }

        this.viewModel.outputs.projectTitle()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe {
                pledge_header_title_no_reward.text = it
            }

        pledge_amount.setOnTouchListener { _, _ ->
            pledge_amount.post {
                pledge_root.smoothScrollTo(0, relativeTop(pledge_amount_label, pledge_root))
                pledge_amount.requestFocus()
            }
            false
        }

        bonus_amount.setOnTouchListener { _, _ ->
            bonus_amount.post {
                pledge_root.smoothScrollTo(0, relativeTop(bonus_support_label, pledge_root))
                bonus_amount.requestFocus()
            }
            false
        }

        shipping_rules.setOnTouchListener { _, _ ->
            shipping_rules_label.post {
                pledge_root.smoothScrollTo(0, relativeTop(shipping_rules_label, pledge_root))
                shipping_rules.requestFocus()
                shipping_rules.showDropDown()
            }
            false
        }

        decrease_pledge.setOnClickListener {
            this.viewModel.inputs.decreasePledgeButtonClicked()
        }

        increase_pledge.setOnClickListener {
            this.viewModel.inputs.increasePledgeButtonClicked()
        }

        pledge_header_container.setOnClickListener {
            toggleAnimation(isExpanded)
        }

        decrease_bonus.setOnClickListener { this.viewModel.inputs.decreaseBonusButtonClicked() }

        increase_bonus.setOnClickListener { this.viewModel.inputs.increaseBonusButtonClicked() }

        RxView.clicks(pledge_footer_pledge_button)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.pledgeButtonClicked() }

        RxView.clicks(pledge_footer_continue_button)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.continueButtonClicked() }
    }

    private fun populateHeaderItems(selectedItems: List<Pair<Project, Reward>>) {
        headerAdapter.populateData(selectedItems)
    }

    private fun toggleAnimation(isExpanded: Boolean) {
        if (isExpanded)
            collapseAnimation()
        else
            expandAnimation()

        this.isExpanded = !isExpanded
    }

    private fun expandAnimation() {
        header_arrow_button.animate().rotation(180f).start()

        val constraintSet = ConstraintSet()
        constraintSet.clone(pledge_header_container)
        constraintSet.clear(R.id.header_summary_list, ConstraintSet.BOTTOM)

        val transition = ChangeBounds()
        transition.duration = 100

        TransitionManager.beginDelayedTransition(pledge_header_container, transition)
        constraintSet.applyTo(pledge_header_container)
    }

    private fun collapseAnimation() {
        header_arrow_button.animate().rotation(0f).start()

        val constraintSet = ConstraintSet()
        constraintSet.clone(pledge_header_container)
        constraintSet.connect(R.id.header_summary_list, ConstraintSet.BOTTOM, R.id.header_animation_guideline, ConstraintSet.BOTTOM)

        val transition = ChangeBounds()
        transition.duration = 100

        TransitionManager.beginDelayedTransition(pledge_header_container, transition)
        constraintSet.applyTo(pledge_header_container)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val stripe = this.viewModel.environment.stripe()
        stripe.onSetupResult(
            requestCode, data,
            object : ApiResultCallback<SetupIntentResult> {
                override fun onSuccess(result: SetupIntentResult) {
                    this@PledgeFragment.viewModel.inputs.stripeSetupResultSuccessful(result.outcome)
                }

                override fun onError(e: Exception) {
                    this@PledgeFragment.viewModel.inputs.stripeSetupResultUnsuccessful(e)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        cards_recycler?.adapter = null
        header_summary_list?.adapter = null
        this.viewModel = null
    }

    override fun addNewCardButtonClicked() {
        this.viewModel.inputs.newCardButtonClicked()
    }

    fun cardAdded(storedCard: StoredCard) {
        this.viewModel.inputs.cardSaved(storedCard)
    }

    override fun cardSelected(storedCard: StoredCard, position: Int) {
        this.viewModel.inputs.cardSelected(storedCard, position)
    }

    override fun ruleSelected(rule: ShippingRule) {
        this.viewModel.inputs.shippingRuleSelected(rule)
        activity?.hideKeyboard()
        shipping_rules.clearFocus()

        if (!pledge_footer_pledge_button.isEnabled) {
            pledge_footer_pledge_button.isEnabled = true
        }
    }

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        shipping_rules.isEnabled = true
        adapter.populateShippingRules(shippingRules, project)
    }

    private fun relativeTop(view: View, parent: ViewGroup): Int {
        val offsetViewBounds = Rect()
        view.getDrawingRect(offsetViewBounds)
        parent.offsetDescendantRectToMyCoords(view, offsetViewBounds)

        return offsetViewBounds.top - parent.paddingTop
    }

    private fun setCurrencySymbols(symbolAndStart: Pair<SpannableString, Boolean>) {
        val symbol = symbolAndStart.first
        val symbolAtStart = symbolAndStart.second
        if (symbolAtStart) {
            pledge_symbol_start.text = symbol
            pledge_symbol_end.text = null

            bonus_symbol_start.text = symbol
            bonus_symbol_end.text = null
        } else {
            pledge_symbol_start.text = null
            pledge_symbol_end.text = symbol

            bonus_symbol_start.text = null
            bonus_symbol_end.text = symbol
        }
    }

    private fun setDeadlineWarning(totalAndDeadline: Pair<String, String>) {
        val total = totalAndDeadline.first
        val deadline = totalAndDeadline.second
        val ksString = this.viewModel.environment.ksString()
        val warning = ksString.format(
            getString(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline),
            "total", total,
            "project_deadline", deadline
        )

        val spannableWarning = SpannableString(warning)

        ViewUtils.addBoldSpan(spannableWarning, total)
        ViewUtils.addBoldSpan(spannableWarning, deadline)

        deadline_warning.text = spannableWarning
    }

    private fun setPledgeMaximumText(maximumAmount: String) {
        val ksString = this.viewModel.environment.ksString()
        pledge_maximum.text = ksString.format(getString(R.string.Enter_an_amount_less_than_max_pledge), "max_pledge", maximumAmount)
        bonus_maximum.text = ksString.format(getString(R.string.Enter_an_amount_less_than_max_pledge), "max_pledge", maximumAmount)
    }

    private fun setPledgeMinimumText(minimumAmount: String) {
        val ksString = this.viewModel.environment.ksString()
        pledge_minimum.text = ksString.format(getString(R.string.The_minimum_pledge_is_min_pledge), "min_pledge", minimumAmount)
    }

    private fun setPlusTextView(textView: TextView, localizedAmount: CharSequence) {
        val ksString = this.viewModel.environment.ksString()
        textView.contentDescription = ksString.format(getString(R.string.plus_shipping_cost), "shipping_cost", localizedAmount.toString())
        textView.text = localizedAmount
    }

    private fun setTextColor(colorResId: Int, vararg textViews: TextView) {
        context?.let {
            val color = ContextCompat.getColor(it, colorResId)
            for (textView in textViews) {
                textView.setTextColor(color)
            }
        }
    }

    private fun setUpCardsAdapter() {
        cards_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        cards_recycler.adapter = RewardCardAdapter(this)
        cards_recycler.addItemDecoration(RewardCardItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_1)))
    }

    private fun setUpShippingAdapter() {
        context?.let {
            adapter = ShippingRulesAdapter(it, R.layout.item_shipping_rule, arrayListOf(), this)
            shipping_rules.setAdapter(adapter)
        }

        this.view?.shipping_rules?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().isNullOrBlank()) {
                    pledge_footer_pledge_button.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun setupRewardRecyclerView() {
        header_summary_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        header_summary_list.adapter = headerAdapter
    }

    private fun setClickableHtml(string: String, textView: TextView) {
        val spannableBuilder = SpannableStringBuilder(ViewUtils.html(string))
        // https://stackoverflow.com/a/19989677
        val urlSpans = spannableBuilder.getSpans(0, string.length, URLSpan::class.java)
        for (urlSpan in urlSpans) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    this@PledgeFragment.viewModel.inputs.linkClicked(urlSpan.url)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = ContextCompat.getColor(textView.context, R.color.accent)
                }
            }
            val spanStart = spannableBuilder.getSpanStart(urlSpan)
            val spanEnd = spannableBuilder.getSpanEnd(urlSpan)
            val spanFlags = spannableBuilder.getSpanFlags(urlSpan)
            spannableBuilder.setSpan(clickableSpan, spanStart, spanEnd, spanFlags)
            spannableBuilder.removeSpan(urlSpan)
        }

        textView.text = spannableBuilder
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setConversionTextView(@NonNull amount: String) {
        val currencyConversionString = context?.getString(R.string.About_reward_amount)
        total_amount_conversion.text = (
            currencyConversionString?.let {
                this.viewModel.environment.ksString().format(it, "reward_amount", amount)
            }
            )
    }

    @SuppressLint("SetTextI18n")
    private fun setHtmlStrings(baseUrl: String) {
        val termsOfUseUrl = UrlUtils.appendPath(baseUrl, HelpActivity.TERMS_OF_USE)
        val cookiePolicyUrl = UrlUtils.appendPath(baseUrl, HelpActivity.COOKIES)
        val privacyPolicyUrl = UrlUtils.appendPath(baseUrl, HelpActivity.PRIVACY)

        val ksString = (activity?.applicationContext as KSApplication).component().environment().ksString()
        val byPledgingYouAgree = getString(R.string.By_pledging_you_agree_to_Kickstarters_Terms_of_Use_Privacy_Policy_and_Cookie_Policy)

        val agreementWithUrls = ksString.format(
            byPledgingYouAgree, "terms_of_use_link", termsOfUseUrl,
            "privacy_policy_link", privacyPolicyUrl, "cookie_policy_link", cookiePolicyUrl
        )

        setClickableHtml(agreementWithUrls, pledge_footer_pledge_agreement)

        val trustUrl = UrlUtils.appendPath(baseUrl, "trust")
        val accountabilityWithUrl = ksString.format(
            getString(R.string.Its_a_way_to_bring_creative_projects_to_life_Learn_more_about_accountability),
            "trust_link",
            trustUrl
        )

        setClickableHtml(accountabilityWithUrl, accountability)
        accountability_container.setOnClickListener {
            this.viewModel.inputs.linkClicked(trustUrl)
        }
    }

    private fun updatePledgeCardState(positionAndCardState: Pair<Int, CardState>) {
        val position = positionAndCardState.first
        val cardState = positionAndCardState.second
        val rewardCardAdapter = cards_recycler.adapter as RewardCardAdapter

        if (cardState == CardState.SELECTED) {
            rewardCardAdapter.setSelectedPosition(position)
        } else {
            rewardCardAdapter.resetSelectedPosition()
        }
    }

    companion object {
        fun newInstance(pledgeData: PledgeData, pledgeReason: PledgeReason): PledgeFragment {
            val fragment = PledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, pledgeData)
            argument.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeReason)
            fragment.arguments = argument
            return fragment
        }
    }
}
