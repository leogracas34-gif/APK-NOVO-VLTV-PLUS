package com.vltv.plus.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vltv.plus.databinding.FragmentHomeBinding

/**
 * HomeFragment do VLTV+: Interface dinâmica estilo Disney+
 * Gerencia os botões de TV ao Vivo, Filmes e Séries.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
        setupFocusSystem()
    }

    private fun setupNavigation() {
        // Lógica para TV ao Vivo
        binding.cardLiveTv.setOnClickListener {
            // Futura implementação: Abrir lista de canais
        }

        // Lógica para Filmes
        binding.cardMovies.setOnClickListener {
            // Futura implementação: Abrir categorias de filmes (TMDB logos)
        }

        // Lógica para Séries
        binding.cardSeries.setOnClickListener {
            // Futura implementação: Abrir categorias de séries (TMDB logos)
        }
    }

    /**
     * Sistema de foco inteligente para Android TV, TV Box e Fire Stick.
     */
    private fun setupFocusSystem() {
        val cards = listOf(binding.cardLiveTv, binding.cardMovies, binding.cardSeries)
        
        cards.forEach { card ->
            card.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    // Efeito visual de escala para destaque no controle remoto
                    view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
                } else {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
