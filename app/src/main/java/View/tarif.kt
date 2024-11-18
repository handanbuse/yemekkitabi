package View

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout.Directions
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Dao
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.yemektariflerikitab.databinding.FragmentTarifBinding
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import model.Tarif
import roomdb.TarifDao
import roomdb.TarifDatabase
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream


class tarif : Fragment() {
    //binding ekle
    private var _binding: FragmentTarifBinding?=null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel:Uri?= null
    private var secilenBitmap : Bitmap? = null

    private lateinit var db:TarifDatabase
    private lateinit var tarifdao: TarifDao
    private val mDisposible= CompositeDisposable()
    private var secilenTarif:Tarif? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher() // burada çağırdık

        db= Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
       tarifdao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binding başlatma
        _binding= FragmentTarifBinding.inflate(inflater,container,false)
        val view=binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener{ gorselsec(it)}
        binding.kaydetbtn.setOnClickListener { kaydet(it) }
        binding.silbtn.setOnClickListener { sil(it) }

        //yeni-eski olayı için
        arguments?.let {


        val bilgi= tarifArgs.fromBundle(it).bilgi

            if(bilgi=="yeni"){
                secilenTarif=null
                binding.silbtn.isEnabled=false
                binding.kaydetbtn.isEnabled=true
                binding.isimEdittext.setText("")
                binding.malzemeedittex.setText("")

            }
            else{
                binding.silbtn.isEnabled=true
                binding.kaydetbtn.isEnabled=false
                val id=tarifArgs.fromBundle(it).id
                mDisposible.add(
                    tarifdao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)

                )




            }
        }

    }
    private fun  handleResponse(tarif:Tarif){
        binding.isimEdittext.setText(tarif.isim)
        binding.malzemeedittex.setText(tarif.malzeme)
        val bitmap=BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        secilenTarif= tarif


    }

    fun kaydet(view: View){
        val isim =binding.isimEdittext.text.toString()
        val malzeme = binding.malzemeedittex.text.toString()

        if(secilenBitmap !=null){
            val kucukBitmap =kucukBitmapOlustur(secilenBitmap!!, 300)
            val outputStream=ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val bytedizi= outputStream.toByteArray()


            val tarif=Tarif(isim,malzeme,bytedizi)
            //Rxjavayı kullanıcaz

            mDisposible.add(tarifdao.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert) )

        }

    }
    private fun handleResponseForInsert(){

        // bir önceki fragmente dön
        val action= tarifDirections.actionTarifToListe()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun sil(view: View){
        if(secilenTarif !=null){
            mDisposible.add(
                tarifdao.delete(tarif=secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)

            )

        }

    }



    private  fun kucukBitmapOlustur(KullanicisectiğiBitmap:Bitmap, maximumBoyut:Int): Bitmap{
        var width=KullanicisectiğiBitmap.width
        var height =KullanicisectiğiBitmap.height
        val BitmapOran :Double= width.toDouble() / height.toDouble()

        if(BitmapOran >1){
            //gorsel yatay
            width= maximumBoyut
            val kisayukseklik = width/BitmapOran
            height = kisayukseklik.toInt()

        }
        else{
            //dikeyse
            height =maximumBoyut
            val kisagenislik = height * BitmapOran
            width=kisagenislik.toInt()

        }

        return Bitmap.createScaledBitmap(KullanicisectiğiBitmap,width,height,true)

    }


    fun gorselsec(view: View){
        val permission = if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            Manifest.permission.READ_MEDIA_IMAGES
        }else{
            Manifest.permission.READ_MEDIA_IMAGES
        }

        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) !=PackageManager.PERMISSION_GRANTED){
            //izin verilmediyse
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                //snackbar göster
                Snackbar.make(view,"Galeriye erişim izni gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction(
                    "izin ver" ,
                    View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                    }

                ).show()

                    // izib isticezz
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }
            else{
                //izin verilmiş galeriye gidebilir
                val intentToGallery= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }

    }
    else {
            //izin verilmiş galeriye gidebilir
            val intentToGallery= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }


    }
    private fun registerLauncher(){
        activityResultLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult(),{result ->
            if(result.resultCode==AppCompatActivity.RESULT_OK){
                val intentFromResult= result.data
                if(intentFromResult !=null){
                    secilenGorsel = intentFromResult.data
                    try {
                        if(Build.VERSION.SDK_INT >=28){
                            val source =ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap=ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)

                        }else{
                            secilenBitmap=MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel!!)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e:IOException){
                        println(e.localizedMessage)
                    }


                }

            }
        })
        permissionLauncher= registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result ->
            if (result){
                //izin verildi
                //galeriye gidebilir
                val intentToGallery= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }else{
                Toast.makeText(requireContext(),"izin verilmedi",Toast.LENGTH_LONG).show()
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposible.clear()
    }

}






