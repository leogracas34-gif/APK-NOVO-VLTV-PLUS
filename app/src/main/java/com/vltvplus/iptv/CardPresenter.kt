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
            // Cor de fundo enquanto carrega
            setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorPrimary))
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        // Agora o CardPresenter reconhece o Movie que está no seu HomeFragment
        if (item is Movie) {
            val cardView = viewHolder.view as ImageCardView

            // Define o tamanho do Card (Proporção 16:9 estilo Disney+)
            cardView.setMainImageDimensions(313, 176)

            // Carrega o logo transparente do TMDB
            Glide.with(cardView.context)
                .load(item.logoUrl)
                .centerInside() // Mantém a proporção do logo sem esticar
                .placeholder(R.color.colorPrimary) // Cor de fundo neutra
                .error(R.color.colorPrimary) // Caso o TMDB falhe
                .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        // Limpa a imagem para liberar memória da TV
        cardView.mainImage = null
        Glide.with(cardView.context).clear(cardView.mainImageView)
    }
}
