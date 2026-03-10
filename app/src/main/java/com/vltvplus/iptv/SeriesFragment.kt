package com.vltvplus.iptv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class SeriesFragment : Fragment(R.layout.fragment_home) {

    private val apiKey = "9b73f5dd15b8165b1b57419be2f29128"
    private lateinit var rowsSupportFragment: RowsSupportFragment
    private lateinit var mainAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializa a grade de linhas da Leanback
        setupRows()
    }

    private fun setupRows() {
        rowsSupportFragment = RowsSupportFragment()
        
        childFragmentManager.beginTransaction()
            .replace(R.id.homeMainGrid, rowsSupportFragment)
            .commit()

        val presenterSelector = ListRowPresenter()
        mainAdapter = ArrayObjectAdapter(presenterSelector)
        rowsSupportFragment.adapter = mainAdapter

        // Carrega as séries do banco de dados Room
        loadSeriesFromDatabase()
    }

    private fun loadSeriesFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            // Busca o conteúdo que foi marcado como série (Ex: categoria de séries no IPTV)
            // Aqui buscamos uma amostragem inicial para preencher a tela
            val savedSeries = db.movieDao().getMoviesByCategory("series_category_id").take(40)

            if (savedSeries.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val listRowAdapter = ArrayObjectAdapter(CardPresenter())
                    val header = HeaderItem("Séries em Destaque")
                    mainAdapter.add(ListRow(header, listRowAdapter))

                    // Busca os logos no TMDB para cada série encontrada
                    savedSeries.forEach { entity ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val logo = fetchSeriesLogoFromTMDB(entity.name)
                            withContext(Dispatchers.Main) {
                                // Adiciona a série ao trilho (Movie aqui é o nosso modelo de UI)
                                listRowAdapter.add(Movie(entity.name, logo))
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchSeriesLogoFromTMDB(query: String): String? {
        return try {
            // Busca especificamente por TV Shows (Séries) no TMDB em PT-BR
            val searchUrl = "https://api.themoviedb.org/3/search/tv?api_key=$apiKey&query=${query.replace(" ", "%20")}&language=pt-BR"
            val response = URL(searchUrl).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            
            if (results.length() > 0) {
                val seriesId = results.getJSONObject(0).getInt("id")
                
                // Busca as imagens/logos da série
                val imagesUrl = "https://api.themoviedb.org/3/tv/$seriesId/images?api_key=$apiKey&include_image_language=pt,en,null"
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
