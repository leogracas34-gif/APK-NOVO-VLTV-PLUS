package com.vltv.plus.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.vltv.plus.R
import com.vltv.plus.databinding.FragmentLoginBinding
import com.vltv.plus.ui.home.HomeFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.vltv.plus.data.network.XtreamService
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val dnsList = listOf(
        "http://fibercdn.sbs", "http://tvblack.shop", "http://redeinternadestiny.top",
        "http://blackstartv.shop", "http://blackdns.shop", "http://blackdeluxe.shop",
        "http://ranos.sbs", "http://cmdtv.casa", "http://cmdtv.pro",
        "http://cmdtv.sbs", "http://cmdtv.top", "http://cmdbr.life"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                performTurboLogin(user, pass)
            } else {
                binding.tvError.text = "Preencha todos os campos"
            }
        }
    }

    private fun performTurboLogin(username: String, password: String) {
        hideKeyboard()
        // Feedback imediato na UI
        binding.btnLogin.text = "Conectando..."
        binding.btnLogin.isEnabled = false
        binding.tvError.text = ""

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            // A busca roda em IO mas retorna o resultado para a Main Thread
            val winner = withContext(Dispatchers.IO) {
                findFastestDns(username, password)
            }

            if (winner != null) {
                saveDnsPreference(winner)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                binding.btnLogin.text = "Entrar"
                binding.btnLogin.isEnabled = true
                binding.tvError.text = "Erro: Servidores indisponíveis ou dados incorretos."
            }
        }
    }

    private suspend fun findFastestDns(user: String, pass: String): String? = coroutineScope {
        val deferreds = dnsList.map { url ->
            async(Dispatchers.IO) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .build()

                    val retrofit = Retrofit.Builder()
                        .baseUrl(if (url.endsWith("/")) url else "$url/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build()

                    val service = retrofit.create(XtreamService::class.java)
                    val response = service.getProfile(user, pass)

                    if (response.isSuccessful && response.body()?.user_info != null) {
                        url
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }

        // Seleciona o primeiro DNS que retornar um valor não nulo
        val result = select<String?> {
            deferreds.forEach { deferred ->
                deferred.onAwait { it }
            }
        }
        
        // Cancela todas as outras tentativas pendentes
        deferreds.forEach { it.cancel() }
        result
    }

    private fun saveDnsPreference(dns: String) {
        val sharedPref = requireActivity().getSharedPreferences("VLTV_PREFS", Context.MODE_PRIVATE)
        sharedPref.edit().putString("winning_dns", dns).apply()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
