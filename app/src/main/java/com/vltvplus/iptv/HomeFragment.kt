package com.vltvplus.iptv

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Modelo de dados para suportar o Logo do TMDB
data class Movie(val title: String, var logoUrl: String? = null)

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val apiKey = "9b73f5dd15b8165b1b57419be2f29128"
    private lateinit var rowsSupportFragment: RowsSupportFragment
    private lateinit var mainAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configuração dos Botões de Atalho para abrir as Telas Reais
        setupCategoryButtons(view)

        // 2. Configuração do Banner de Destaque
        val banner = view.findViewById<ImageView>(R.id.bannerImage)
        banner.isFocusable = true
        banner.requestFocus() // Foco inicial no Banner para controle remoto
        
        banner.setOnClickListener {
            Toast.makeText(context, "Abrindo Destaque...", Toast.LENGTH_SHORT).show()
        }

        // 3. Inicialização dos Trilhos (Grid Vertical)
        setupRows()
    }

    private fun setupCategoryButtons(view: View) {
        val btnLive = view.findViewById<Button>(R.id.btnLiveTv)
        val btnMovies = view.findViewById<Button>(R.id.btnMovies)
        val btnSeries = view.findViewById<Button>(R.id.btnSeries)

        btnLive.setOnClickListener {
            // No futuro: replaceFragment(LiveFragment())
            Toast.makeText(context, "Abrindo TV ao Vivo...", Toast.LENGTH_SHORT).show()
        }

        btnMovies.setOnClickListener {
            // Abre a tela real de Filmes
            replaceFragment(MoviesFragment())
        }

        btnSeries.setOnClickListener {
            // Abre a tela real de Séries
            replaceFragment(SeriesFragment())
        }
    }

    private fun setupRows() {
        rowsSupportFragment = RowsSupportFragment()
        
        childFragmentManager.beginTransaction()
            .replace(R.id.homeMainGrid, rowsSupportFragment)
            .commit()

        val presenterSelector = ListRowPresenter()
        mainAdapter = ArrayObjectAdapter(presenterSelector)
        rowsSupportFragment.adapter = mainAdapter

        // Carrega o conteúdo misto para a Home (Filmes e Séries)
        loadHomeContent()
    }

    private fun loadHomeContent() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            
            // Busca os dados reais salvos no Login
            val savedMovies = db.movieDao().getMoviesByCategory("0").take(15)
            val savedSeries = db.seriesDao().getAllSeries().take(15)

            withContext(Dispatchers.Main) {
                // 1. Cria o Trilho de Filmes
                if (savedMovies.isNotEmpty()) {
                    val movieAdapter = ArrayObjectAdapter(CardPresenter())
                    mainAdapter.add(ListRow(HeaderItem("Filmes Recomendados"), movieAdapter))
                    
                    savedMovies.forEach { entity ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val logo = fetchLogoFromTMDB(entity.name, "movie")
                            withContext(Dispatchers.Main) {
                                movieAdapter.add(Movie(entity.name, logo))
                            }
                        }
                    }
                }

                // 2. Cria o Trilho de Séries
                if (savedSeries.isNotEmpty()) {
                    val seriesAdapter = ArrayObjectAdapter(CardPresenter())
                    mainAdapter.add(ListRow(HeaderItem("Séries em Destaque"), seriesAdapter))
                    
                    savedSeries.forEach { entity ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val logo = fetchLogoFromTMDB(entity.name, "tv")
                            withContext(Dispatchers.Main) {
                                seriesAdapter.add(Movie(entity.name, logo))
                            }
                        }
                    }
                }

                // Se ainda estiver tudo vazio, tenta novamente em breve (banco sendo populado)
                if (savedMovies.isEmpty() && savedSeries.isEmpty()) {
                    view?.postDelayed({ loadHomeContent() }, 2000)
                }
            }
        }
    }

    private suspend fun fetchLogoFromTMDB(query: String, type: String): String? {
        return try {
            val searchPath = if (type == "movie") "search/movie" else "search/tv"
            val searchUrl = "https://api.themoviedb.org/3/$searchPath?api_key=$apiKey&query=${query.replace(" ", "%20")}&language=pt-BR"
            
            val response = URL(searchUrl).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            
            if (results.length() > 0) {
                val id = results.getJSONObject(0).getInt("id")
                val imagesPath = if (type == "movie") "movie/$id/images" else "tv/$id/images"
                
                val imagesUrl = "https://api.themoviedb.org/3/$imagesPath?api_key=$apiKey&include_image_language=pt,en,null"
                val imagesResponse = URL(imagesUrl).readText()
                val imagesJson = JSONObject(imagesResponse)
                val logos = imagesJson.getJSONArray("logos")
                
                if (logos.length() > 0) {
                    val filePath = logos.getJSONObject(0).getString("file_path")
                    "https://image.tmdb.org/t/p/original$filePath"
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null) // Permite voltar para a Home ao apertar "Back"
            .commit()
    }
}
