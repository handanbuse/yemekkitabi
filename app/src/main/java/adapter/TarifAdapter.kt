package adapter

import View.listeDirections
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.yemektariflerikitab.databinding.RecyclerrowBinding
import model.Tarif

class TarifAdapter(val TarifListesi:List<Tarif>):RecyclerView.Adapter<TarifAdapter.TarifHolder>() {
    class TarifHolder(val binding: RecyclerrowBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder {
    val reycylerRowBinding=RecyclerrowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  TarifHolder(reycylerRowBinding)
    }

    override fun getItemCount(): Int {
      return TarifListesi.size
    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) {
holder.binding.reycylerowText.text=TarifListesi[position].isim
        holder.itemView.setOnClickListener{
            val action=listeDirections.actionListeToTarif(bilgi = "eski",id=TarifListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}