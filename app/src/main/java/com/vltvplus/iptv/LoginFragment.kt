package com.vltvplus.iptv

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class LoginFragment : Fragment(R.layout.fragment_login) {

    // Lista oficial dos 12 DNS reais do VLTV+
    private val dnsList = listOf(
        "http://fibercdn.sbs",
        "http://tvblack.shop",
        "http://redeinternadestiny.top",
        "http://blackstartv.shop",
        "http://blackdns.shop",
        "http://blackdeluxe.shop",
        "http://ranos.sbs",
        "http://cmdtv.casa",
        "http://cmdtv.pro",
        "http://cmdtv.sbs",
        "http://cmdtv.top",
        "http://cmdbr.life"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginButton = view.findViewById<Button>(R.id.loginButton)

        // CONFIGURAÇÃO COMPLETA DE CONTROLE REMOTO (LEANBACK/D-PAD)
        usernameEditText.isFocusable = true
        usernameEditText.requestFocus() 
        
        passwordEditText.isFocusable = true
        loginButton.isFocusable = true

        usernameEditText.nextFocusDownId = R.id.passwordEditText
        passwordEditText.nextFocusUpId = R.id.usernameEditText
        passwordEditText.nextFocusDownId = R.id.loginButton
        loginButton.nextFocusUpId = R.id.passwordEditText

        loginButton.setOnClickListener {
            val user = usernameEditText.text.toString().trim()
            val pass = passwordEditText.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha usuário e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.text = "CONECTANDO..."
            loginButton.isEnabled = false

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // 1. Corrida de DNS
                    val vencedor = iniciarCorridaDns(dnsList)
                    
                    withContext(Dispatchers.Main) {
                        loginButton.text = "SINCRONIZANDO..."
                    }

                    // 2. Sincronização Obrigatória (O Login ESPERA o banco carregar aqui)
                    val repository = IptvRepository(requireContext().applicationContext)
                    val sucessoSincronizacao = repository.sincronizarFilmes(vencedor, user, pass)

                    withContext(Dispatchers.Main) {
                        if (isAdded) { 
                            if (sucessoSincronizacao) {
                                // 3. Abre a Home apenas com o banco preenchido
                                abrirTelaHome()
                            } else {
                                resetarLogin(loginButton, "Erro ao carregar lista.")
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            resetarLogin(loginButton, "Erro de conexão. Verifique os dados.")
                        }
                    }
                }
            }
        }
    }

    private fun resetarLogin(button: Button, mensagem: String) {
        button.text = "ENTRAR"
        button.isEnabled = true
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show()
    }

    private suspend fun iniciarCorridaDns(urls: List<String>): String = coroutineScope {
        val channel = Channel<String>()
        urls.forEach { url ->
            launch {
                try {
                    if (testarConexaoDns(url)) {
                        channel.send(url)
                    }
                } catch (e: Exception) {}
            }
        }
        val dnsVencedor = channel.receive()
        coroutineContext.cancelChildren()
        channel.close()
        return@coroutineScope dnsVencedor
    }

    private fun testarConexaoDns(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 2500
            connection.readTimeout = 2500
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode == 200 || responseCode == 401
        } catch (e: Exception) {
            false
        }
    }

    private fun abrirTelaHome() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
    }
}
