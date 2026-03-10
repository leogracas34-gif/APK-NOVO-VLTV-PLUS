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
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val dao = db.streamDao() // Corrigido para usar o streamDao() do AppDatabase v6
                
                // Pegamos todos os filmes usando a função correta do seu DAO
                val allMovies = dao.getAllVods() 

                withContext(Dispatchers.Main) {
                    if (allMovies.isNotEmpty()) {
                        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
                        val header = HeaderItem("Todos os Filmes")
                        
                        // Especificando o tipo VodEntity para remover o erro de ambiguidade do Kotlin
                        allMovies.forEach { entity: VodEntity ->
                            // Usa o Movie(title, logoUrl) que você definiu na HomeFragment
                            listRowAdapter.add(Movie(entity.name, entity.stream_icon))
                        }
                        
                        mainAdapter.add(ListRow(header, listRowAdapter))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
