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

        loginButton.setOnClickListener {
            val user = usernameEditText.text.toString().trim()
            val pass = passwordEditText.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha usuário e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Feedback visual de conexão para o usuário (TV/Mobile)
            loginButton.text = "CONECTANDO..."
            loginButton.isEnabled = false

            // Corrida dos 12 DNS em segundo plano (Não trava a UI)
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val vencedor = iniciarCorridaDns(dnsList)
                    
                    withContext(Dispatchers.Main) {
                        // 1. Download silencioso em background
                        iniciarPreCarregamentoTurboEmSegundoPlano(vencedor, user, pass)
                        
                        // 2. Abre a Home instantaneamente (Lógica Disney+)
                        abrirTelaHome()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        loginButton.text = "ENTRAR"
                        loginButton.isEnabled = true
                        Toast.makeText(requireContext(), "Erro de conexão. Verifique os dados.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private suspend fun iniciarCorridaDns(urls: List<String>): String = coroutineScope {
        val channel = Channel<String>()
        
        urls.forEach { url ->
            launch {
                try {
                    if (testarConexaoDns(url)) {
                        channel.send(url)
                    }
                } catch (e: Exception) {
                    // Falha em um DNS não interrompe os outros
                }
            }
        }

        val dnsVencedor = channel.receive()
        
        // Mata as outras 11 tentativas para economizar bateria e processamento
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

    private fun iniciarPreCarregamentoTurboEmSegundoPlano(dns: String, user: String, pass: String) {
        // Futura integração com Room para salvar os dados enquanto o usuário navega na Home
    }

    private fun abrirTelaHome() {
        // Realiza a troca de tela para o HomeFragment com suporte a controle remoto
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
    }
}
