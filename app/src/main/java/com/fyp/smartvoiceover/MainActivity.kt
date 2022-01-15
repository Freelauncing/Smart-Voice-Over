package com.fyp.smartvoiceover

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fyp.smartvoiceover.assistant.AssistantActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {

    // view initializations
    private lateinit var floatingActionButton: FloatingActionButton

    // permission code
    val RecordAudioRequestCode : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initializing views
        floatingActionButton = findViewById(R.id.floating_action_button)

        // getting permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        // setting on click listener
        floatingActionButton.setOnClickListener{
            startActivity(Intent(this@MainActivity, AssistantActivity::class.java))
        }
    }

    // on request permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // for audio
        if (requestCode == RecordAudioRequestCode && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(
                this,
                "Permission Granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.email -> {
                showImageChoiceDialogue()
            }
        }
        return true
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RecordAudioRequestCode
            )
        }
    }

    private fun showImageChoiceDialogue(){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.getLayoutInflater()
        val dialogView = inflater.inflate(R.layout.custom_email_dialogue, null)
        dialogBuilder.setView(dialogView)

        val btn_no = dialogView.findViewById<Button>(R.id.btn_no)
        val btn_yes = dialogView.findViewById<Button>(R.id.btn_yes)

        val email = dialogView.findViewById<EditText>(R.id.Email)
        val password = dialogView.findViewById<EditText>(R.id.Password)

        if(!getEmail().isNullOrEmpty()){
            email.setText(getEmail())
        }
        if(!getPassword().isNullOrEmpty()){
            password.setText(getPassword())
        }

        val alertDialog = dialogBuilder.create()

        btn_no.setOnClickListener {
            alertDialog.dismiss()
        }

        btn_yes.setOnClickListener {
            storeCredentials(email.text.toString(), password.text.toString())
            Toast.makeText(this,"Saved", Toast.LENGTH_SHORT)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    fun storeCredentials(email:String,password:String){
        val sharedPreference =  getSharedPreferences("SECRETS", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("Email",email)
        editor.putString("Password",password)
        editor.commit()
    }

    fun getEmail(): String?{
        val sharedPreference =  getSharedPreferences("SECRETS", Context.MODE_PRIVATE)
        return sharedPreference.getString("Email",null)
    }
    fun getPassword(): String?{
        val sharedPreference =  getSharedPreferences("SECRETS", Context.MODE_PRIVATE)
        return sharedPreference.getString("Password",null)
    }
}