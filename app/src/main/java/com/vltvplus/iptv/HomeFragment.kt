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

        // 1. Configuração dos Botões de Atalho com Foco Leanback
        setupCategoryButtons(view)

        // 2. Configuração do Banner de Destaque
        val banner = view.findViewById<ImageView>(R.id.bannerImage)
        banner.setOnClickListener {
            Toast.makeText(context, "Abrindo Destaque...", Toast.LENGTH_SHORT).show()
        }

        // 3. Inicialização dos Trilhos de Filmes (Grid Vertical)
        setupRows()
    }

    private fun setupCategoryButtons(view: View) {
        val btnLive = view.findViewById<Button>(R.id.btnLiveTv)
        val btnMovies = view.findViewById<Button>(R.id.btnMovies)
        val btnSeries = view.findViewById<Button>(R.id.btnSeries)

        val categoryClickListener = View.OnClickListener { v ->
            val msg = when (v.id) {
                R.id.btnLiveTv -> "Abrindo TV ao Vivo..."
                R.id.btnMovies -> "Abrindo Filmes..."
                R.id.btnSeries -> "Abrindo Séries..."
                else -> ""
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        btnLive.setOnClickListener(categoryClickListener)
        btnMovies.setOnClickListener(categoryClickListener)
        btnSeries.setOnClickListener(categoryClickListener)
    }

    private fun setupRows() {
        rowsSupportFragment = RowsSupportFragment()
        
        childFragmentManager.beginTransaction()
            .replace(R.id.homeMainGrid, rowsSupportFragment)
            .commit()

        val presenterSelector = ListRowPresenter()
        mainAdapter = ArrayObjectAdapter(presenterSelector)
        rowsSupportFragment.adapter = mainAdapter

        // Inicia a carga dos dados reais vindos do Banco de Dados Room
        loadMoviesFromDatabase()
    }

    private fun loadMoviesFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            // Busca todos os filmes salvos (limitando a 15 para o primeiro trilho de teste)
            val savedMovies = db.movieDao().getMoviesByCategory("0").take(15)

            if (savedMovies.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val listRowAdapter = ArrayObjectAdapter(CardPresenter())
                    val header = HeaderItem("Conteúdo para Você")
                    mainAdapter.add(ListRow(header, listRowAdapter))

                    // Para cada filme real, busca o logo no TMDB
                    savedMovies.forEach { entity ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val logo = fetchLogoFromTMDB(entity.name)
                            withContext(Dispatchers.Main) {
                                listRowAdapter.add(Movie(entity.name, logo))
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchLogoFromTMDB(query: String): String? {
        return try {
            // Busca o filme priorizando resultados em Português do Brasil
            val searchUrl = "https://api.themoviedb.org/3/search/movie?api_key=$apiKey&query=${query.replace(" ", "%20")}&language=pt-BR"
            val response = URL(searchUrl).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            
            if (results.length() > 0) {
                val movieId = results.getJSONObject(0).getInt("id")
                
                // Busca as imagens solicitando logos em PT, EN ou sem idioma (null)
                val imagesUrl = "https://api.themoviedb.org/3/movie/$movieId/images?api_key=$apiKey&include_image_language=pt,en,null"
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
}
