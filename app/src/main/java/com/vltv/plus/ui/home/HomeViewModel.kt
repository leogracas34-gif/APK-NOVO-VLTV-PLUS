package com.vltv.plus.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vltv.plus.data.local.MediaEntity
import kotlinx.coroutines.launch

/**
 * ViewModel da Home do VLTV+
 * Responsável por integrar TMDB (logos em pt-BR) e dados do servidor.
 * Filtro exclusivo para lançamentos de 2026 (Janeiro em diante).
 */
class HomeViewModel : ViewModel() {

    private val TMDB_API_KEY = "9b73f5dd15b8165b1b57419be2f29128"
    private val TMDB_LANGUAGE = "pt-BR"

    // Lista de 10 itens para o Carrossel Infinito
    private val _bannerItems = MutableLiveData<List<MediaEntity>>()
    val bannerItems: LiveData<List<MediaEntity>> = _bannerItems

    private val _bannerImageUrl = MutableLiveData<String>()
    val bannerImageUrl: LiveData<String> = _bannerImageUrl

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadHomeContent()
    }

    private fun loadHomeContent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Aqui entrará a lógica para buscar o banner principal via TMDB
                _bannerImageUrl.value = "https://image.tmdb.org/t/p/original/path_to_banner.jpg"
                
                // Lógica para carregar categorias rápidas da Database inteligente (Room)
                loadCategoriesFromDatabase()

                // Sincronização e Filtro 2026:
                // 1. Busca todos os itens da Database
                // 2. Filtra apenas lançamentos de 2026 (Janeiro em diante)
                // 3. Pega os 10 mais recentes para o Carrossel
                syncBannerHighlights()
                
            } catch (e: Exception) {
                // Tratamento de erro silencioso para não quebrar a experiência do usuário
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun syncBannerHighlights() {
        // Esta função será alimentada pela sua Database Robusta
        // O filtro "2026" garante que nada de anos anteriores apareça no destaque
        /* val allMedia = repository.getAllMedia() 
           val filtered = allMedia.filter { it.releaseDate != null && it.releaseDate.contains("2026") }
                                  .take(10)
           _bannerItems.postValue(filtered)
        */
    }

    private suspend fun loadCategoriesFromDatabase() {
        // Implementação futura da busca no Room com suporte a GZip
    }

    /**
     * Função para buscar a logo/poster de um filme ou série especificamente em PT-BR
     * Substitui o texto pelo Logo dinâmico do TMDB
     */
    fun getMovieLogoPath(tmdbId: String): String {
        return "https://api.themoviedb.org/3/movie/$tmdbId/images?api_key=$TMDB_API_KEY&language=$TMDB_LANGUAGE"
    }
}
