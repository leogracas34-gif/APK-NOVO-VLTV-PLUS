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
                performTurboLogin(username = user, password = pass)
            } else {
                binding.tvError.text = "Preencha todos os campos"
            }
        }
    }

    private fun performTurboLogin(username: String, password: String) {
        hideKeyboard()
        binding.btnLogin.text = "Conectando..."
        binding.btnLogin.isEnabled = false
        binding.tvError.text = ""

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            // Agora recebemos o resultado ou a mensagem de erro técnica
            val result = withContext(Dispatchers.IO) {
                findFastestDns(username, password)
            }

            if (result.winner != null) {
                saveDnsPreference(result.winner)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                binding.btnLogin.text = "Entrar"
                binding.btnLogin.isEnabled = true
                // Exibe o erro técnico para você saber por que não conectou
                binding.tvError.text = "Erro: ${result.lastErrorMessage}"
            }
        }
    }

    // Classe auxiliar para capturar o vencedor e o erro ao mesmo tempo
    data class LoginResult(val winner: String? = null, val lastErrorMessage: String? = null)

    private suspend fun findFastestDns(user: String, pass: String): LoginResult = coroutineScope {
        var errorLog = "Nenhum servidor respondeu"
        
        val deferreds = dnsList.map { url ->
            async(Dispatchers.IO) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(7, TimeUnit.SECONDS)
                        .readTimeout(7, TimeUnit.SECONDS)
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
                    } else {
                        "Falha: ${response.code()} em $url"
                    }
                } catch (e: Exception) {
                    "Erro: ${e.message} em $url"
                }
            }
        }

        val fastResult = select<String?> {
            deferreds.forEach { deferred ->
                deferred.onAwait { res ->
                    if (res != null && !res.startsWith("Falha") && !res.startsWith("Erro")) res else null
                }
            }
            // Timeout de segurança caso nenhum responda rápido
            launch { delay(8000) }.invokeOnCompletion { }
        }

        // Se falhou, pegamos a mensagem do primeiro para debugar
        if (fastResult == null) {
            val firstResponse = deferreds.first().await()
            errorLog = firstResponse ?: "Servidores offline"
        }

        deferreds.forEach { it.cancel() }
        LoginResult(winner = fastResult, lastErrorMessage = errorLog)
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
