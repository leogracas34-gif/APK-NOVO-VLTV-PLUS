package com.vltv.plus.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vltv.plus.databinding.ItemBannerBinding
import com.vltv.plus.data.local.MediaEntity

class BannerAdapter(private val onBannerClick: (MediaEntity) -> Unit) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    private var items: List<MediaEntity> = emptyList()

    fun submitList(newList: List<MediaEntity>) {
        items = newList
        notifyDataSetChanged()
    }

    // Lógica para Loop Infinito: retorna um número muito alto
    override fun getItemCount(): Int = if (items.isEmpty()) 0 else Integer.MAX_VALUE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val realPosition = position % items.size
        holder.bind(items[realPosition])
    }

    inner class BannerViewHolder(private val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaEntity) {
            // Carrega o Poster de fundo
            Glide.with(binding.ivBannerBackground.context)
                .load(item.streamIcon)
                .into(binding.ivBannerBackground)

            // Carrega a LOGO do TMDB (substitui o texto)
            Glide.with(binding.ivMovieLogo.context)
                .load(item.tmdbLogo)
                .into(binding.ivMovieLogo)

            binding.tvDescription.text = item.customData // Sinopse salva aqui

            // Configuração de Foco (D-Pad) para Android TV
            binding.btnWatch.apply {
                setOnClickListener { onBannerClick(item) }
                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
                    } else {
                        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                    }
                }
            }
        }
    }
}
