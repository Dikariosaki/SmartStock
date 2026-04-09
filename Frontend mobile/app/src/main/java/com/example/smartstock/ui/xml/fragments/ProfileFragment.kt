package com.example.smartstock.ui.xml.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smartstock.R
import com.example.smartstock.ui.session.SessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val sessionViewModel: SessionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName: TextView = view.findViewById(R.id.tvProfileName)
        val tvEmail: TextView = view.findViewById(R.id.tvProfileEmail)
        val tvRole: TextView = view.findViewById(R.id.tvProfileRole)
        val btnLogout: Button = view.findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener { sessionViewModel.logout() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.uiState.collect { state ->
                    val session = state.session
                    tvName.text = session?.name ?: "Usuario"
                    tvEmail.text = "Correo: ${session?.email ?: "-"}"
                    tvRole.text = "Rol: ${session?.roleName ?: "-"}"
                }
            }
        }
    }
}
