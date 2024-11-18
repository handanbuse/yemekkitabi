package View

import adapter.TarifAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.yemektariflerikitab.databinding.FragmentListeBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import model.Tarif
import roomdb.TarifDao
import roomdb.TarifDatabase


class liste : Fragment() {
    //binding ekle
    private var _binding:FragmentListeBinding?=null
    private val binding get() = _binding!!

    private lateinit var db: TarifDatabase
    private lateinit var tarifdao: TarifDao
    private val mDisposible= CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db= Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
        tarifdao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binding başlatma
        _binding= FragmentListeBinding.inflate(inflater,container,false)
        val view=binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //onclick metodunu burdan almalısın fun ile çalışmaz
        binding.floatingActionButton2.setOnClickListener{ yeniekle(it)}
        binding.tarifRecyclerview.layoutManager=LinearLayoutManager(requireContext())
        verileriAl()
    }
    private  fun verileriAl(){
        mDisposible.add(
            tarifdao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )

    }
    private fun handleResponse(tarifler : List<Tarif>){
       val adapter=TarifAdapter(tarifler)
         binding.tarifRecyclerview.adapter= adapter


    }
    fun yeniekle(view:View){
        val action=listeDirections.actionListeToTarif(bilgi = "yeni", id=-1)
        Navigation.findNavController(view).navigate(action)

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposible.clear()
    }

}