package com.example.smartstock

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smartstock.ui.session.SessionViewModel
import com.example.smartstock.ui.xml.XmlRoutes
import com.example.smartstock.ui.xml.canAccessXmlRoute
import com.example.smartstock.ui.xml.fragments.HomeFragment
import com.example.smartstock.ui.xml.fragments.InventoryFragment
import com.example.smartstock.ui.xml.fragments.LoginFragment
import com.example.smartstock.ui.xml.fragments.MovementsFragment
import com.example.smartstock.ui.xml.fragments.ProductsFragment
import com.example.smartstock.ui.xml.fragments.ProfileFragment
import com.example.smartstock.ui.xml.fragments.ReportsFragment
import com.example.smartstock.ui.xml.fragments.TasksFragment
import com.example.smartstock.ui.xml.fragments.UsersFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val sessionViewModel: SessionViewModel by viewModels()

    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvHeaderSubtitle: TextView
    private lateinit var navScroll: View

    private lateinit var navButtons: Map<String, Button>

    private var currentRoute: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle)
        navScroll = findViewById(R.id.navScroll)

        navButtons =
            mapOf(
                XmlRoutes.HOME to findViewById(R.id.btnNavHome),
                XmlRoutes.USERS to findViewById(R.id.btnNavUsers),
                XmlRoutes.PRODUCTS to findViewById(R.id.btnNavProducts),
                XmlRoutes.TASKS to findViewById(R.id.btnNavTasks),
                XmlRoutes.INVENTORY to findViewById(R.id.btnNavInventory),
                XmlRoutes.MOVEMENTS to findViewById(R.id.btnNavMovements),
                XmlRoutes.REPORTS to findViewById(R.id.btnNavReports),
                XmlRoutes.PROFILE to findViewById(R.id.btnNavProfile),
            )

        setupNavigationClicks()
        observeSession()
    }

    fun openRoute(route: String, force: Boolean = false) {
        val roleName = sessionViewModel.uiState.value.session?.roleName
        val safeRoute =
            when {
                sessionViewModel.uiState.value.session == null -> XmlRoutes.LOGIN
                canAccessXmlRoute(roleName, route) -> route
                else -> XmlRoutes.HOME
            }

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (!force && safeRoute == currentRoute && currentFragment != null) {
            return
        }

        currentRoute = safeRoute
        val fragment = createFragment(safeRoute)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
        updateSelectedButton(safeRoute)
    }

    private fun setupNavigationClicks() {
        navButtons.forEach { (route, button) ->
            button.setOnClickListener { openRoute(route) }
        }
    }

    private fun observeSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.uiState.collect { state ->
                    val session = state.session
                    if (session == null) {
                        tvHeaderTitle.text = "SmartStock"
                        tvHeaderSubtitle.text = "Inicia sesion para continuar"
                        navScroll.visibility = View.GONE
                        openRoute(XmlRoutes.LOGIN)
                    } else {
                        tvHeaderTitle.text = "SmartStock Mobile"
                        tvHeaderSubtitle.text = "Rol: ${session.roleName}"
                        navScroll.visibility = View.VISIBLE
                        configureNavVisibility(session.roleName)

                        if (!canAccessXmlRoute(session.roleName, currentRoute) || currentRoute == XmlRoutes.LOGIN) {
                            openRoute(XmlRoutes.HOME, force = true)
                        } else {
                            updateSelectedButton(currentRoute)
                        }
                    }
                }
            }
        }
    }

    private fun configureNavVisibility(roleName: String?) {
        navButtons.forEach { (route, button) ->
            button.visibility = if (canAccessXmlRoute(roleName, route)) View.VISIBLE else View.GONE
        }
    }

    private fun updateSelectedButton(route: String) {
        val selectedText = ContextCompat.getColor(this, android.R.color.white)
        val normalText = ContextCompat.getColor(this, R.color.text_primary)

        navButtons.forEach { (buttonRoute, button) ->
            val isSelected = route == buttonRoute
            button.isSelected = isSelected
            button.setTextColor(if (isSelected) selectedText else normalText)
        }
    }

    private fun createFragment(route: String): Fragment =
        when (route) {
            XmlRoutes.LOGIN -> LoginFragment()
            XmlRoutes.HOME -> HomeFragment()
            XmlRoutes.USERS -> UsersFragment()
            XmlRoutes.PRODUCTS -> ProductsFragment()
            XmlRoutes.TASKS -> TasksFragment()
            XmlRoutes.INVENTORY -> InventoryFragment()
            XmlRoutes.MOVEMENTS -> MovementsFragment()
            XmlRoutes.REPORTS -> ReportsFragment()
            XmlRoutes.PROFILE -> ProfileFragment()
            else -> HomeFragment()
        }
}
