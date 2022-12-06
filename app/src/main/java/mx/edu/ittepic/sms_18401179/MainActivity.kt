package mx.edu.ittepic.sms_18401179

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import mx.edu.ittepic.sms_18401179.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),1)
        mostrar()
        binding.enviar.setOnClickListener {
            enviarSMS()
            mostrar()
        }
    }

    private fun enviarSMS(){
        try {
            val manejadorsms=SmsManager.getDefault()
            manejadorsms.sendTextMessage(binding.numero.text.toString(),null,
                binding.mensaje.text.toString(),null,null)
            Toast.makeText(this,"Mensaje enviado",Toast.LENGTH_LONG).show()
            insertarDB()
        }catch (e:Exception){
            AlertDialog.Builder(this)
                .setTitle("ERROR")
                .setMessage(e.message)
                .show()
        }
    }

    private fun mostrar(){
        var arreglo = ArrayList<String>()

        FirebaseFirestore.getInstance().collection("smsenviados").addSnapshotListener{q,e->
            if (e!=null){
                AlertDialog.Builder(this)
                    .setMessage(e.message).show()
                return@addSnapshotListener
            }
            for(documento in q!!){
                var cadena = "Numero: ${documento.getString("numero")}\n"+
                        "Mensaje: ${documento.getString("mensaje")}\n"+
                        "Fecha: ${documento.getString("fecha")}\n"
                arreglo.add(cadena)
            }
            var adapter =ArrayAdapter<String>(
                this,android.R.layout.simple_list_item_1,arreglo
            )
            binding.lista.adapter = adapter
        }



    }


    @SuppressLint("NewApi")
    private fun insertarDB(){
        var fecha = LocalDateTime.now()
        val formato = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")
        val fechaFormato = fecha.format(formato)
        var datos = hashMapOf(
            "numero" to binding.numero.text.toString(),
            "fecha" to fechaFormato,
            "mensaje" to binding.mensaje.text.toString()
        )
        FirebaseFirestore.getInstance().collection("smsenviados").add(datos)
            .addOnSuccessListener {
                binding.numero.text.clear()
                binding.mensaje.text.clear()
            }
            .addOnFailureListener{
                AlertDialog.Builder(this)
                    .setTitle("ERROR")
                    .setMessage(it.message)
                    .show()
            }
    }

}