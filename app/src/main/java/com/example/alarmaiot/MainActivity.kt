package com.example.alarmaiot

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    // Elementos de la interfaz
    private lateinit var txtEstado: TextView
    private lateinit var txtSensor: TextView
    private lateinit var txtLed: TextView
    private lateinit var txtConexion: TextView

    private lateinit var switchLuz: SwitchCompat
    private lateinit var btnArmar: Button
    private lateinit var btnSilenciar: Button
    private lateinit var btnCerrarSesion: Button

    // Firebase Realtime Database
    private lateinit var referenciaAlarma: DatabaseReference
    private var listenerAlarma: ValueEventListener? = null

    // Estado de la alarma
    private var sistemaArmado = true
    private var luzDetectada = false
    private var alarmaSilenciada = false

    // Control de sincronización
    private var datosCargados = false
    private var actualizandoDesdeFirebase = false

    // Sirena simulada
    private var reproductor: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar que exista una sesión iniciada
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            abrirLogin()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { vista, insets ->

            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            vista.setPadding(
                barras.left,
                barras.top,
                barras.right,
                barras.bottom
            )

            insets
        }

        // Vincular los elementos de la interfaz
        txtEstado = findViewById(R.id.txtEstado)
        txtSensor = findViewById(R.id.txtSensor)
        txtLed = findViewById(R.id.txtLed)
        txtConexion = findViewById(R.id.txtConexion)

        switchLuz = findViewById(R.id.switchLuz)
        btnArmar = findViewById(R.id.btnArmar)
        btnSilenciar = findViewById(R.id.btnSilenciar)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Cada usuario tiene su propia alarma
        referenciaAlarma = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(usuario.uid)
            .child("alarma")

        configurarBotones()
        escucharCambiosFirebase()
    }

    private fun configurarBotones() {

        // Simular el sensor LDR
        switchLuz.setOnCheckedChangeListener { _, activado ->

            if (!datosCargados || actualizandoDesdeFirebase) {
                return@setOnCheckedChangeListener
            }

            val cambios = mutableMapOf<String, Any>(
                "luzDetectada" to activado
            )

            // Si desaparece la luz, la sirena se reinicia
            // para poder sonar en la siguiente intrusión.
            if (!activado) {
                cambios["alarmaSilenciada"] = false
            }

            referenciaAlarma.updateChildren(cambios)
                .addOnFailureListener {
                    mostrarError(
                        "No se pudo actualizar el sensor."
                    )
                }
        }

        // Armar o desarmar el sistema
        btnArmar.setOnClickListener {

            if (!datosCargados) {
                return@setOnClickListener
            }

            if (sistemaArmado) {

                // Desarmar permite una apertura autorizada
                referenciaAlarma.updateChildren(
                    mapOf(
                        "sistemaArmado" to false,
                        "alarmaSilenciada" to false
                    )
                ).addOnFailureListener {
                    mostrarError(
                        "No se pudo desarmar el sistema."
                    )
                }

            } else {

                // No permitir armar mientras haya luz
                if (luzDetectada) {

                    Toast.makeText(
                        this,
                        "Primero cierra el compartimento.",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    referenciaAlarma.updateChildren(
                        mapOf(
                            "sistemaArmado" to true,
                            "alarmaSilenciada" to false
                        )
                    ).addOnFailureListener {
                        mostrarError(
                            "No se pudo armar el sistema."
                        )
                    }
                }
            }
        }

        // Silenciar la sirena sin apagar el LED
        btnSilenciar.setOnClickListener {

            if (!datosCargados) {
                return@setOnClickListener
            }

            if (sistemaArmado && luzDetectada) {

                referenciaAlarma
                    .child("alarmaSilenciada")
                    .setValue(true)
                    .addOnFailureListener {
                        mostrarError(
                            "No se pudo silenciar la sirena."
                        )
                    }
            }
        }

        // Cerrar la sesión de Firebase
        btnCerrarSesion.setOnClickListener {

            detenerSirena()

            FirebaseAuth.getInstance().signOut()

            abrirLogin()
        }
    }

    private fun escucharCambiosFirebase() {

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                // Inicializar la alarma si es la primera vez
                // que esta cuenta utiliza la aplicación.
                if (!snapshot.exists()) {

                    referenciaAlarma.setValue(
                        mapOf(
                            "sistemaArmado" to true,
                            "luzDetectada" to false,
                            "alarmaSilenciada" to false
                        )
                    ).addOnFailureListener {
                        mostrarError(
                            "No se pudo inicializar la alarma."
                        )
                    }

                    return
                }

                // Leer el estado almacenado en Firebase
                sistemaArmado = snapshot
                    .child("sistemaArmado")
                    .getValue(Boolean::class.java) ?: true

                luzDetectada = snapshot
                    .child("luzDetectada")
                    .getValue(Boolean::class.java) ?: false

                alarmaSilenciada = snapshot
                    .child("alarmaSilenciada")
                    .getValue(Boolean::class.java) ?: false

                datosCargados = true

                // Actualizar el interruptor sin enviar
                // nuevamente el mismo cambio a Firebase.
                actualizandoDesdeFirebase = true

                switchLuz.isChecked = luzDetectada

                actualizandoDesdeFirebase = false

                txtConexion.text =
                    "Estado cargado desde Firebase"

                txtConexion.setTextColor(
                    Color.parseColor("#15803D")
                )

                actualizarSistema()
            }

            override fun onCancelled(error: DatabaseError) {

                datosCargados = false

                switchLuz.isEnabled = false
                btnArmar.isEnabled = false
                btnSilenciar.isEnabled = false

                txtConexion.text =
                    "Error de Firebase: ${error.message}"

                txtConexion.setTextColor(
                    Color.parseColor("#B91C1C")
                )

                detenerSirena()
            }
        }

        listenerAlarma = listener

        referenciaAlarma.addValueEventListener(listener)
    }

    private fun actualizarSistema() {

        val intrusionDetectada =
            sistemaArmado && luzDetectada

        // Actualizar el sensor simulado
        txtSensor.text = if (luzDetectada) {
            "Sensor LDR: LUZ DETECTADA"
        } else {
            "Sensor LDR: SIN LUZ"
        }

        // Actualizar el estado general
        when {

            !sistemaArmado -> {
                mostrarEstado(
                    "SISTEMA DESARMADO",
                    "#1D4ED8",
                    "#DBEAFE"
                )
            }

            intrusionDetectada && alarmaSilenciada -> {
                mostrarEstado(
                    "ALARMA SILENCIADA",
                    "#92400E",
                    "#FEF3C7"
                )
            }

            intrusionDetectada -> {
                mostrarEstado(
                    "¡ALARMA ACTIVADA!",
                    "#B91C1C",
                    "#FEE2E2"
                )
            }

            else -> {
                mostrarEstado(
                    "SISTEMA ARMADO Y PROTEGIDO",
                    "#15803D",
                    "#DCFCE7"
                )
            }
        }

        // Simulación del LED rojo
        if (intrusionDetectada) {

            txtLed.text = "LED ROJO: ENCENDIDO"

            txtLed.setTextColor(
                Color.parseColor("#B91C1C")
            )

            txtLed.setBackgroundColor(
                Color.parseColor("#FEE2E2")
            )

        } else {

            txtLed.text = "LED rojo: APAGADO"

            txtLed.setTextColor(
                Color.parseColor("#475569")
            )

            txtLed.setBackgroundColor(
                Color.parseColor("#E2E8F0")
            )
        }

        // Actualizar los botones
        btnArmar.text = if (sistemaArmado) {
            "Desarmar sistema"
        } else {
            "Armar sistema"
        }

        btnArmar.isEnabled = datosCargados
        switchLuz.isEnabled = datosCargados

        btnSilenciar.isEnabled =
            datosCargados &&
                    intrusionDetectada &&
                    !alarmaSilenciada

        // Controlar la sirena simulada
        if (intrusionDetectada && !alarmaSilenciada) {
            iniciarSirena()
        } else {
            detenerSirena()
        }
    }

    private fun mostrarEstado(
        mensaje: String,
        colorTexto: String,
        colorFondo: String
    ) {

        txtEstado.text = mensaje

        txtEstado.setTextColor(
            Color.parseColor(colorTexto)
        )

        txtEstado.setBackgroundColor(
            Color.parseColor(colorFondo)
        )
    }

    private fun iniciarSirena() {

        // Evitar reproducir varias sirenas simultáneas
        if (reproductor != null) return

        try {

            val sonido = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            ) ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )

            if (sonido != null) {

                reproductor = MediaPlayer.create(
                    this,
                    sonido
                )

                reproductor?.isLooping = true
                reproductor?.start()
            }

        } catch (e: Exception) {

            e.printStackTrace()
            detenerSirena()
        }
    }

    private fun detenerSirena() {

        reproductor?.let { player ->

            try {

                if (player.isPlaying) {
                    player.stop()
                }

            } catch (e: IllegalStateException) {

                e.printStackTrace()

            } finally {

                player.release()
            }
        }

        reproductor = null
    }

    private fun mostrarError(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun abrirLogin() {

        startActivity(
            Intent(this, LoginActivity::class.java)
        )

        finish()
    }

    override fun onDestroy() {

        // Dejar de escuchar Firebase al cerrar la pantalla
        if (::referenciaAlarma.isInitialized) {

            listenerAlarma?.let {
                referenciaAlarma.removeEventListener(it)
            }
        }

        detenerSirena()

        super.onDestroy()
    }
}
