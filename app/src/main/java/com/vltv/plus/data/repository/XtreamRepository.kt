package com.vltv.plus.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.vltv.plus.data.network.DnsManager
import com.vltv.plus.data.network.XtreamService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class XtreamRepository {
    
    suspend fun testLogin(username: String, password: String): Boolean {
        val (dns, response) = DnsManager.findWorkingDns(username, password) ?: return false
        
        // Salvar DNS vencedor
        saveWinningDns(dns)
        
        return response?.user_info != null
    }
    
    private fun saveWinningDns(dns: String) {
        // Implementar SharedPreferences depois
    }
}
