package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.databinding.ActivityNewsletterBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.ui.data.Newsletter
import com.kickstarter.viewmodels.NewsletterViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(NewsletterViewModel.ViewModel::class)
class NewsletterActivity : BaseActivity<NewsletterViewModel.ViewModel>() {

    private lateinit var build: Build
    private lateinit var currentUserType: CurrentUserType
    private lateinit var ksString: KSString

    private lateinit var binding: ActivityNewsletterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsletterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.build = environment().build()
        this.currentUserType = environment().currentUser()
        this.ksString = environment().ksString()

        this.viewModel.outputs.user()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::displayPreferences)

        this.viewModel.errors.unableToSavePreferenceError()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this, getString(R.string.profile_settings_error)) }

        this.viewModel.outputs.showOptInPrompt()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showOptInPrompt)

        this.viewModel.outputs.subscribeAll()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { SwitchCompatUtils.setCheckedWithoutAnimation(binding.subscribeAllSwitch, it) }

        binding.alumniSwitch.setOnClickListener { viewModel.inputs.sendAlumniNewsletter(binding.alumniSwitch.isChecked) }
        binding.artsNewsSwitch.setOnClickListener { viewModel.inputs.sendArtsNewsNewsletter(binding.artsNewsSwitch.isChecked) }
        binding.filmsSwitch.setOnClickListener { viewModel.inputs.sendFilmsNewsletter(binding.filmsSwitch.isChecked) }
        binding.gamesWeLoveSwitch.setOnClickListener { viewModel.inputs.sendGamesNewsletter(binding.gamesWeLoveSwitch.isChecked) }
        binding.happeningSwitch.setOnClickListener { viewModel.inputs.sendHappeningNewsletter(binding.happeningSwitch.isChecked) }
        binding.inventSwitch.setOnClickListener { viewModel.inputs.sendInventNewsletter(binding.inventSwitch.isChecked) }
        binding.musicSwitch.setOnClickListener { viewModel.inputs.sendMusicNewsletter(binding.musicSwitch.isChecked) }
        binding.newsEventsSwitch.setOnClickListener { viewModel.inputs.sendPromoNewsletter(binding.newsEventsSwitch.isChecked) }
        binding.projectsWeLoveSwitch.setOnClickListener { viewModel.inputs.sendWeeklyNewsletter(binding.projectsWeLoveSwitch.isChecked) }
        binding.readsSwitch.setOnClickListener { viewModel.inputs.sendReadsNewsletter(binding.readsSwitch.isChecked) }
        binding.subscribeAllSwitch.setOnClickListener { viewModel.inputs.sendAllNewsletter(binding.subscribeAllSwitch.isChecked) }
    }

    private fun displayPreferences(@NonNull user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.alumniSwitch, isTrue(user.alumniNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.artsNewsSwitch, isTrue(user.artsCultureNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.filmsSwitch, isTrue(user.filmNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.gamesWeLoveSwitch, isTrue(user.gamesNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.happeningSwitch, isTrue(user.happeningNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.inventSwitch, isTrue(user.inventNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.musicSwitch, isTrue(user.musicNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.newsEventsSwitch, isTrue(user.promoNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.projectsWeLoveSwitch, isTrue(user.weeklyNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.readsSwitch, isTrue(user.publishingNewsletter()))
    }

    private fun newsletterString(@NonNull newsletter: Newsletter): String? {
        return when (newsletter) {
            Newsletter.ALL -> getString(R.string.profile_settings_newsletter_subscribe_all)
            Newsletter.ALUMNI -> getString(R.string.profile_settings_newsletter_alumni)
            Newsletter.ARTS -> getString(R.string.profile_settings_newsletter_arts)
            Newsletter.FILMS -> getString(R.string.profile_settings_newsletter_film)
            Newsletter.GAMES -> getString(R.string.profile_settings_newsletter_games)
            Newsletter.HAPPENING -> getString(R.string.profile_settings_newsletter_happening)
            Newsletter.INVENT -> getString(R.string.profile_settings_newsletter_invent)
            Newsletter.MUSIC -> getString(R.string.Its_like_the_radio_but_nothing_sucks_and_also_its_a_newsletter)
            Newsletter.PROMO -> getString(R.string.profile_settings_newsletter_promo)
            Newsletter.READS -> getString(R.string.profile_settings_newsletter_publishing)
            Newsletter.WEEKLY -> getString(R.string.profile_settings_newsletter_weekly)
        }
    }

    private fun showOptInPrompt(newsletter: Newsletter) {
        val string = newsletterString(newsletter)

        val optInDialogMessageString = this.ksString.format(getString(R.string.profile_settings_newsletter_opt_in_message), "newsletter", string)
        ViewUtils.showDialog(this, getString(R.string.profile_settings_newsletter_opt_in_title), optInDialogMessageString)
    }
}
