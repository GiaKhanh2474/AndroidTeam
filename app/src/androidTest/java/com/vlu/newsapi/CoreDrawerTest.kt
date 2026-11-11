package com.vlu.newsapi

import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Test

class CoreDrawerTest {
    @Test fun existingMarketBackActionClosesPrivateStackAfterRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.openMarketDrawer()
                val host = activity.supportFragmentManager
                    .findFragmentById(R.id.drawer_market_fragment_container) as NavHostFragment
                assertEquals(R.id.marketDrawerFragment, host.navController.currentDestination?.id)
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager
                    .findFragmentById(R.id.drawer_market_fragment_container) as NavHostFragment
                assertEquals(R.id.marketDrawerFragment, host.navController.currentDestination?.id)
                host.navController.navigateUp()
                assertEquals(R.id.mainFragment, host.navController.currentDestination?.id)
            }
        }
    }
}
