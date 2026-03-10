package com.vltvplus.iptv

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class CardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            // Estilo Disney+: Apenas imagem, sem barra de texto embaixo
            cardType = ImageCardView.CARD_TYPE_MAIN_ONLY
            setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorPrimary))
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val movie = item as Movie // Classe que criaremos no próximo passo
        val cardView = viewHolder.view as ImageCardView

        // Define o tamanho do Card (Proporção 16:9 estilo Banner)
        cardView.setMainImageDimensions(313, 176)

        // Lógica do Logo TMDB: Carrega a imagem transparente no lugar do texto
        // A URL do logo será processada no HomeFragment e passada para cá
        Glide.with(cardView.context)
            .load(movie.logoUrl) 
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
    }
}
