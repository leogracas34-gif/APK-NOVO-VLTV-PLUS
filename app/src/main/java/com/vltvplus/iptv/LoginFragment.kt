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

    // Lista dos 12 DNS que combinamos (coloque as URLs reais aqui depois)
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

            // Altera o visual do botão para mostrar que está processando
            loginButton.text = "CONECTANDO..."
            loginButton.isEnabled = false

            // Inicia a corrida dos 12 DNS em segundo plano para não travar a tela
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val vencedor = iniciarCorridaDns(dnsList)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Conectado no DNS: $vencedor", Toast.LENGTH_SHORT).show()
                        
                        // 1. Dispara o download silencioso
                        iniciarPreCarregamentoTurboEmSegundoPlano(vencedor, user, pass)
                        
                        // 2. Pula para a Home sem esperar o download terminar
                        abrirTelaHome()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        loginButton.text = "ENTRAR"
                        loginButton.isEnabled = true
                        Toast.makeText(requireContext(), "Nenhum DNS respondeu. Tente novamente.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Lógica Exata: Dispara os 12, o primeiro que responder OK cancela os outros imediatamente
    private suspend fun iniciarCorridaDns(urls: List<String>): String = coroutineScope {
        val channel = Channel<String>()
        
        urls.forEach { url ->
            launch {
                try {
                    if (testarConexaoDns(url)) {
                        channel.send(url) // Envia o vencedor para o canal
                    }
                } catch (e: Exception) {
                    // Se este falhar, ignora em silêncio e deixa os outros continuarem a corrida
                }
            }
        }

        // Fica aguardando o milissegundo em que o primeiro servidor enviar o OK
        val dnsVencedor = channel.receive()
        
        // Cancela e mata imediatamente a busca nos outros servidores que ficaram para trás
        coroutineContext.cancelChildren()
        channel.close()
        
        return@coroutineScope dnsVencedor
    }

    // Testa a conexão para ver se o servidor IPTV está online
    private fun testarConexaoDns(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 2500 // Tempo máximo de resposta bem agressivo (2.5 segundos)
            connection.readTimeout = 2500
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode == 200 || responseCode == 401 // 200 (OK) ou 401 (API viva, mas pede login)
        } catch (e: Exception) {
            false
        }
    }

    // Função que abrigará o Gzip e o Banco de Dados no futuro
    private fun iniciarPreCarregamentoTurboEmSegundoPlano(dns: String, user: String, pass: String) {
        // O código do OkHttp com Gzip e DoH (Anti-bloqueio) será injetado aqui
        // Isso rodará 100% em background sem congelar a interface
    }

    // Substitui o cenário atual pelo cenário da Home
    private fun abrirTelaHome() {
        // Como o HomeFragment ainda não existe, coloquei um Toast temporário para o código compilar no GitHub
        Toast.makeText(requireContext(), "Iniciando Home Instantânea...", Toast.LENGTH_SHORT).show()
        
        // O código final será este abaixo (comentado até criarmos a tela Home):
        /*
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
        */
    }
}
