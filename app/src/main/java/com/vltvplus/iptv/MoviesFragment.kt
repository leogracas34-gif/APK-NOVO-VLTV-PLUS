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

class MoviesFragment : Fragment(R.layout.fragment_home) {

    private lateinit var rowsSupportFragment: RowsSupportFragment
    private lateinit var mainAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        // Carrega os filmes do banco de dados organizados por trilhos
        loadContentFromDatabase()
    }

    private fun loadContentFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            // Pegamos todos os filmes para exibir na tela de Filmes
            val allMovies = db.movieDao().getMoviesByCategory("0") 

            withContext(Dispatchers.Main) {
                if (allMovies.isNotEmpty()) {
                    val listRowAdapter = ArrayObjectAdapter(CardPresenter())
                    val header = HeaderItem("Todos os Filmes")
                    
                    allMovies.forEach { entity ->
                        // Cria o objeto Movie que o CardPresenter entende
                        listRowAdapter.add(Movie(entity.name, null))
                    }
                    
                    mainAdapter.add(ListRow(header, listRowAdapter))
                }
            }
        }
    }
}
