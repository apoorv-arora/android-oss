package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.stripe.android.model.CardBrand
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.Date
import java.util.GregorianCalendar

class RewardCardSelectedViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardCardSelectedViewHolderViewModel.ViewModel

    private val expirationDate = TestSubscriber.create<String>()
    private val id = TestSubscriber.create<String>()
    private val issuer = TestSubscriber.create<String>()
    private val issuerImage = TestSubscriber.create<Int>()
    private val lastFour = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = RewardCardSelectedViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.expirationDate().subscribe(this.expirationDate)
        this.vm.outputs.issuer().subscribe(this.issuer)
        this.vm.outputs.issuerImage().subscribe(this.issuerImage)
        this.vm.outputs.lastFour().subscribe(this.lastFour)
    }

    @Test
    fun testExpirationDate() {
        setUpEnvironment(environment())
        val calendar = GregorianCalendar(2019, 2, 1)
        val date: Date = calendar.time

        val creditCard = StoredCardFactory.discoverCard()
            .toBuilder()
            .expiration(date)
            .build()
        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.expirationDate.assertValue("03/2019")
    }

    @Test
    fun testIssuerImage() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuerImage.assertValue(R.drawable.discover_md)
    }

    @Test
    fun testIssuer() {
        setUpEnvironment(environment())
        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.issuer.assertValue(CardBrand.Discover.code)
    }

    @Test
    fun testLastFourDigits() {
        setUpEnvironment(environment())

        val creditCard = StoredCardFactory.discoverCard()

        this.vm.inputs.configureWith(Pair(creditCard, ProjectFactory.project()))

        this.lastFour.assertValue("1234")
    }
}
