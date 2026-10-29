package com.example.alarmaiot

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var edtCorreo: EditText
    private lateinit var edtContrasena: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnRegistrar: Button
    private lateinit var txtMensaje: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        edtCorreo = findViewById(R.id.edtCorreo)
        edtContrasena = findViewById(R.id.edtContrasena)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        txtMensaje = findViewById(R.id.txtMensaje)

        btnIngresar.setOnClickListener {
            procesarAcceso(false)
        }

        btnRegistrar.setOnClickListener {
            procesarAcceso(true)
        }
    }

    override fun onStart() {
        super.onStart()

        // Si ya existe una sesión iniciada,
        // no es necesario pedir nuevamente la contraseña.
        if (auth.currentUser != null) {
            abrirAlarma()
        }
    }

    private fun procesarAcceso(registro: Boolean) {
        val correo = edtCorreo.text.toString().trim()
        val contrasena = edtContrasena.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            txtMensaje.text = "Ingresa un correo válido."
            return
        }

        if (contrasena.isEmpty()) {
            txtMensaje.text = "Ingresa tu contraseña."
            return
        }

        if (registro && contrasena.length < 6) {
            txtMensaje.text =
                "La contraseña debe tener al menos 6 caracteres."
            return
        }

        btnIngresar.isEnabled = false
        btnRegistrar.isEnabled = false
        txtMensaje.text = "Conectando con Firebase..."

        if (registro) {
            registrarUsuario(correo, contrasena)
        } else {
            iniciarSesion(correo, contrasena)
        }
    }

    private fun registrarUsuario(
        correo: String,
        contrasena: String
    ) {
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { tarea ->

                if (tarea.isSuccessful) {
                    abrirAlarma()
                } else {
                    mostrarError(
                        "No se pudo crear la cuenta. " +
                                "Comprueba los datos y la conexión."
                    )
                }
            }
    }

    private fun iniciarSesion(
        correo: String,
        contrasena: String
    ) {
        auth.signInWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { tarea ->

                if (tarea.isSuccessful) {
                    abrirAlarma()
                } else {
                    mostrarError(
                        "No se pudo iniciar sesión. " +
                                "Comprueba tus credenciales y la conexión."
                    )
                }
            }
    }

    private fun mostrarError(mensaje: String) {
        txtMensaje.text = mensaje
        btnIngresar.isEnabled = true
        btnRegistrar.isEnabled = true
    }

    private fun abrirAlarma() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
